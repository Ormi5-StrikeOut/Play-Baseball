package org.example.spring.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.spring.common.ApiResponseDto;
import org.example.spring.domain.member.dto.LoginRequestDto;
import org.example.spring.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<String>> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {

        String accessToken = authService.login(loginRequestDto, response);
        return ResponseEntity.ok(ApiResponseDto.success("로그인 성공", accessToken));

    }

}
