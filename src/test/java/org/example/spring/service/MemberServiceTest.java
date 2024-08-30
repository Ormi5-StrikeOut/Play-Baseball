package org.example.spring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.example.spring.constants.Gender;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.MemberRole;
import org.example.spring.domain.member.dto.MemberJoinRequestDto;
import org.example.spring.domain.member.dto.MemberResponseDto;
import org.example.spring.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    private MemberJoinRequestDto validMemberDto;

    @BeforeEach
    void setUp() {

        validMemberDto = MemberJoinRequestDto.builder()
            .email("test@example.com")
            .password("Password1!")
            .nickname("testuser")
            .name("Test User")
            .phoneNumber("010-1234-5678")
            .gender(Gender.MALE)
            .build();

    }

    @Test
    @DisplayName("회원 등록이 성공적으로 되는지 확인")
    void registerMember() {
        // Given
        String encodedPassword = "encodedPassword123!";
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(memberRepository.existsByPhoneNumber(anyString())).thenReturn(false);

        Member savedMember = Member.builder()
            .id(1L)
            .name(validMemberDto.getName())
            .email(validMemberDto.getEmail())
            .nickname(validMemberDto.getNickname())
            .password(encodedPassword)
            .phoneNumber(validMemberDto.getPhoneNumber())
            .gender(validMemberDto.getGender())
            .role(MemberRole.USER)
            .build();
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        // When
        Member registeredMember = memberService.registerMember(validMemberDto);

        // Then
        assertThat(registeredMember).isNotNull();
        assertThat(registeredMember.getId()).isNotNull();
        assertThat(registeredMember.getName()).isEqualTo(validMemberDto.getName());
        assertThat(registeredMember.getEmail()).isEqualTo(validMemberDto.getEmail());
        assertThat(registeredMember.getNickname()).isEqualTo(validMemberDto.getNickname());
        assertThat(registeredMember.getPassword()).isEqualTo(encodedPassword);
        assertThat(registeredMember.getPhoneNumber()).isEqualTo(validMemberDto.getPhoneNumber());
        assertThat(registeredMember.getGender()).isEqualTo(validMemberDto.getGender());
        assertThat(registeredMember.getRole()).isEqualTo(MemberRole.USER);

        verify(passwordEncoder).encode(validMemberDto.getPassword());
        verify(memberRepository).existsByEmail(validMemberDto.getEmail());
        verify(memberRepository).existsByNickname(validMemberDto.getNickname());
        verify(memberRepository).existsByPhoneNumber(validMemberDto.getPhoneNumber());
        verify(memberRepository).save(any(Member.class));

    }

    @Test
    @DisplayName("회원 전체 목록 조회 - 페이징")
    void getAllMembers_no_search() {
        // given
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Member> expectedPage = Page.empty();
        given(memberRepository.findAll(pageable)).willReturn(expectedPage);

        // when
        Page<MemberResponseDto> result = memberService.getAllMembers(page, size);

        // then
        assertThat(result).isEqualTo(expectedPage);
        then(memberRepository).should().findAll(pageable);
    }

}