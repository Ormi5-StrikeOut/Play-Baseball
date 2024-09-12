package org.example.spring.common;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.exception.AccountBannedException;
import org.example.spring.exception.AccountDeletedException;
import org.example.spring.exception.AuthenticationFailedException;
import org.example.spring.exception.EmailAlreadyVerifiedException;
import org.example.spring.exception.EmailVerificationTokenExpiredException;
import org.example.spring.exception.InvalidCredentialsException;
import org.example.spring.exception.InvalidTokenException;
import org.example.spring.exception.MemberNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.debug("IllegalArgument: " + e);
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
        log.debug("Attempt to access deleted account: " + ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponseDto.error("이 계정은 삭제 되었습니다"));
    }

    @ExceptionHandler(AccountBannedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAccountBannedException(AccountBannedException ex) {
        log.debug("Attempt to access banned account: " + ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponseDto.error("이 계정은 밴 되었습니다"));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        log.debug("Invalid credentials: " + ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponseDto.error("Invalid email or password"));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleInvalidTokenException(InvalidTokenException ex) {
        log.debug("Invalid token: " + ex);
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

    @ExceptionHandler(EmailVerificationTokenExpiredException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleExpiredTokenException(EmailVerificationTokenExpiredException e) {
        log.debug("email verify token is expired: " + e);
        return ResponseEntity.status(HttpStatus.GONE).body(ApiResponseDto.error("이메일 인증 토큰이 만료되었습니다. 새로운 인증 이메일을 요청해주세요."));
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMemberNotFoundException(MemberNotFoundException e) {
        log.debug("Member not found: " + e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponseDto.error("회원을 찾지 못했습니다"));
    }

    @ExceptionHandler(EmailAlreadyVerifiedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleEmailAlreadyVerifiedException(EmailAlreadyVerifiedException e) {
        log.debug("is already verified email: " + e);
        return ResponseEntity.badRequest().body(ApiResponseDto.error("이미 인증된 이메일입니다."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleNoResourceFoundException(NoResourceFoundException ex) {
        if (ex.getResourcePath().contains("favicon.ico")) {
            // favicon.ico 요청에 대해 204 No Content 응답
            return ResponseEntity.noContent().build();
        }
        // 다른 리소스에 대한 처리
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponseDto.error("요청한 리소스를 찾을 수 없습니다."));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMultipartException(MultipartException ex) {
        log.error("Multipart request processing failed", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponseDto.error("파일 업로드 처리 중 오류가 발생했습니다."));
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAuthenticationFailedException(AuthenticationFailedException ex) {
        log.error("Authentication Failed: " + ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponseDto.error("인증에 실패 했습니다."));
    }
}
