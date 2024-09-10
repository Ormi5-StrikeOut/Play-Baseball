package org.example.spring.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.MemberRole;
import org.example.spring.exception.AccountDeletedException;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 회원 상태를 확인하는 필터
 * <p>
 * 이 필터는 요청의 인증 토큰을 검증하고, 회원의 상태(삭제됨, 차단됨, 이메일 미인증 등)에 따라
 * 적절한 응답을 처리합니다. 또한 공개 엔드포인트와 관리자 요청을 특별히 처리합니다.
 * </p>
 */
@Slf4j
@Component
public class MemberStatusCheckFilter extends OncePerRequestFilter {

	private final JwtTokenValidator jwtTokenValidator;

	public MemberStatusCheckFilter(JwtTokenValidator jwtTokenValidator) {
		this.jwtTokenValidator = jwtTokenValidator;
	}

	/**
	 * 필터 내부 로직을 처리합니다.
	 * <p>
	 * 요청 경로와 메소드를 확인하여 공개 엔드포인트인지 검사하고,
	 * 회원의 상태에 따라 적절한 처리를 수행합니다.
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

		String path = request.getRequestURI();
		String method = request.getMethod();
		log.debug("Processing request: {} {}", method, path);

		// 공개 엔드포인트 처리
		if (isPublicEndpoint(path, method) || response.isCommitted()) {
			filterChain.doFilter(request, response);
			return;
		}
		String token = jwtTokenValidator.extractTokenFromHeader(request);
		Member member = jwtTokenValidator.getMemberFromToken(token);

		if (member.getRole() == MemberRole.ADMIN) {
			filterChain.doFilter(request, response);
			log.debug("Admin pass this filter");
			return;
		}

		if (token != null) {
			try {
				if (jwtTokenValidator.validateToken(token)) {
					log.debug("Member: {}, Role: {}, EmailVerified: {}", member.getEmail(), member.getRole(),
						member.isEmailVerified());

					if (member.getDeletedAt() != null) {
						handleDeletedUser(response);
						return;
					}

					if (member.getRole() == MemberRole.BANNED) {
						handleBannedUser(request, response, filterChain, member);
						return;
					}

					if (!member.isEmailVerified() && isEmailVerificationRequired(path, method)) {
						// 이메일 인증이 필요한 엔드포인트 목록
						List<String> emailVerificationRequiredEndpoints = Arrays.asList(
							"/api/exchanges", "/api/reviews", "/api/messages"
						);

						if (emailVerificationRequiredEndpoints.stream().anyMatch(path::startsWith)) {
							handleUnverifiedEmail(response);
							return;
						}
					}
					log.debug("Member passed all checks in MemberStatusCheckFilter");
				}
			} catch (AccountDeletedException e) {
				handleDeletedUser(response);
				return;
			} catch (Exception e) {
				log.error("Could not set user authentication in security context", e);
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
		}
		filterChain.doFilter(request, response);
	}

	/**
	 * 차단된 사용자 처리
	 * <p>
	 * 차단된 사용자의 요청을 처리합니다. 특정 엔드포인트에 대해서만 제한적인 접근을 허용합니다.
	 * </p>
	 *
	 * @param request     HTTP 요청
	 * @param response    HTTP 응답
	 * @param filterChain 필터 체인
	 * @param member      회원 정보
	 * @throws IOException      IO 예외
	 * @throws ServletException 서블릿 예외
	 */
	private void handleBannedUser(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain,
		Member member)
		throws IOException, ServletException {
		// BANNED 사용자에게 허용된 엔드포인트 목록
		List<String> allowedEndpoints = Arrays.asList("/api/auth/logout", "/api/members/my/**", "/api/members/my");

		if (allowedEndpoints.contains(request.getRequestURI())) {
			// 허용된 엔드포인트에 대해서는 제한된 권한으로 인증 처리
			UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(member, null, Collections.emptyList());
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(request, response);
		} else {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.getWriter().write("Your account is banned. Access is limited.");
		}
	}

	/**
	 * 삭제된 사용자 처리
	 * <p>
	 * 삭제된 계정에 대한 접근을 거부하고 적절한 메시지를 응답합니다.
	 * </p>
	 *
	 * @param response HTTP 응답
	 * @throws IOException IO 예외
	 */
	private void handleDeletedUser(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.getWriter().write("This account has been deleted.");
	}

	/**
	 * 이메일 미인증 사용자 처리
	 * <p>
	 * 이메일 인증이 필요한 리소스에 대한 접근을 거부하고 적절한 메시지를 응답합니다.
	 * </p>
	 *
	 * @param response HTTP 응답
	 * @throws IOException IO 예외
	 */
	private void handleUnverifiedEmail(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.getWriter().write("Email verification required to access this resource.");
	}

	/**
	 * 공개 엔드포인트 확인
	 * <p>
	 * 주어진 경로와 메소드가 공개적으로 접근 가능한 엔드포인트인지 확인합니다.
	 * </p>
	 *
	 * @param path   요청 경로
	 * @param method 요청 메소드
	 * @return 공개 엔드포인트인 경우 true, 그렇지 않으면 false
	 */
	private boolean isPublicEndpoint(String path, String method) {
		return "/api/auth/login".equals(path)
			|| "/favicon.ico".equals(path)
			|| "/".equals(path)
			|| "/api/members/join".equals(path)
			|| path.startsWith("/api/members/verify-email")
			|| path.startsWith("/api/members/resend-verification-email")
			|| path.startsWith("/api/members/reset-password")
			|| path.startsWith("/api/members/request-password-reset")
			|| ("/api/exchanges".startsWith(path) && "GET".equalsIgnoreCase(method))
			|| ("/api/reviews".startsWith(path) && "GET".equalsIgnoreCase(method))
			|| path.startsWith("/api/exchanges/five");
	}

	/**
	 * 이메일 인증 필요 여부 확인
	 * <p>
	 * 주어진 경로와 메소드에 대해 이메일 인증이 필요한지 확인합니다.
	 * </p>
	 *
	 * @param path   요청 경로
	 * @param method 요청 메소드
	 * @return 이메일 인증이 필요한 경우 true, 그렇지 않으면 false
	 */
	private boolean isEmailVerificationRequired(String path, String method) {
		return (path.startsWith("/api/exchanges") && !"GET".equalsIgnoreCase(method))
			|| (path.startsWith("/api/reviews") && !"GET".equalsIgnoreCase(method))
			|| path.startsWith("/api/messages");
	}
}

