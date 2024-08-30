package org.example.spring.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.common.ApiResponseDto;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.dto.MemberJoinRequestDto;
import org.example.spring.domain.member.dto.MemberResponseDto;
import org.example.spring.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/join")
    public ResponseEntity<ApiResponseDto<String>> registerMember(@RequestBody MemberJoinRequestDto memberJoinRequestDto) {
        Member member = memberService.registerMember(memberJoinRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponseDto.success("회원가입에 성공했습니다.", member.getNickname()));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<MemberResponseDto>>> getAllMembers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<MemberResponseDto> members = memberService.getAllMembers(page, size);
        return ResponseEntity.ok(ApiResponseDto.success("회원 목록 조회 성공", members));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponseDto.error("회원가입에 실패했습니다: " + e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleException(Exception e) {
        log.error("서버 오류 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponseDto.error("서버 오류가 발생했습니다."));
    }


}
