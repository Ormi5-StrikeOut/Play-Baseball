package org.example.spring.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import org.example.spring.common.ApiResponseDto;
import org.example.spring.constants.Gender;
import org.example.spring.domain.member.MemberRole;
import org.example.spring.domain.member.dto.MemberEmailVerifiedResponseDto;
import org.example.spring.domain.member.dto.MemberJoinRequestDto;
import org.example.spring.domain.member.dto.MemberModifyRequestDto;
import org.example.spring.domain.member.dto.MemberResponseDto;
import org.example.spring.domain.member.dto.MemberRoleModifyRequestDto;
import org.example.spring.service.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberController memberController;

    @Test
    void registerMember_Success() {
        MemberJoinRequestDto requestDto = MemberJoinRequestDto.builder()
            .email("test@example.com")
            .password("password")
            .nickname("nickname")
            .name("name")
            .phoneNumber("010-1234-5678")
            .gender(Gender.MALE)
            .build();

        MemberResponseDto responseDto = MemberResponseDto.builder()
            .id(1L)
            .email("test@example.com")
            .nickname("nickname")
            .role(MemberRole.USER)
            .build();

        when(memberService.registerMember(any(MemberJoinRequestDto.class))).thenReturn(responseDto);

        ResponseEntity<ApiResponseDto<MemberResponseDto>> response = memberController.registerMember(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("회원가입에 성공했습니다.", response.getBody().getMessage());
        assertEquals(responseDto, response.getBody().getData());
        verify(memberService, times(1)).registerMember(any(MemberJoinRequestDto.class));
    }

    @Test
    void getAllMembers_Success() {
        Page<MemberResponseDto> page = new PageImpl<>(Collections.singletonList(MemberResponseDto.builder().build()));
        when(memberService.getAllMembers(anyInt(), anyInt())).thenReturn(page);

        ResponseEntity<ApiResponseDto<Page<MemberResponseDto>>> response = memberController.getAllMembers(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("회원 목록 조회 성공", response.getBody().getMessage());
        assertEquals(page, response.getBody().getData());
    }

    @Test
    void getMyMember_Success() {
        MemberResponseDto responseDto = MemberResponseDto.builder()
            .id(1L)
            .email("test@example.com")
            .nickname("nickname")
            .role(MemberRole.USER)
            .build();

        when(memberService.getMyMember(any(HttpServletRequest.class))).thenReturn(responseDto);

        ResponseEntity<ApiResponseDto<MemberResponseDto>> response = memberController.getMyMember(mock(HttpServletRequest.class));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("회원 조회 성공:", response.getBody().getMessage());
        assertEquals(responseDto, response.getBody().getData());
    }

    @Test
    void deleteMember_Success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        doNothing().when(memberService).deleteMember(any(HttpServletRequest.class), any(HttpServletResponse.class));

        ResponseEntity<ApiResponseDto<Void>> result = memberController.deleteMember(request, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("회원 삭제 성공", result.getBody().getMessage());
        assertNull(result.getBody().getData());
        verify(memberService, times(1)).deleteMember(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void modifyMember_Success() {
        MemberModifyRequestDto requestDto = MemberModifyRequestDto.builder()
            .nickname("newNickname")
            .name("newName")
            .phoneNumber("010-9876-5432")
            .gender(Gender.FEMALE)
            .build();

        MemberResponseDto responseDto = MemberResponseDto.builder()
            .id(1L)
            .email("test@example.com")
            .nickname("newNickname")
            .role(MemberRole.USER)
            .build();

        when(memberService.modifyMember(any(HttpServletRequest.class), any(MemberModifyRequestDto.class))).thenReturn(responseDto);

        ResponseEntity<ApiResponseDto<MemberResponseDto>> response = memberController.modifyMember(mock(HttpServletRequest.class), requestDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("회원 정보 수정 성공:", response.getBody().getMessage());
        assertEquals(responseDto, response.getBody().getData());
    }

    @Test
    void modifyMemberRole_Success() {
        MemberRoleModifyRequestDto requestDto = MemberRoleModifyRequestDto.builder()
            .role(MemberRole.ADMIN)
            .build();

        MemberResponseDto responseDto = MemberResponseDto.builder()
            .id(1L)
            .email("test@example.com")
            .nickname("nickname")
            .role(MemberRole.ADMIN)
            .build();

        when(memberService.modifyMemberRole(anyLong(), any(MemberRoleModifyRequestDto.class), any(HttpServletRequest.class))).thenReturn(responseDto);

        ResponseEntity<ApiResponseDto<MemberResponseDto>> response = memberController.modifyMemberRole(1L, requestDto,
            mock(HttpServletRequest.class));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("회원 권한 수정 성공", response.getBody().getMessage());
        assertEquals(responseDto, response.getBody().getData());
    }

    @Test
    void verifyEmail_Success() {
        MemberEmailVerifiedResponseDto responseDto = MemberEmailVerifiedResponseDto.builder()
            .email("test@example.com")
            .build();

        when(memberService.verifyEmail(anyString())).thenReturn(responseDto);

        ResponseEntity<ApiResponseDto<MemberEmailVerifiedResponseDto>> response = memberController.verifyEmail("token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("이메일 인증이 완료되었습니다.", response.getBody().getMessage());
        assertEquals(responseDto, response.getBody().getData());
    }

    @Test
    void resendVerificationEmail_Success() {
        doNothing().when(memberService).resendVerificationEmail(anyString());

        ResponseEntity<ApiResponseDto<Void>> response = memberController.resendVerificationEmail("test@example.com");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Verification email resent", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(memberService, times(1)).resendVerificationEmail("test@example.com");
    }
}