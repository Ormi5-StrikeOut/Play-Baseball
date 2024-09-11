package org.example.spring.security.filter;

import java.io.IOException;
import java.util.Map;

import org.example.spring.security.jwt.JwtTokenValidator;
import org.example.spring.security.service.RateLimiterService;
import org.example.spring.security.utils.AuthUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 요청 속도 제한 필터
 * <p>
 * 이 필터는 들어오는 요청에 대해 속도 제한을 적용합니다.
 * JWT 토큰, IP 주소, User-Agent를 기반으로 요청을 식별하고 제한합니다.
 * </p>
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

	private final RateLimiterService rateLimiter;
	private final JwtTokenValidator jwtTokenValidator;
	private final AuthUtils authUtils;
	private final ObjectMapper objectMapper;

	public RateLimitFilter(RateLimiterService rateLimiter, JwtTokenValidator jwtTokenValidator, AuthUtils authUtils,
		ObjectMapper objectMapper) {
		this.rateLimiter = rateLimiter;
		this.jwtTokenValidator = jwtTokenValidator;
		this.authUtils = authUtils;
		this.objectMapper = objectMapper;
	}

	/**
	 * 필터 내부 로직을 처리합니다.
	 * <p>
	 * 요청에 대한 속도 제한을 확인하고, 제한을 초과하지 않은 경우 요청을 계속 진행합니다.
	 * 제한을 초과한 경우 오류 응답을 생성합니다.
	 * </p>
	 *
	 * @param request     HTTP 요청
	 * @param response    HTTP 응답
	 * @param filterChain 필터 체인
	 * @throws ServletException 서블릿 예외
	 * @throws IOException      IO 예외
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		String token = jwtTokenValidator.extractTokenFromHeader(request);
		String ip = authUtils.getClientIpAddress(request);
		String userAgent = request.getHeader("User-Agent");

		if (rateLimiter.tryConsume(token, ip, userAgent)) {
			filterChain.doFilter(request, response);
		} else {
			createErrorResponse(response);
		}
	}

	/**
	 * 오류 응답 생성
	 * <p>
	 * 속도 제한 초과 시 클라이언트에게 전송할 JSON 형식의 오류 응답을 생성합니다.
	 * </p>
	 *
	 * @param response HTTP 응답
	 * @throws IOException IO 예외
	 */
	private void createErrorResponse(HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		Map<String, Object> errorResponse = Map.of(
			"status", HttpStatus.TOO_MANY_REQUESTS.value(),
			"error", "Too Many Requests",
			"message", "Rate limit exceeded. Please try again later."
		);

		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}