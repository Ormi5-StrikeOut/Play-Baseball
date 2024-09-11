package org.example.spring.security.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@Profile("dev")
@Slf4j
public class SecurityDevConfig {

    private final JwtValidatorFilter jwtValidatorFilter;
    private final RateLimitFilter rateLimitFilter;
    private final MemberStatusCheckFilter memberStatusCheckFilter;

    public SecurityDevConfig(JwtValidatorFilter jwtValidatorFilter, RateLimitFilter rateLimitFilter,
        MemberStatusCheckFilter memberStatusCheckFilter) {
        this.jwtValidatorFilter = jwtValidatorFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.memberStatusCheckFilter = memberStatusCheckFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(corsConfig -> corsConfig.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/stomp/content/**")
                .ignoringRequestMatchers("/api/auth/login")
                .ignoringRequestMatchers("/api/members/join")
            )
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtValidatorFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(memberStatusCheckFilter, JwtValidatorFilter.class)
            .authorizeHttpRequests(request -> request
                // 비회원 공개 엔드포인트
                .requestMatchers("/", "/api/auth/login", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs/swagger-config", "/favicon.ico")
                .permitAll()
                .requestMatchers("/api/members/reset-password", "/api/members/request-password-reset").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/members/join").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/messages/**").hasAnyAuthority(MemberRole.USER.name(), MemberRole.ADMIN.name())
                .requestMatchers(HttpMethod.GET, "/api/messages/**").hasAnyAuthority(MemberRole.USER.name(), MemberRole.ADMIN.name())
                .requestMatchers(HttpMethod.POST, "/api/messages/**").hasAnyAuthority(MemberRole.USER.name(), MemberRole.ADMIN.name())
                .requestMatchers(HttpMethod.GET, "/api/members/verify-email").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/exchanges", "/api/exchanges/five").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reviews").permitAll()

                // 사용자 및 관리자 엔드포인트 (이메일 인증 필요)
                .requestMatchers(HttpMethod.PUT, "/api/members/**").hasAuthority(MemberRole.VERIFIED_USER.name())
                .requestMatchers(HttpMethod.POST, "/api/exchanges", "/api/reviews").hasAuthority(MemberRole.VERIFIED_USER.name())
                .requestMatchers(HttpMethod.PUT, "/api/exchanges/**", "/api/reviews/**").hasAuthority(MemberRole.VERIFIED_USER.name())
                .requestMatchers("/api/messages", "/api/messages/**").hasAuthority(MemberRole.VERIFIED_USER.name())

                // 관리자 전용 엔드포인트
                .requestMatchers(HttpMethod.GET, "/api/members").hasAnyAuthority(MemberRole.ADMIN.name())
                .requestMatchers(HttpMethod.PUT, "/api/members/verify-role/**").hasAnyAuthority(MemberRole.ADMIN.name())

                // 인증된 사용자 엔드포인트 (이메일 인증 불필요)
                .requestMatchers("/api/auth/logout", "/api/members/my/**", "/api/members/my", "/api/members/resend-verification-email")
                .authenticated()
                .requestMatchers("/api/exchange-likes/**").authenticated()

                .requestMatchers("/stomp/content").permitAll()
                .requestMatchers("/stomp/content/**").permitAll()

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
                .policyDirectives("default-src 'self'; img-src 'self' data:; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline';")
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(false);
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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
