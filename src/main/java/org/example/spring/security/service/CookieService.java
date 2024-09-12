package org.example.spring.security.service;

import org.example.spring.security.jwt.JwtTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 쿠키 관련 작업을 처리하는 서비스 클래스입니다.
 * JWT 토큰을 쿠키에 저장하고 추출하는 기능을 제공합니다.
 */
@Service
public class CookieService {

	private final JwtTokenProvider jwtTokenProvider;

	public CookieService(JwtTokenProvider jwtTokenProvider) {
		this.jwtTokenProvider = jwtTokenProvider;
	}

	/**
	 * HTTP 요청에서 특정 이름의 쿠키에서 토큰을 추출합니다.
	 *
	 * @param request    HTTP 요청
	 * @param cookieName 토큰이 저장된 쿠키의 이름
	 * @return 토큰 값, 찾지 못한 경우 null
	 */
	public String extractTokenFromCookie(HttpServletRequest request, String cookieName) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(cookieName)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * HTTP 응답에 리프레시 토큰을 쿠키로 추가합니다.
	 *
	 * @param response HTTP 응답
	 * @param token    쿠키로 추가할 리프레시 토큰
	 */
	public void addRefreshTokenCookie(HttpServletResponse response, String token) {
		ResponseCookie cookie = ResponseCookie.from("refresh_token", token)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.maxAge(jwtTokenProvider.getRefreshTokenExpiration())
			.sameSite("Lax")
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	/**
	 * HTTP 응답에 리프레시 토큰을 제거합니다
	 *
	 * @param response HttpServletResponse
	 */
	public void removeRefreshTokenCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("refresh_token", null);
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}
}
