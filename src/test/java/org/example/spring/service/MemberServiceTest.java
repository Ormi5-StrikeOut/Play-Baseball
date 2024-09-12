package org.example.spring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.example.spring.constants.Gender;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.MemberRole;
import org.example.spring.domain.member.dto.MemberEmailVerifiedResponseDto;
import org.example.spring.domain.member.dto.MemberJoinRequestDto;
import org.example.spring.domain.member.dto.MemberResponseDto;
import org.example.spring.domain.member.dto.MemberRoleModifyRequestDto;
import org.example.spring.exception.EmailVerificationTokenExpiredException;
import org.example.spring.exception.InvalidTokenException;
import org.example.spring.exception.MemberNotFoundException;
import org.example.spring.exception.ResourceNotFoundException;
import org.example.spring.repository.MemberRepository;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.example.spring.security.service.AccountManagementService;
import org.example.spring.security.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@Mock
	private MemberRepository memberRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtTokenValidator jwtValidator;
	@Mock
	private AccountManagementService accountManagementService;
	@Mock
	private EmailService emailService;
	@InjectMocks
	private MemberService memberService;

	private MemberJoinRequestDto validMemberDto;

	private Member savedMember;

	private String encodedPassword;

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
		encodedPassword = "encodedPassword123!";

		savedMember = Member.builder()
			.id(1L)
			.email(validMemberDto.getEmail())
			.nickname(validMemberDto.getNickname())
			.password(encodedPassword)
			.role(MemberRole.USER)
			.build();

	}

	@Test
	@DisplayName("회원 등록 - 성공 케이스")
	void registerMember_success() {
		// Given
		when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
		when(memberRepository.existsByEmail(anyString())).thenReturn(false);
		when(memberRepository.existsByNickname(anyString())).thenReturn(false);
		when(memberRepository.existsByPhoneNumber(anyString())).thenReturn(false);
		when(emailService.generateEmailToken(anyString())).thenReturn("verificationToken");

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
		verify(emailService).generateEmailToken(validMemberDto.getEmail());
		verify(emailService).sendVerificationEmail(validMemberDto.getEmail(), "verificationToken");
	}

	@Test
	@DisplayName("회원 이메일 인증 - 성공 케이스")
	void verifyEmail_success() {
		// Given
		String token = "validToken";
		String email = "test@example.com";
		Member member = Member.builder()
			.email(email)
			.emailVerified(false)
			.build();
		when(jwtValidator.extractUsername(token)).thenReturn(email);
		when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

		// When
		MemberEmailVerifiedResponseDto response = memberService.verifyEmail(token);

		// Then
		assertThat(response.getEmail()).isEqualTo(email);
		assertThat(member.isEmailVerified()).isTrue();
		verify(memberRepository).save(member);
	}

	@Test
	@DisplayName("회원 이메일 인증 - 토큰 만료 예외")
	void verifyEmail_token_expired() {
		// Given
		String expiredToken = "expiredToken";
		when(jwtValidator.extractUsername(expiredToken)).thenThrow(
			new EmailVerificationTokenExpiredException("Token expired"));

		// When & Then
		assertThatThrownBy(() -> memberService.verifyEmail(expiredToken))
			.isInstanceOf(EmailVerificationTokenExpiredException.class)
			.hasMessage("Token expired");
	}

	@Test
	@DisplayName("회원 이메일 인증 - 유효하지 않은 토큰 예외")
	void verifyEmail_invalid_token() {
		// Given
		String invalidToken = "invalidToken";
		when(jwtValidator.extractUsername(invalidToken)).thenThrow(new InvalidTokenException("Invalid token"));

		// When & Then
		assertThatThrownBy(() -> memberService.verifyEmail(invalidToken))
			.isInstanceOf(InvalidTokenException.class)
			.hasMessage("Invalid token");
	}

	@Test
	@DisplayName("회원 권한 수정 - 관리자만 가능")
	void modifyMemberRole_whenAdminModifies_thenRoleUpdated() {
		// Given
		Long memberId = 1L;
		MemberRoleModifyRequestDto requestDto = MemberRoleModifyRequestDto.builder().role(MemberRole.ADMIN).build();
		Member admin = Member.builder().id(2L).role(MemberRole.ADMIN).build();
		Member member = Member.builder().id(memberId).role(MemberRole.USER).build();
		when(jwtValidator.extractTokenFromHeader(any())).thenReturn("token");
		when(jwtValidator.extractUsername(anyString())).thenReturn(admin.getEmail());
		when(memberRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
		when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
		when(memberRepository.save(any(Member.class))).thenReturn(member);

		// When
		MemberResponseDto result = memberService.modifyMemberRole(memberId, requestDto, null);

		// Then
		assertThat(result).isNotNull();
		assertThat(result.getRole()).isEqualTo(MemberRole.ADMIN);
		verify(memberRepository).save(any(Member.class));
	}

	@Test
	@DisplayName("회원 권한 수정 - 관리자 권한 없음 예외")
	void modifyMemberRole_whenNonAdminModifies_thenAccessDenied() {
		// Given
		Long memberId = 1L;
		MemberRoleModifyRequestDto requestDto = MemberRoleModifyRequestDto.builder().role(MemberRole.ADMIN).build();
		Member user = Member.builder().id(2L).role(MemberRole.USER).build();
		when(jwtValidator.extractTokenFromHeader(any())).thenReturn("token");
		when(jwtValidator.extractUsername(anyString())).thenReturn(user.getEmail());
		when(memberRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

		// When & Then
		assertThatThrownBy(() -> memberService.modifyMemberRole(memberId, requestDto, null))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessage("권한이 없습니다.");
	}

	@Test
	@DisplayName("회원 삭제 - 회원 찾기 실패 예외")
	void deleteMember_member_not_found() {
		// Given
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		doThrow(new ResourceNotFoundException("Member", "email", "unknown@email.com"))
			.when(accountManagementService).deactivateAccount(request, response);

		// When & Then
		assertThrows(ResourceNotFoundException.class, () -> memberService.deleteMember(request, response));

		// Verify
		verify(accountManagementService).deactivateAccount(request, response);
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

	@Test
	@DisplayName("비밀번호 재설정 이메일 발송 - 성공 케이스")
	void sendPasswordResetEmail_success() {
		// Given
		String email = "test@example.com";
		Member member = Member.builder().email(email).build();
		when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
		when(emailService.generateEmailToken(email)).thenReturn("resetToken");
		doNothing().when(emailService).sendPasswordResetEmail(email, "resetToken");

		// When
		assertDoesNotThrow(() -> memberService.sendPasswordResetEmail(email));

		// Then
		verify(memberRepository).findByEmail(email);
		verify(emailService).generateEmailToken(email);
		verify(emailService).sendPasswordResetEmail(email, "resetToken");
	}

	@Test
	@DisplayName("비밀번호 재설정 이메일 발송 - 회원 없음 예외")
	void sendPasswordResetEmail_memberNotFound() {
		// Given
		String email = "nonexistent@example.com";
		when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(MemberNotFoundException.class, () -> memberService.sendPasswordResetEmail(email));
	}

	@Test
	@DisplayName("비밀번호 재설정 - 성공 케이스")
	void resetPassword_success() {
		// Given
		String token = "validToken";
		String email = "test@example.com";
		String newPassword = "newPassword123!";
		Member member = Member.builder().email(email).build();

		when(jwtValidator.extractUsername(token)).thenReturn(email);
		when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
		when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

		// When
		assertDoesNotThrow(() -> memberService.resetPassword(token, newPassword));

		// Then
		verify(jwtValidator).extractUsername(token);
		verify(memberRepository).findByEmail(email);
		verify(passwordEncoder).encode(newPassword);
		verify(memberRepository).save(member);
	}

	@Test
	@DisplayName("비밀번호 재설정 - 유효하지 않은 토큰")
	void resetPassword_invalidToken() {
		// Given
		String token = "invalidToken";
		String newPassword = "newPassword123!";

		when(jwtValidator.extractUsername(token)).thenThrow(new JwtException("Invalid token"));

		// When & Then
		assertThrows(InvalidTokenException.class, () -> memberService.resetPassword(token, newPassword));
	}

	@Test
	@DisplayName("비밀번호 재설정 토큰 검증 - 성공 케이스")
	void validateResetToken_success() {
		// Given
		String token = "validToken";
		String email = "test@example.com";
		Member member = Member.builder().email(email).build();

		when(jwtValidator.validateToken(token)).thenReturn(true);
		when(jwtValidator.extractUsername(token)).thenReturn(email);
		when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));

		// When
		boolean result = memberService.validateResetToken(token);

		// Then
		assertTrue(result);
		verify(jwtValidator).validateToken(token);
		verify(jwtValidator).extractUsername(token);
		verify(memberRepository).findByEmail(email);
	}

	@Test
	@DisplayName("비밀번호 재설정 토큰 검증 - 유효하지 않은 토큰")
	void validateResetToken_invalidToken() {
		// Given
		String token = "invalidToken";

		when(jwtValidator.validateToken(token)).thenReturn(false);

		// When
		boolean result = memberService.validateResetToken(token);

		// Then
		assertFalse(result);
		verify(jwtValidator).validateToken(token);
	}

	@Test
	@DisplayName("삭제된 계정 폐기 - 성공 케이스")
	void deleteExpiredAccounts_success() {
		// Given
		Timestamp expirationThreshold = Timestamp.from(Instant.now().minus(Duration.ofDays(3)));
		List<Member> expiredAccounts = Arrays.asList(
			Member.builder().id(1L).build(),
			Member.builder().id(2L).build()
		);

		when(memberRepository.findByDeletedAtBeforeAndDeletedAtIsNotNull(any(Timestamp.class)))
			.thenReturn(expiredAccounts);

		// When
		memberService.deleteExpiredAccounts();

		// Then
		verify(memberRepository).findByDeletedAtBeforeAndDeletedAtIsNotNull(any(Timestamp.class));
		verify(memberRepository).deleteAll(expiredAccounts);
	}

}