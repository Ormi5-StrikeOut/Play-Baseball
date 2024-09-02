package org.example.spring.security.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.example.spring.domain.member.MemberRole;
import org.example.spring.security.handler.CustomAccessDeniedHandler;
import org.example.spring.security.handler.CustomAuthenticationEntryPoint;
import org.example.spring.security.jwt.CookieService;
import org.example.spring.security.jwt.JwtAuthenticationService;
import org.example.spring.security.jwt.JwtValidatorFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

@Configuration
public class SecurityConfig {

    private final CookieService cookieService;
    private final JwtAuthenticationService jwtAuthenticationService;

    public SecurityConfig(CookieService cookieService, JwtAuthenticationService jwtAuthenticationService) {
        this.cookieService = cookieService;
        this.jwtAuthenticationService = jwtAuthenticationService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .addFilterBefore(jwtValidatorFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(request -> request
                .requestMatchers("/api/members", "api/members/verify-role/").hasAuthority(MemberRole.ADMIN.name())
                .requestMatchers("/api/exchanges").hasAnyAuthority(MemberRole.USER.name(), MemberRole.ADMIN.name())
                .anyRequest().permitAll()
            );
        http.formLogin(withDefaults());
        http.httpBasic(basicConfig -> basicConfig.authenticationEntryPoint(new CustomAuthenticationEntryPoint()));
        http.exceptionHandling(exceptionHandlingConfigurer -> exceptionHandlingConfigurer.accessDeniedHandler(new CustomAccessDeniedHandler()));

        return http.build();
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
