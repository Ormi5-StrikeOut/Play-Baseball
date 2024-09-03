package org.example.spring.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.spring.common.ApiResponseDto;
import org.example.spring.domain.member.dto.LoginRequestDto;
import org.example.spring.domain.member.dto.LoginResponseDto;
import org.example.spring.security.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 HTTP 요청을 처리하는 컨트롤러 클래스입니다.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 사용자 로그인 요청을 처리합니다.
     *
     * @param loginRequestDto 로그인 요청 데이터
     * @param response HTTP 응답
     * @return 로그인 결과와 액세스 토큰을 포함한 ResponseEntity
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {

        LoginResponseDto loginResponseDto = authService.login(loginRequestDto, response);
        return ResponseEntity.ok(ApiResponseDto.success("로그인 성공", loginResponseDto));

    }

}
