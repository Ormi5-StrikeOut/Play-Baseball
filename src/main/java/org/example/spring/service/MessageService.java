package org.example.spring.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.constant.ErrorCode;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.MemberRole;
import org.example.spring.domain.message.Message;
import org.example.spring.domain.message.MessageMember;
import org.example.spring.domain.message.MessageRoom;
import org.example.spring.domain.message.messageDto.MessageMemberResponseDto;
import org.example.spring.domain.message.messageDto.MessageRequestDto;
import org.example.spring.domain.message.messageDto.MessageResponseDto;
import org.example.spring.domain.message.messageDto.MessageRoomResponseDto;
import org.example.spring.exception.MessageException;
import org.example.spring.redis.RedisPublisher;
import org.example.spring.redis.RedisSubscriber;
import org.example.spring.repository.message.MessageMemberRepository;
import org.example.spring.repository.message.MessageRepository;
import org.example.spring.repository.message.MessageRoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;

    private final MessageRoomRepository messageRoomRepository;

    private final MessageMemberRepository messageMemberRepository;

    private final MemberService memberService;

    private final RedisPublisher redisPublisher;

    private final Map<String, ChannelTopic> topics;

    private final RedisMessageListenerContainer redisMessageListener;

    private final RedisSubscriber redisSubscriber;

    public MessageResponseDto createMessage(MessageRequestDto mrd) {
        MessageRoom messageRoom = verifiedMessageRoom(mrd.getMessageRoomId());
        Member user = memberService.findById(mrd.getMemberId());
        Message message = saveMessage(mrd, messageRoom, user);
        MessageResponseDto dto = MessageResponseDto.of(messageRepository.save(message));
        redisPublisher.publish(ChannelTopic.of("messageRoom" + messageRoom.getMessageRoomId()), dto);
        return dto;
    }

    public MessageRoomResponseDto createMessageRoom(Long memberId) {
        Member member = memberService.findById(memberId);

        if (member.getRole() != MemberRole.USER) {
            throw new MessageException(ErrorCode.UNAUTHORIZED_MESSAGE_ACCESS); // 403 Forbidden 에러
        }

        try {
            MessageRoom messageRoom = saveMessageRoom();
            sendTopic(messageRoom.getMessageRoomId());
            return MessageRoomResponseDto.of(messageRoom);

        } catch (MessageException error) {
            throw new MessageException(ErrorCode.MESSAGE_FAILED);
        }
    }

    public Page<MessageRoomResponseDto> getMessageRooms(Long memberId, Pageable pageable) {
        Page<MessageMember> userMessageRooms = messageMemberRepository.findQueryMessageRoom(memberId, pageable);

        List<MessageMember> messageMembers = userMessageRooms.getContent();

        List<Long> messageRoomIds = messageMembers.stream()
                .map(messageMember -> messageMember.getMessageRoom().getMessageRoomId())
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



    /* 검증 및 유틸 로직 */

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

    private MessageRoom saveMessageRoom() {
        MessageRoom messageRoom = MessageRoom.builder()
                .build();
        return messageRoomRepository.save(messageRoom);
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
