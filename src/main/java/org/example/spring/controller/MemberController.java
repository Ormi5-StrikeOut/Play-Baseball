package org.example.spring.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.spring.common.ApiResponseDto;
import org.example.spring.domain.member.dto.MemberEmailVerifiedResponseDto;
import org.example.spring.domain.member.dto.MemberJoinRequestDto;
import org.example.spring.domain.member.dto.MemberModifyRequestDto;
import org.example.spring.domain.member.dto.MemberResponseDto;
import org.example.spring.domain.member.dto.MemberRoleModifyRequestDto;
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

/**
 * 회원 관련 API 엔드포인트를 담당하는 컨트롤러 클래스입니다. 회원 가입, 회원 목록 조회 등의 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 새로운 회원을 등록합니다.
     *
     * @param memberJoinRequestDto 회원 가입 정보를 담은 DTO
     * @return 회원 가입 성공 여부 및 생성된 회원 MemberResponseDto
     */
    @PostMapping("/join")
    public ResponseEntity<ApiResponseDto<MemberResponseDto>> registerMember(@RequestBody MemberJoinRequestDto memberJoinRequestDto) {
        MemberResponseDto savedMember = memberService.registerMember(memberJoinRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponseDto.success("회원가입에 성공했습니다.", savedMember));
    }

    /**
     * 페이징 처리된 전체 회원 목록을 조회합니다.
     *
     * @param page 조회할 페이지 번호
     * @param size 한 페이지당 표시할 회원 수
     * @return 페이징된 회원 목록 DTO
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<MemberResponseDto>>> getAllMembers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<MemberResponseDto> members = memberService.getAllMembers(page, size);
        return ResponseEntity.ok(ApiResponseDto.success("회원 목록 조회 성공", members));
    }

    /**
     * JWT 토큰에서 추출된 회원의 정보를 조회합니다.
     *
     * @param request HttpServletRequest
     * @return MemberResponseDto
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponseDto<MemberResponseDto>> getMyMember(HttpServletRequest request) {
        MemberResponseDto myMember = memberService.getMyMember(request);
        return ResponseEntity.ok(ApiResponseDto.success("회원 조회 성공:", myMember));
    }

    /**
     * JWT 토큰에서 추출된 회원의 탈퇴(soft delete)
     *
     * @param request HttpServletRequest
     */
    @PutMapping("/my/resign")
    public ResponseEntity<ApiResponseDto<Void>> deleteMember(HttpServletRequest request, HttpServletResponse response) {
        memberService.deleteMember(request, response);
        return ResponseEntity.ok(ApiResponseDto.success("회원 삭제 성공", null));
    }

    /**
     * 요청온 Request 에서 추출한 email 을 가지고 있는 회원을 modify 합니다.
     *
     * @param request 요청
     * @return 수정된 회원 응답 Dto
     */
    @PutMapping("/my/modify-member")
    public ResponseEntity<ApiResponseDto<MemberResponseDto>> modifyMember(HttpServletRequest request, @RequestBody MemberModifyRequestDto member) {
        MemberResponseDto modifiedMember = memberService.modifyMember(request, member);
        return ResponseEntity.ok(ApiResponseDto.success("회원 정보 수정 성공:", modifiedMember));
    }

    /**
     * admin 권한을 가지고 있으면 회원의 권한을 수정할 수 있습니다.
     *
     * @param memberId                   수정할 회원 ID
     * @param memberRoleModifyRequestDto 변경할 권한 요청
     * @param request                    HttpServletRequest
     * @return 권한이 변경된 회원 Dto
     */
    @PatchMapping("verify-role/{memberId}")
    public ResponseEntity<ApiResponseDto<MemberResponseDto>> modifyMemberRole(
        @PathVariable Long memberId,
        @RequestBody MemberRoleModifyRequestDto memberRoleModifyRequestDto,
        HttpServletRequest request) {
        MemberResponseDto modifiedMember = memberService.modifyMemberRole(memberId, memberRoleModifyRequestDto, request);
        return ResponseEntity.ok(ApiResponseDto.success("회원 권한 수정 성공", modifiedMember));
    }

    /**
     * 이메일 인증 링크를 클릭했을 때 처리하는 엔드포인트입니다.
     *
     * @param token 이메일 인증 토큰
     * @return 이메일 인증 결과
     */
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponseDto<MemberEmailVerifiedResponseDto>> verifyEmail(@RequestParam String token) {
        MemberEmailVerifiedResponseDto memberEmailVerifiedResponseDto = memberService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponseDto.success("이메일 인증이 완료되었습니다.", memberEmailVerifiedResponseDto));
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<ApiResponseDto<Void>> resendVerificationEmail(@RequestParam String email) {
        memberService.resendVerificationEmail(email);
        return ResponseEntity.ok(ApiResponseDto.success("Verification email resent", null));

    }
}
