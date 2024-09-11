package org.example.spring.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Messages", description = "Messages 관리")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @Operation(
            summary = "새로운 메시지 생성",
            description = "새로운 메시지를 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "메시지 생성 성공", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping
    public ResponseEntity<ResponseDto> postMessage(@RequestBody @Validated MessageRequestDto messageRequestDto) {
        ResponseDto response = ResponseDto.builder()
                .data(messageService.createMessage(messageRequestDto))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "채팅방에서 메시지 전송",
            description = "특정 채팅방에 메시지를 전송합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "메시지 전송 성공", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @MessageMapping("/chats/{roomId}")
    public ResponseEntity sendMessage(@DestinationVariable Long roomId, @RequestBody @Validated MessageRequestDto messageRequestDto) {
        ResponseDto response =
                ResponseDto.of(messageService.createMessage(messageRequestDto));
        return new ResponseEntity<>(response ,HttpStatus.CREATED);
    }

    @Operation(
            summary = "새 메시지 방 생성",
            description = "특정 회원을 위한 새로운 메시지 방을 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "메시지 방 생성 성공", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "회원이 존재하지 않음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/room")
    public ResponseEntity<ResponseDto> createMessageRoom() {
        MessageRoomResponseDto messageRoomResponseDto = messageService.createMessageRoom();
        return new ResponseEntity<>(
                ResponseDto.of(messageRoomResponseDto),
                HttpStatus.CREATED
        );
    }

    @Operation(
            summary = "회원의 메시지 방 목록 조회",
            description = "특정 회원의 모든 메시지 방을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "메시지 방 목록 조회 성공", content = @Content(schema = @Schema(implementation = PageResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "회원이 존재하지 않음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )

    @GetMapping("/member")
    public ResponseEntity<PageResponseDto> getMessageRooms(
            @PageableDefault(page = 0, size = 10, sort = "lastMessageAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<MessageRoomResponseDto> messageRooms = messageService.getMessageRooms(pageable);
        PageResponseDto response = PageResponseDto.of(messageRooms.getContent(), messageRooms);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "메시지 방의 메시지 조회",
            description = "특정 메시지 방에서 메시지를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "메시지 조회 성공", content = @Content(schema = @Schema(implementation = ResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "회원 또는 메시지 방이 존재하지 않음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("/rooms/member/{messageRoomId}")
    public ResponseEntity getMessages(
            @PathVariable ("messageRoomId") @Positive Long messageRoomId
    ) {
        ResponseDto response =
                ResponseDto.of(messageService.getMessageRoom(messageRoomId));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "메시지 전송",
            description = "특정 메시지 방에 메시지를 전송합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "메시지 전송 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(
            @RequestParam("messageRoomId") Long messageRoomId,
            @RequestParam("messageContent") String messageContent
    ) {
        messageService.sendRequestMessage(messageRoomId, messageContent);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Operation(
            summary = "메시지 방 삭제",
            description = "특정 메시지 방을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "메시지 방 삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "메시지 방 또는 회원이 존재하지 않음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @DeleteMapping("/room/{messageRoomId}")
    public ResponseEntity deleteMessageRoom(
            @PathVariable("messageRoomId") Long messageRoomId
    ) {
        messageService.deleteMessageRoom(messageRoomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
