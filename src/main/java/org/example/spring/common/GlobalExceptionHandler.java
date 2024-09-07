package org.example.spring.common;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.exception.AccountBannedException;
import org.example.spring.exception.AccountDeletedException;
import org.example.spring.exception.InvalidCredentialsException;
import org.example.spring.exception.InvalidTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponseDto.error("요청에 실패했습니다: " + e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleException(Exception e) {
        log.debug("서버 오류 발생: " + e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponseDto.error("서버 오류가 발생했습니다."));
    }

    @ExceptionHandler(AccountDeletedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAccountDeletedException(AccountDeletedException ex) {
        log.warn("Attempt to access deleted account: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponseDto.error("이 계정은 삭제 되었습니다"));
    }

    @ExceptionHandler(AccountBannedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAccountBannedException(AccountBannedException ex) {
        log.warn("Attempt to access banned account: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponseDto.error("이 계정은 밴 되었습니다"));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        log.warn("Invalid credentials: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponseDto.error("Invalid email or password"));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("Invalid token: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponseDto.error("토큰이 유효하지 않습니다 " + ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String errorMessage = "잘못된 요청 데이터가 전달되었습니다.";
        if (ex.getCause() instanceof InvalidFormatException cause) {
            if (cause.getTargetType() != null && cause.getTargetType().isEnum()) {
                errorMessage = String.format("'%s' 필드에 잘못된 값이 전달되었습니다. '%s' 중 하나의 값을 선택해주세요.",
                    cause.getPath().getFirst().getFieldName(),
                    Arrays.asList(cause.getTargetType().getEnumConstants()));
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponseDto.error(errorMessage));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("access denied: {}", ex.getMessage());
        String errorMessage = "'" + request.getRequestURI() + "' 경로에 대한 접근 권한이 없습니다.";
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponseDto.error(errorMessage));
    }
}
