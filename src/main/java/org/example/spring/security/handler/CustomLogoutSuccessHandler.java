package org.example.spring.security.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.example.spring.security.jwt.JwtUtils;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final JwtUtils jwtUtils;
    private final JwtTokenValidator jwtTokenValidator;

    public CustomLogoutSuccessHandler(JwtUtils jwtUtils, JwtTokenValidator jwtTokenValidator) {
        this.jwtUtils = jwtUtils;
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            // 리프레시 토큰 쿠키 삭제
            removeRefreshTokenCookie(response);

            // 액세스 토큰 무효화 (블랙리스트에 추가)
            invalidateAccessToken(request);

            // 응답 설정
            sendLogoutSuccessResponse(response);
        } catch (IOException e) {
            // IOException 처리
            log.error("Failed to send logout success response", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("An error occurred during logout", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void removeRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS를 사용하는 경우
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private void invalidateAccessToken(HttpServletRequest request) {
        String accessToken = jwtUtils.extractTokenFromHeader(request);
        if (accessToken != null) {
            jwtTokenValidator.addToBlacklist(accessToken);
        }
    }

    private void sendLogoutSuccessResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"Logout successful\"}");
        response.getWriter().flush();
    }
}
