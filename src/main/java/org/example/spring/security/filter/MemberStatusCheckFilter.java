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

@Slf4j
@Component
public class MemberStatusCheckFilter extends OncePerRequestFilter {

    private final JwtTokenValidator jwtTokenValidator;

    public MemberStatusCheckFilter(JwtTokenValidator jwtTokenValidator) {
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        log.debug("Processing request: {} {}", method, path);

        // 공개 엔드포인트 처리
        if (isPublicEndpoint(path, method)|| response.isCommitted()) {
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
                    log.debug("Member: {}, Role: {}, EmailVerified: {}", member.getEmail(), member.getRole(), member.isEmailVerified());

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

    private void handleBannedUser(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, Member member)
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

    private void handleDeletedUser(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("This account has been deleted.");
    }

    private void handleUnverifiedEmail(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("Email verification required to access this resource.");
    }

    private boolean isPublicEndpoint(String path, String method) {
        return "/api/auth/login".equals(path)
            || "/favicon.ico".equals(path)
            || "/".equals(path)
            || "/api/members/join".equals(path)
            || path.startsWith("/api/members/verify-email")
            || ("/api/exchanges".equals(path) && "GET".equalsIgnoreCase(method))
            || ("/api/reviews".equals(path) && "GET".equalsIgnoreCase(method))
            || path.startsWith("/api/exchanges/five")
            || path.startsWith("/stomp/content");
    }

    private boolean isEmailVerificationRequired(String path, String method) {
        return (path.startsWith("/api/exchanges") && !"GET".equalsIgnoreCase(method))
            || (path.startsWith("/api/reviews") && !"GET".equalsIgnoreCase(method))
            || path.startsWith("/api/messages");
    }
}

