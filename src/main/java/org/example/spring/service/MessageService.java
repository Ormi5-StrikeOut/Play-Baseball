package org.example.spring.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.constant.ErrorCode;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.MemberRole;
import org.example.spring.domain.message.Message;
import org.example.spring.domain.message.MessageMember;
import org.example.spring.domain.message.MessageRoom;
import org.example.spring.domain.message.messageDto.MessageRequestDto;
import org.example.spring.domain.message.messageDto.MessageResponseDto;
import org.example.spring.domain.message.messageDto.MessageRoomResponseDto;
import org.example.spring.exception.MessageException;
import org.example.spring.redis.RedisPublisher;
import org.example.spring.redis.RedisSubscriber;
import org.example.spring.repository.MemberRepository;
import org.example.spring.repository.message.MessageMemberRepository;
import org.example.spring.repository.message.MessageRepository;
import org.example.spring.repository.message.MessageRoomRepository;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;

    private final MessageRoomRepository messageRoomRepository;

    private final MessageMemberRepository messageMemberRepository;

    private final MemberRepository memberRepository;

    private final RedisPublisher redisPublisher;

    private final Map<String, ChannelTopic> topics = new HashMap<>();

    private final RedisMessageListenerContainer redisMessageListener;

    private final RedisSubscriber redisSubscriber;

    private final JwtTokenValidator jwtTokenValidator;

    private final HttpServletRequest request;

    /* 새로운 메시지 생성, Redis 채널 발행 */
    public MessageResponseDto createMessage(MessageRequestDto mrd) {
        MessageRoom messageRoom = verifiedMessageRoom(mrd.getMessageRoomId());

        Member member = validateUserRole(mrd.getMemberId());

        Message message = saveMessage(mrd, messageRoom, member);

        Message savedMessage = messageRepository.save(message);

        messageRoom.updateLastMessageAt();

        messageRoomRepository.save(messageRoom);

        MessageResponseDto dto = MessageResponseDto.of(savedMessage);

        redisPublisher.publish(ChannelTopic.of("messageRoom" + messageRoom.getId()), dto);

        return dto;
    }

    /* 특정 멤버 새로운 메시지 방 생성 */
    public MessageRoomResponseDto createMessageRoom() {
        Long memberId = extractMemberIdFromJwt();

        Member member = validateUserRole(memberId);

        try {
            MessageRoom messageRoom = saveMessageRoom();
            addMemberToMessageRoom(messageRoom, member);
            sendTopic(messageRoom.getId());

            return MessageRoomResponseDto.of(messageRoom);

        } catch (MessageException error) {

            throw new MessageException(ErrorCode.MESSAGE_FAILED);
        }
    }

    /* 특정 멤버 방 목록 조회 */
    public Page<MessageRoomResponseDto> getMessageRooms(Pageable pageable) {

        Long memberId = extractMemberIdFromJwt();
        Page<MessageMember> userMessageRooms = messageMemberRepository.findQueryMessageRoom(memberId, pageable);

        List<MessageMember> messageMembers = userMessageRooms.getContent();

        List<Long> messageRoomIds = messageMembers.stream()
                .map(messageMember -> messageMember.getMessageRoom().getId())
                .collect(Collectors.toList());

        List<MessageRoom> messageRooms = messageRoomRepository.findAllById(messageRoomIds);

        List<MessageRoomResponseDto> messageRoomResponseDtoList = messageRooms.stream()
                .map(MessageRoomResponseDto::of)
                .collect(Collectors.toList());

        return new PageImpl<>(
                messageRoomResponseDtoList,
                pageable,
                userMessageRooms.getTotalElements()
        );
    }

    /* 메시지 방 삭제, 만약 참여자 모두 삭제한 경우는 메시지랑 방 전부 삭제 */
    public void deleteMessageRoom(Long messageRoomId) {
        Long memberId = extractMemberIdFromJwt();

        MessageRoom messageRoom = verifiedMessageRoom(messageRoomId);

        List<MessageMember> messageMembers = messageMemberRepository.findByMessageRoomIdAndMemberId(messageRoomId, memberId);

        if (!messageMembers.isEmpty()) {
            messageMemberRepository.deleteAll(messageMembers);
        } else {
            throw new MessageException(ErrorCode.MEMBER_NOT_FOUND_IN_ROOM);
        }

        List<MessageMember> remainingMembers = messageMemberRepository.findByMessageRoomId(messageRoomId);

        if (remainingMembers.isEmpty()) {
            messageRepository.deleteAllById(messageRoom.getMessages().stream()
                    .map(Message::getId)
                    .collect(Collectors.toList()));

            messageRoomRepository.deleteById(messageRoomId);
        }
    }

    /* 특정 메시지 방 정보 조회 */
    public MessageRoomResponseDto getMessageRoom(Long messageRoomId) {
        Long memberId = extractMemberIdFromJwt();

        MessageRoom findMessageRoom = verifiedMessageRoom(messageRoomId);

        List<Message> messages = new ArrayList<>(findMessageRoom.getMessages());

        boolean memberExistsInMessages = messages.stream()
                .anyMatch(message -> message.getMember().getId().equals(memberId));

        if (!memberExistsInMessages) {
            throw new MessageException(ErrorCode.MEMBER_NOT_FOUND_IN_ROOM);
        }

        MessageRoomResponseDto messageRoomResponseDto = MessageRoomResponseDto.builder()
                .messageRoomId(findMessageRoom.getId())
                .createAt(findMessageRoom.getCreatedAt())
                .lastMessageAt(findMessageRoom.getLastMessageAt())
                .messages(messages.stream()
                        .map(MessageResponseDto::of)
                        .collect(Collectors.toList()))
                .build();

        sendTopic(findMessageRoom.getId());

        return messageRoomResponseDto;
    }

    /* 특정 메시지 방 -> 메시지 전송 */
    public void sendRequestMessage(Long messageRoomId, String messageContent) {
        Long memberId = extractMemberIdFromJwt();

        Member member = validateUserRole(memberId);

        MessageRoom messageRoom = messageRoomRepository.findById(messageRoomId)
                .orElseThrow(() -> new MessageException(ErrorCode.MESSAGE_NOT_FOUND));

        Message sendMessage = Message.builder()
                .messageContent(messageContent)
                .member(member)
                .messageRoom(messageRoom)
                .build();

        messageRepository.save(sendMessage);

        messageRoomRepository.save(messageRoom);
    }

    //////////////////////////////////////////////////////////////////////////////////

    /* 검증 및 유틸 로직 */
    private Long extractMemberIdFromJwt() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new MessageException(ErrorCode.UNAUTHORIZED_MESSAGE_ACCESS);
        }

        String token = authHeader.substring("Bearer ".length());

        String email = jwtTokenValidator.extractUsername(token);

        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        Member member = optionalMember.orElseThrow(() -> new MessageException(ErrorCode.MEMBER_NOT_FOUND));
        Long memberId = member.getId();
        log.info("Extracted member ID: {}", memberId);
        return memberId;
    }

    private void addMemberToMessageRoom(MessageRoom messageRoom, Member member) {
        MessageMember messageMember = MessageMember.builder()
                .messageRoom(messageRoom)
                .member(member)
                .build();

        MessageMember savedMessageMember = messageMemberRepository.save(messageMember);

        // Log the state of the savedMessageMember object
        log.info("Saved MessageMember - ID: {}, Member ID: {}, Message Room ID: {}",
                savedMessageMember.getId(),
                savedMessageMember.getMember().getId(),
                savedMessageMember.getMessageRoom().getId());
    }

    public Member validateUserRole(Long memberId) {
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        optionalMember.ifPresent(member -> log.info("Found Member - ID: {}, Name: {}, Email: {}",
                member.getId(), member.getName(), member.getEmail()));

        Member member = optionalMember.orElseThrow(() -> new MessageException(ErrorCode.UNAUTHORIZED_MESSAGE_ACCESS));

        if (member.getRole() != MemberRole.USER && member.getRole() != MemberRole.ADMIN) {
            throw new MessageException(ErrorCode.UNAUTHORIZED_MESSAGE_ACCESS);
        }

        return member;
    }

    public MessageRoom verifiedMessageRoom(Long messageRoomId) {
        return messageRoomRepository.findById(messageRoomId)
                .orElseThrow(() -> new MessageException(ErrorCode.MESSAGE_NOT_FOUND));
    }

    private static Message saveMessage(MessageRequestDto mrd, MessageRoom messageRoom, Member member) {
        Message message = Message.builder()
                .messageContent(mrd.getMessageContent())
                .member(member)
                .messageRoom(messageRoom)
                .build();

        return message;
    }

    public MessageRoom saveMessageRoom() {
        MessageRoom messageRoom = MessageRoom.builder()
                .build();

        log.info("opopo");

        MessageRoom savedMessageRoom = messageRoomRepository.save(messageRoom);

        log.info("After Save - MessageRoom - ID: {}",
                savedMessageRoom.getId());

        return savedMessageRoom;
    }

    private void sendTopic(Long messageRoomId) {
        String roomId = "messageRoom" + messageRoomId;
        log.info("topics={}",topics);
        if (!topics.containsKey(roomId)) {
            log.info("메세지 토픽 만들어짐");
            ChannelTopic topic = new ChannelTopic(roomId);
            redisMessageListener.addMessageListener(redisSubscriber, topic);
            topics.put(roomId, topic);
            log.info("메세지 토픽 전송");
            log.info("topics={}",topics);
        }
    }
}
