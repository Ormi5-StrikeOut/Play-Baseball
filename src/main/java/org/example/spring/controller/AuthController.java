package org.example.spring.controller;

import org.example.spring.common.ApiResponseDto;
import org.example.spring.domain.member.dto.LoginRequestDto;
import org.example.spring.domain.member.dto.LoginResponseDto;
import org.example.spring.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

	@PostMapping("/login")
	@Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
	@ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = LoginResponseDto.class)))
	public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(
		@Parameter(description = "로그인 요청 데이터") @RequestBody LoginRequestDto loginRequestDto,
		@Parameter(description = "HTTP 응답", hidden = true) HttpServletResponse response) {
		LoginResponseDto loginResponseDto = authService.login(loginRequestDto, response);
		return ResponseEntity.ok(ApiResponseDto.success("로그인 성공", loginResponseDto));
	}

	@PostMapping("/logout")
	@Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리합니다.")
	@ApiResponse(responseCode = "200", description = "로그아웃 성공")
	public ResponseEntity<ApiResponseDto<Void>> logout(
		@Parameter(description = "현재 사용자의 요청", hidden = true) HttpServletRequest request,
		@Parameter(description = "HTTP 응답", hidden = true) HttpServletResponse response) {
		authService.logout(request, response);
		return ResponseEntity.ok(ApiResponseDto.success("로그아웃 성공", null));
	}
}
