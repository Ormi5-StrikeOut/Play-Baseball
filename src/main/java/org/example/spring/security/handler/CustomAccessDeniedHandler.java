package org.example.spring.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * 접근이 거부된 경우 처리를 담당하는 핸들러 클래스입니다.
 * AccessDeniedHandler 인터페이스를 구현하여 사용자 정의 접근 거부 처리 로직을 제공합니다.
 */
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 접근이 거부된 경우 호출되는 메서드입니다.
     * JSON 형식의 오류 응답을 생성하여 클라이언트에게 반환합니다.
     *
     * @param request 현재 HTTP 요청
     * @param response HTTP 응답 객체
     * @param accessDeniedException 발생한 접근 거부 예외
     * @throws IOException 입출력 예외 발생 시
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
        throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("User Authorities: {}", auth != null ? auth.getAuthorities() : "No Authentication found");

        // 동적 값 설정
        String currentTimeStamp = LocalDateTime.now().toString();
        String message = (accessDeniedException != null && accessDeniedException.getMessage() != null) ? accessDeniedException.getMessage() : "Authentication Failed";
        String path = request.getRequestURI();

        assert accessDeniedException != null;
        log.debug("Access Denied Error: {}", accessDeniedException.getMessage());
        log.debug("Requested URL: {}", request.getRequestURL());
        log.debug("User Principal: {}", request.getUserPrincipal());

        response.setHeader("play-baseball-denied-reason", "Authentication Failed");
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // JSON 응답 구성
        Map<String, Object> deniedResponse = new HashMap<>();
        deniedResponse.put("timestamp", currentTimeStamp);
        deniedResponse.put("status", HttpStatus.FORBIDDEN.value());
        deniedResponse.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
        deniedResponse.put("message", message);
        deniedResponse.put("path", path);

        response.getWriter().write(objectMapper.writeValueAsString(deniedResponse));

    }
}
