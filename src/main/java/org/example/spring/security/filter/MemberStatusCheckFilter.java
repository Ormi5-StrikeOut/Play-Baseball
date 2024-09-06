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

        // 로그인 요청의 경우 별도 처리
        if ("/api/auth/login".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = jwtTokenValidator.extractTokenFromHeader(request);
        if (token != null) {
            try {
                if (jwtTokenValidator.validateToken(token)) {
                    Member member = jwtTokenValidator.getMemberFromToken(token);

                    if (member.getDeletedAt() != null) {
                        handleDeletedUser(response);
                        return;
                    }

                    if (member.getRole() == MemberRole.BANNED) {
                        handleBannedUser(request, response, filterChain, member);
                        return;
                    }
                }
            } catch (AccountDeletedException e) {
                handleDeletedUser(response);
                return;
            } catch (Exception e) {
                log.error("Could not set user authentication in security context", e);
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void handleBannedUser(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, Member member)
        throws IOException, ServletException {
        // BANNED 사용자에게 허용된 엔드포인트 목록
        List<String> allowedEndpoints = Arrays.asList("/api/support", "/api/account/status");

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
}

