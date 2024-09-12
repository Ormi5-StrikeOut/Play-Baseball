package org.example.spring.controller;

import org.example.spring.common.ApiResponseDto;
import org.example.spring.domain.member.dto.EmailRequestDto;
import org.example.spring.domain.member.dto.MemberEmailVerifiedResponseDto;
import org.example.spring.domain.member.dto.MemberJoinRequestDto;
import org.example.spring.domain.member.dto.MemberModifyRequestDto;
import org.example.spring.domain.member.dto.MemberResponseDto;
import org.example.spring.domain.member.dto.MemberRoleModifyRequestDto;
import org.example.spring.domain.member.dto.PasswordResetRequestDto;
import org.example.spring.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 회원 관련 API 엔드포인트를 담당하는 컨트롤러 클래스입니다. 회원 가입, 회원 목록 조회 등의 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController {

	private final MemberService memberService;

	@PostMapping("/join")
	@Operation(summary = "회원 가입", description = "새로운 회원을 등록합니다.")
	@ApiResponse(responseCode = "201", description = "회원가입 성공",
		content = @Content(schema = @Schema(implementation = MemberResponseDto.class)))
	public ResponseEntity<ApiResponseDto<MemberResponseDto>> registerMember(
		@Valid @RequestBody MemberJoinRequestDto memberJoinRequestDto) {
		MemberResponseDto savedMember = memberService.registerMember(memberJoinRequestDto);
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(ApiResponseDto.success("회원가입에 성공했습니다.", savedMember));
	}

	@GetMapping
	@Operation(summary = "전체 회원 목록 조회", description = "페이징 처리된 전체 회원 목록을 조회합니다.")
	@ApiResponse(responseCode = "200", description = "회원 목록 조회 성공",
		content = @Content(schema = @Schema(implementation = Page.class)))
	public ResponseEntity<ApiResponseDto<Page<MemberResponseDto>>> getAllMembers(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		Page<MemberResponseDto> members = memberService.getAllMembers(page, size);
		return ResponseEntity.ok(ApiResponseDto.success("회원 목록 조회 성공", members));
	}

	@GetMapping("/my")
	@Operation(summary = "내 정보 조회", description = "JWT 토큰에서 추출된 회원의 정보를 조회합니다.")
	@ApiResponse(responseCode = "200", description = "회원 정보 조회 성공",
		content = @Content(schema = @Schema(implementation = MemberResponseDto.class)))
	public ResponseEntity<ApiResponseDto<MemberResponseDto>> getMyMember(HttpServletRequest request) {
		MemberResponseDto myMember = memberService.getMyMember(request);
		return ResponseEntity.ok(ApiResponseDto.success("회원 조회 성공:", myMember));
	}

	@PutMapping("/my/resign")
	@Operation(summary = "회원 탈퇴", description = "JWT 토큰에서 추출된 회원의 탈퇴(soft delete)를 처리합니다.")
	@ApiResponse(responseCode = "200", description = "회원 삭제 성공")
	public ResponseEntity<ApiResponseDto<Void>> deleteMember(HttpServletRequest request, HttpServletResponse response) {
		memberService.deleteMember(request, response);
		return ResponseEntity.ok(ApiResponseDto.success("회원 삭제 성공", null));
	}

	@PutMapping("/my/modify-member")
	@Operation(summary = "회원 정보 수정", description = "요청온 Request에서 추출한 email을 가지고 있는 회원을 수정합니다.")
	@ApiResponse(responseCode = "200", description = "회원 정보 수정 성공",
		content = @Content(schema = @Schema(implementation = MemberResponseDto.class)))
	public ResponseEntity<ApiResponseDto<MemberResponseDto>> modifyMember(@Valid HttpServletRequest request,
		@RequestBody MemberModifyRequestDto member) {
		MemberResponseDto modifiedMember = memberService.modifyMember(request, member);
		return ResponseEntity.ok(ApiResponseDto.success("회원 정보 수정 성공:", modifiedMember));
	}

	@PatchMapping("verify-role/{memberId}")
	@Operation(summary = "회원 권한 수정", description = "admin 권한을 가지고 있으면 회원의 권한을 수정할 수 있습니다.")
	@ApiResponse(responseCode = "200", description = "회원 권한 수정 성공",
		content = @Content(schema = @Schema(implementation = MemberResponseDto.class)))
	public ResponseEntity<ApiResponseDto<MemberResponseDto>> modifyMemberRole(@Valid
	@PathVariable Long memberId,
		@RequestBody MemberRoleModifyRequestDto memberRoleModifyRequestDto,
		HttpServletRequest request) {
		MemberResponseDto modifiedMember = memberService.modifyMemberRole(memberId, memberRoleModifyRequestDto,
			request);
		return ResponseEntity.ok(ApiResponseDto.success("회원 권한 수정 성공", modifiedMember));
	}

	@GetMapping("/verify-email")
	@Operation(summary = "이메일 인증", description = "이메일 인증 링크를 클릭했을 때 처리하는 엔드포인트입니다.")
	@ApiResponse(responseCode = "200", description = "이메일 인증 완료",
		content = @Content(schema = @Schema(implementation = MemberEmailVerifiedResponseDto.class)))
	public ResponseEntity<ApiResponseDto<MemberEmailVerifiedResponseDto>> verifyEmail(@RequestParam String token) {
		MemberEmailVerifiedResponseDto memberEmailVerifiedResponseDto = memberService.verifyEmail(token);
		return ResponseEntity.ok(ApiResponseDto.success("이메일 인증이 완료되었습니다.", memberEmailVerifiedResponseDto));
	}

	@PostMapping("/resend-verification-email")
	@Operation(summary = "인증 이메일 재발송", description = "인증 이메일을 재발송합니다.")
	@ApiResponse(responseCode = "200", description = "인증 이메일 재발송 완료")
	public ResponseEntity<ApiResponseDto<Void>> resendVerificationEmail(@RequestParam String email) {
		memberService.resendVerificationEmail(email);
		return ResponseEntity.ok(ApiResponseDto.success("인증 이메일 재발송이 완료되었습니다.", null));
	}

	@PostMapping("/request-password-reset")
	@Operation(summary = "비밀번호 재설정 요청", description = "비밀번호 재설정 이메일을 발송합니다.")
	@ApiResponse(responseCode = "200", description = "비밀번호 재설정 메일 발송 완료")
	public ResponseEntity<ApiResponseDto<Void>> requestPasswordReset(
		@RequestBody @Valid EmailRequestDto emailRequestDto) {
		memberService.sendPasswordResetEmail(emailRequestDto.getEmail());
		return ResponseEntity.ok(ApiResponseDto.success("패스워드 재설정 메일 발송이 완료되었습니다.", null));
	}

	@GetMapping("/reset-password")
	@Operation(summary = "비밀번호 재설정 토큰 검증", description = "비밀번호 재설정 토큰의 유효성을 검증합니다.")
	@ApiResponse(responseCode = "200", description = "유효한 토큰")
	@ApiResponse(responseCode = "400", description = "유효하지 않은 토큰")
	public ResponseEntity<ApiResponseDto<Void>> validateResetToken(@RequestParam String token) {
		boolean isValid = memberService.validateResetToken(token);
		if (isValid) {
			return ResponseEntity.ok(ApiResponseDto.success("유효한 토큰입니다.", null));
		} else {
			return ResponseEntity.badRequest().body(ApiResponseDto.error("유효하지 않은 토큰입니다."));
		}
	}

	@PatchMapping("/reset-password")
	@Operation(summary = "비밀번호 재설정", description = "새로운 비밀번호로 재설정합니다.")
	@ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공")
	public ResponseEntity<ApiResponseDto<Void>> resetPassword(@RequestParam String token,
		@RequestBody @Valid PasswordResetRequestDto passwordResetRequestDto) {
		memberService.resetPassword(token, passwordResetRequestDto.getNewPassword());
		return ResponseEntity.ok(ApiResponseDto.success("비밀번호가 성공적으로 재설정되었습니다.", null));
	}
}
