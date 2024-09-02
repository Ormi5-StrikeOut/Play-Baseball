package org.example.spring.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * API 응답을 위한 공통 DTO 클래스입니다.
 *
 * @param <T> 응답 데이터의 타입
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class ApiResponseDto<T> {

    /**
     * API 응답 메세지.
     * 주로 요청 처리 결과에 대한 설명이나 오류 메세지를 포함합니다.
     */
    private String message;

    /**
     * API 응답 데이터
     * 요청에 대한 실제 데이터를 포함합니다. 데이터의 타입은 제네릭 파라미터에 의해 결정됩니다.
     */
    private T data;

    /**
     * 성공 응답을 생성합니다.
     *
     * @param <T> 응답 데이터의 데이터 타입
     * @param message 성공 메세지
     * @param data 응답 데이터
     * @return 성공 응답 DTO
     */
    public static <T> ApiResponseDto<T> success(String message, T data) {
        return new ApiResponseDto<>(message, data);
    }

    /**
     * 오류 응답을 생성합니다.
     *
     * @param message 오류 메세지
     * @return 오류 응답 DTO
     */
    public static ApiResponseDto<Void> error(String message) {
        return new ApiResponseDto<>(message, null);
    }
}
