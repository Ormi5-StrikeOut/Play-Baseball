package org.example.spring.security.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.example.spring.security.jwt.JwtTokenValidator;
import org.example.spring.security.service.CookieService;
import org.example.spring.security.service.JwtAuthenticationService;
import org.example.spring.security.service.TokenBlacklistService;
import org.example.spring.wrapper.CustomHttpServletRequestWrapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 들어오는 요청에 대해 JWT 토큰의 유효성을 검사하는 필터 클래스입니다.
 */
@Slf4j
@Component
public class JwtValidatorFilter extends OncePerRequestFilter {

	private final CookieService cookieService;
	private final JwtAuthenticationService jwtAuthenticationService;
	private final TokenBlacklistService tokenBlacklistService;
	private final JwtTokenValidator jwtTokenValidator;

	private static final List<String> PUBLIC_PATHS = Arrays.asList(
		"/swagger-ui", "/v3/api-docs", "/webjars", "/api/auth/login", "/api/members/join", "/api/members/verify-email",
		"/api/members/reset-password", "/api/members/request-password-reset", "/stomp/content"
	);

	public JwtValidatorFilter(CookieService cookieService, JwtAuthenticationService jwtAuthenticationService,
		TokenBlacklistService tokenBlacklistService,
		JwtTokenValidator jwtTokenValidator) {
		this.cookieService = cookieService;
		this.jwtAuthenticationService = jwtAuthenticationService;
		this.tokenBlacklistService = tokenBlacklistService;
		this.jwtTokenValidator = jwtTokenValidator;
	}

	/**
	 * 들어오는 요청을 필터링하여 JWT 토큰의 유효성을 검사하고 인증을 설정합니다.
	 *
	 * @param request     HTTP 요청
	 * @param response    HTTP 응답
	 * @param filterChain 필터 체인
	 * @throws IOException I/O 예외 발생 시
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws IOException {
		String requestURI = request.getRequestURI();
		log.debug("Processing request: {}", requestURI);
		try {
			String accessToken = jwtTokenValidator.extractTokenFromHeader(request);
			String refreshToken = cookieService.extractTokenFromCookie(request, "refresh_token");

			log.debug("Initially extracted tokens - Access: {}, Refresh: {}",
				accessToken != null ? accessToken.substring(0, Math.min(accessToken.length(), 20)) + "..." :
					"Not present",
				refreshToken != null ? refreshToken.substring(0, Math.min(refreshToken.length(), 20)) + "..." :
					"Not present");

			Authentication authentication = null;
			boolean tokenRefreshed = false;

			if (accessToken != null && !tokenBlacklistService.isTokenBlacklisted(accessToken)) {
				try {
					authentication = jwtAuthenticationService.authenticateWithAccessToken(accessToken, request);
					log.debug("Authentication successful with access token");
				} catch (ExpiredJwtException e) {
					log.debug("Access token expired, attempting to use refresh token");
					accessToken = null;  // 만료된 토큰을 null로 설정
				}
			}

			if (authentication == null && refreshToken != null) {
				try {
					authentication = jwtAuthenticationService.refreshAccessToken(refreshToken, request, response);
					accessToken = jwtTokenValidator.extractTokenFromResponseHeader(response);
					tokenRefreshed = true;
					log.debug("Token refreshed successfully. New access token: {}",
						accessToken != null ? accessToken.substring(0, Math.min(accessToken.length(), 20)) + "..." :
							"Not present");
				} catch (Exception e) {
					log.error("Failed to refresh token: {}", e.getMessage());
				}
			}

			if (authentication != null) {
				SecurityContextHolder.getContext().setAuthentication(authentication);
				if (tokenRefreshed) {
					log.debug("Updating request with new access token");
					request = new CustomHttpServletRequestWrapper(request);
					((CustomHttpServletRequestWrapper)request).putHeader("Authorization", "Bearer " + accessToken);
				}
				filterChain.doFilter(request, response);
			} else {
				log.warn("Authentication failed for request: {}", requestURI);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("Authentication failed. Please log in again.");
			}
		} catch (Exception e) {
			log.error("Authentication error: {}", e.getMessage());
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("Authentication failed: " + e.getMessage());
		}
	}

	/**
	 * 이 필터를 적용하지 않아야 하는 요청인지 결정합니다.
	 *
	 * @param request HTTP 요청
	 * @return 필터를 적용하지 않아야 하면 true, 그렇지 않으면 false
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getServletPath();
		String method = request.getMethod();
		boolean shouldSkip = PUBLIC_PATHS.stream().anyMatch(path::startsWith)
			|| path.equals("/")
			|| ("/api/exchanges".equals(path) && "GET".equalsIgnoreCase(method))
			|| ("/api/reviews".equals(path) && "GET".equalsIgnoreCase(method))
			|| path.equals("/favicon.ico");
		log.debug("Should skip filter for path {}: {}", path, shouldSkip);
		return shouldSkip;
	}
}