package org.example.spring.security.config;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;
import java.util.List;
import org.example.spring.domain.member.MemberRole;
import org.example.spring.security.handler.CustomAccessDeniedHandler;
import org.example.spring.security.handler.CustomAuthenticationEntryPoint;
import org.example.spring.security.jwt.CookieService;
import org.example.spring.security.jwt.JwtAuthenticationService;
import org.example.spring.security.jwt.JwtValidatorFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
public class SecurityDevConfig {

    private final CookieService cookieService;
    private final JwtAuthenticationService jwtAuthenticationService;

    public SecurityDevConfig(CookieService cookieService, JwtAuthenticationService jwtAuthenticationService) {
        this.cookieService = cookieService;
        this.jwtAuthenticationService = jwtAuthenticationService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(corsConfig -> corsConfig.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(jwtValidatorFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(request -> request
                // 비회원 공개 엔드포인트
                .requestMatchers(HttpMethod.POST, "/api/members/join", "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/members/verify/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/exchanges", "/api/exchanges/five").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reviews").permitAll()

                // 사용자 및 관리자 엔드포인트
                .requestMatchers(HttpMethod.PUT, "/api/members/**").hasAnyAuthority(MemberRole.USER.name(), MemberRole.ADMIN.name())
                .requestMatchers(HttpMethod.DELETE, "/api/members/my").hasAnyAuthority(MemberRole.USER.name(), MemberRole.ADMIN.name())
                .requestMatchers(HttpMethod.GET, "/api/members/**").hasAnyAuthority(MemberRole.USER.name(), MemberRole.ADMIN.name())
                .requestMatchers(HttpMethod.POST, "/api/exchanges", "/api/reviews").hasAnyAuthority(MemberRole.USER.name(), MemberRole.ADMIN.name())
                .requestMatchers(HttpMethod.PUT, "/api/exchanges/**", "/api/reviews/**").hasAnyAuthority(MemberRole.USER.name(), MemberRole.ADMIN.name())

                // 관리자 전용 엔드포인트
                .requestMatchers(HttpMethod.GET, "/api/members").hasAuthority(MemberRole.ADMIN.name())
                .requestMatchers(HttpMethod.PUT, "/api/members/verify-role/**").hasAuthority(MemberRole.ADMIN.name())

                // 인증된 사용자 엔드포인트
                .requestMatchers(HttpMethod.GET, "/api/auth/logout", "/api/auth/reissue-token/**").authenticated()
                .requestMatchers("/api/exchange-likes/**").authenticated()

                // 기타 모든 요청
                .anyRequest().authenticated()
            );
        http.formLogin(withDefaults());
        http.httpBasic(basicConfig -> basicConfig.authenticationEntryPoint(new CustomAuthenticationEntryPoint()));
        http.exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer.accessDeniedHandler(new CustomAccessDeniedHandler()));

        return http.build();
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

    @Bean
    public JwtValidatorFilter jwtValidatorFilter() {
        return new JwtValidatorFilter(cookieService, jwtAuthenticationService);
    }
}
