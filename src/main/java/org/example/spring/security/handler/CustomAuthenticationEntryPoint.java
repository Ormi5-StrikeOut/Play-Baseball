package org.example.spring.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * 인증되지 않은 사용자의 보호된 리소스 접근 시 호출되는 진입점 클래스입니다.
 * AuthenticationEntryPoint 인터페이스를 구현하여 사용자 정의 인증 실패 처리 로직을 제공합니다.
 */
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 인증되지 않은 사용자가 보호된 리소스에 접근을 시도할 때 호출되는 메서드입니다.
     * JSON 형식의 오류 응답을 생성하여 클라이언트에게 반환합니다.
     *
     * @param request 현재 HTTP 요청
     * @param response HTTP 응답 객체
     * @param authException 발생한 인증 예외
     * @throws IOException 입출력 예외 발생 시
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
        throws IOException {
        String currentTimeStamp = Instant.now().toString();
        String message;
        String errorReason = response.getHeader("play-baseball-error-reason");

        if ("Invalid Token".equals(errorReason)) {
            message = "The provided token is invalid. Please login again.";
        } else {
            message = (authException != null && authException.getMessage() != null) ? authException.getMessage()
                : "Unauthorized";
        }

        String path = request.getRequestURI();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", currentTimeStamp);
        errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("error", HttpStatus.UNAUTHORIZED.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", path);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}