package org.example.spring.security.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;
import java.util.List;
import org.example.spring.domain.member.MemberRole;
import org.example.spring.security.filter.JwtValidatorFilter;
import org.example.spring.security.filter.MemberStatusCheckFilter;
import org.example.spring.security.filter.RateLimitFilter;
import org.example.spring.security.handler.CustomAccessDeniedHandler;
import org.example.spring.security.handler.CustomAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@Profile("dev")
public class SecurityDevConfig {

    private final JwtValidatorFilter jwtValidatorFilter;
    private final RateLimitFilter rateLimitFilter;
    private final MemberStatusCheckFilter memberStatusCheckFilter;

    public SecurityDevConfig(JwtValidatorFilter jwtValidatorFilter, RateLimitFilter rateLimitFilter, MemberStatusCheckFilter memberStatusCheckFilter) {
        this.jwtValidatorFilter = jwtValidatorFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.memberStatusCheckFilter = memberStatusCheckFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(corsConfig -> corsConfig.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtValidatorFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(memberStatusCheckFilter, JwtValidatorFilter.class)
            .authorizeHttpRequests(request -> request
                // 비회원 공개 엔드포인트
                .requestMatchers("/", "/api/auth/login", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs/swagger-config", "/favicon.ico").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/members/join").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/members/verify-email").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/exchanges", "/api/exchanges/five").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reviews").permitAll()

                // 사용자 및 관리자 엔드포인트 (이메일 인증 필요)
                .requestMatchers(HttpMethod.PUT, "/api/members/**").access(hasRolesAndEmailVerified(MemberRole.USER, MemberRole.ADMIN))
                .requestMatchers(HttpMethod.POST, "/api/exchanges", "/api/reviews").access(hasRolesAndEmailVerified(MemberRole.USER, MemberRole.ADMIN))
                .requestMatchers(HttpMethod.PUT, "/api/exchanges/**", "/api/reviews/**").access(hasRolesAndEmailVerified(MemberRole.USER, MemberRole.ADMIN))
                .requestMatchers("/api/messages").access(hasRolesAndEmailVerified(MemberRole.USER, MemberRole.ADMIN))
                .requestMatchers("/api/messages/**").access(hasRolesAndEmailVerified(MemberRole.USER, MemberRole.ADMIN))

                // 관리자 전용 엔드포인트 (이메일 인증 필요)
                .requestMatchers(HttpMethod.GET, "/api/members").access(hasRoleAndEmailVerified(MemberRole.ADMIN))
                .requestMatchers(HttpMethod.PUT, "/api/members/verify-role/**").access(hasRoleAndEmailVerified(MemberRole.ADMIN))

                // 인증된 사용자 엔드포인트 (이메일 인증 불필요)
                .requestMatchers("/api/auth/logout", "/api/members/my/**", "/api/members/my", "/api/members/resend-verification-email").authenticated()
                .requestMatchers("/api/exchange-likes/**").authenticated()

                // 기타 모든 요청
                .anyRequest().authenticated()
            );

        http.exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer
            .accessDeniedHandler(new CustomAccessDeniedHandler())
            .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
        );
        http.headers(headersConfig -> headersConfig
            .xssProtection(HeadersConfigurer.XXssConfig::disable)
            .contentSecurityPolicy(csp -> csp
                .policyDirectives("default-src 'self'; " +
                    "img-src 'self' data:; " +
                    "script-src 'self' 'unsafe-inline'; " +
                    "style-src 'self' 'unsafe-inline'; " +
                    "connect-src 'self' http://localhost:8080")
            )
            .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            .contentTypeOptions(withDefaults())
            .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .preload(true)
                .maxAgeInSeconds(31536000)
            )
        );

        return http.build();
    }

    private AuthorizationManager<RequestAuthorizationContext> hasRolesAndEmailVerified(MemberRole... roles) {
        return (authentication, context) -> {
            if (authentication == null) {
                return new AuthorizationDecision(false);
            }

            boolean hasRequiredRole = false;
            for (MemberRole role : roles) {
                if (authentication.get().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + role.name()))) {
                    hasRequiredRole = true;
                    break;
                }
            }

            boolean isEmailVerified = authentication.get().getAuthorities().contains(new SimpleGrantedAuthority("EMAIL_VERIFIED"));

            return new AuthorizationDecision(hasRequiredRole && isEmailVerified);
        };
    }

    private AuthorizationManager<RequestAuthorizationContext> hasRoleAndEmailVerified(MemberRole role) {
        return (authentication, context) -> {
            if (authentication == null) {
                return new AuthorizationDecision(false);
            }

            boolean hasRequiredRole = authentication.get().getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + role.name()));
            boolean isEmailVerified = authentication.get().getAuthorities().contains(new SimpleGrantedAuthority("EMAIL_VERIFIED"));

            return new AuthorizationDecision(hasRequiredRole && isEmailVerified);
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
            .role(MemberRole.ADMIN.name()).implies(MemberRole.USER.name())
            .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
