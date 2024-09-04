package org.example.spring.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.domain.message.messageDto.*;
import org.example.spring.service.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ResponseDto> postMessage(@RequestBody @Validated MessageRequestDto messageRequestDto) {
        ResponseDto response = ResponseDto.builder()
                .data(messageService.createMessage(messageRequestDto))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @MessageMapping("/chats/{roomId}")
    public ResponseEntity sendMessage(@DestinationVariable Long roomId, @RequestBody @Validated MessageRequestDto messageRequestDto) {
        ResponseDto response =
                ResponseDto.of(messageService.createMessage(messageRequestDto));
        return new ResponseEntity<>(response ,HttpStatus.CREATED);
    }

    @PostMapping("/{memberId}")
    public ResponseEntity<ResponseDto> createMessageRoom(@PathVariable("memberId") Long memberId) {
        MessageRoomResponseDto messageRoomResponseDto = messageService.createMessageRoom(memberId);
        return new ResponseEntity<>(
                ResponseDto.of(messageRoomResponseDto),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<PageResponseDto> getMessageRooms(
            @PathVariable("memberId") Long memberId,
            @PageableDefault(page = 0, size = 10, sort = "lastMessageAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<MessageRoomResponseDto> messageRooms = messageService.getMessageRooms(memberId, pageable);
        PageResponseDto response = PageResponseDto.of(messageRooms.getContent(), messageRooms);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/rooms/{memberId}/{messageRoomId}")
    public ResponseEntity getMessages(
            @PathVariable ("memberId") @Positive Long memberId,
            @PathVariable ("messageRoomId") @Positive Long messageRoomId
    ) {
        ResponseDto response =
                ResponseDto.of(messageService.getMessageRoom(messageRoomId, memberId));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(
            @RequestParam("memberId") Long memberId,
            @RequestParam("messageRoomId") Long messageRoomId,
            @RequestParam("messageContent") String messageContent
    ) {
        messageService.sendRequestMessage(memberId, messageRoomId, messageContent);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping("/rooms/{messageRoomId}/{memberId}")
    public ResponseEntity deleteMessageRoom(
            @PathVariable("messageRoomId") Long messageRoomId,
            @PathVariable("memberId") Long memberId
    ) {
        messageService.deleteMessageRoom(messageRoomId, memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
