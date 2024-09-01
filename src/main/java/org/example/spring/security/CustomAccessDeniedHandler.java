package org.example.spring.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
        throws IOException, ServletException {
        // 동적 값 설정
        String currentTimeStamp = LocalDateTime.now().toString();
        String message = (accessDeniedException != null && accessDeniedException.getMessage() != null) ? accessDeniedException.getMessage() : "Authentication Failed";
        String path = request.getRequestURI();
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
