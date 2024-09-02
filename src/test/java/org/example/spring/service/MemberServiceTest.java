package org.example.spring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import org.springframework.data.domain.PageImpl;
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

        assertThat(memberService).isNotNull();
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
        MemberResponseDto registeredMember = memberService.registerMember(validMemberDto);

        // Then
        assertThat(registeredMember).isNotNull();
        assertThat(registeredMember.getId()).isEqualTo(1L);
        assertThat(registeredMember.getEmail()).isEqualTo(validMemberDto.getEmail());
        assertThat(registeredMember.getNickname()).isEqualTo(validMemberDto.getNickname());
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

        Member member = Member.builder()
            .id(1L)
            .email("test@example.com")
            .nickname("testuser")
            .role(MemberRole.USER)
            .build();

        Page<Member> memberPage = new PageImpl<>(List.of(member), pageable, 1);
        when(memberRepository.findAll(pageable)).thenReturn(memberPage);

        // when
        Page<MemberResponseDto> result = memberService.getAllMembers(page, size);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getEmail()).isEqualTo("test@example.com");
        assertThat(result.getContent().getFirst().getNickname()).isEqualTo("testuser");
        assertThat(result.getContent().getFirst().getRole()).isEqualTo(MemberRole.USER);

        verify(memberRepository).findAll(pageable);
    }
}
