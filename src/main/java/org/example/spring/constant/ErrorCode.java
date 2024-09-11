package org.example.spring.constant;

import lombok.Getter;

@Getter
public enum ErrorCode {
    MESSAGE_ROOM_EXISTS(409, "메시지 방이 이미 존재합니다."),
    MESSAGE_NOT_FOUND(404, "메시지를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(404, "회원을 찾을 수 없습니다."),
    MESSAGE_CONTENT_EMPTY(400, "메시지 내용이 비어 있습니다."),
    MEMBER_NOT_FOUND_IN_ROOM(404, "메시지 방에서 멤버를 찾을 수 없습니다."),
    UNAUTHORIZED_MESSAGE_ACCESS(403, "메시지 접근 권한이 없습니다."),
    MESSAGE_FAILED(500, "서버 오류");

    @Getter
    private int status;
    @Getter
    private final String message;

    ErrorCode(int code, String message) {
        this.status = code;
        this.message = message;
    }
}
