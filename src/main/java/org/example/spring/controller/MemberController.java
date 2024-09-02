package org.example.spring.controller;

import lombok.RequiredArgsConstructor;
import org.example.spring.common.ApiResponseDto;
import org.example.spring.domain.member.dto.MemberJoinRequestDto;
import org.example.spring.domain.member.dto.MemberResponseDto;
import org.example.spring.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
}
