package org.example.spring.service;

import static org.example.spring.domain.member.dto.MemberResponseDto.*;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.MemberRole;
import org.example.spring.domain.member.dto.MemberEmailVerifiedResponseDto;
import org.example.spring.domain.member.dto.MemberJoinRequestDto;
import org.example.spring.domain.member.dto.MemberModifyRequestDto;
import org.example.spring.domain.member.dto.MemberResponseDto;
import org.example.spring.domain.member.dto.MemberRoleModifyRequestDto;
import org.example.spring.exception.EmailAlreadyVerifiedException;
import org.example.spring.exception.EmailVerificationTokenExpiredException;
import org.example.spring.exception.InvalidTokenException;
import org.example.spring.exception.MemberNotFoundException;
import org.example.spring.exception.ResourceNotFoundException;
import org.example.spring.repository.MemberRepository;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.example.spring.security.service.AccountManagementService;
import org.example.spring.security.service.EmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 회원 관련 서비스 로직을 담당하는 클래스입니다. 회원 가입, 회원 목록 조회 등의 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

	private final AccountManagementService accountManagementService;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenValidator jwtValidator;
	private final EmailService emailService;

	/**
	 * JWT 토큰에서 추출된 회원의 정보를 조회합니다.
	 *
	 * @param request HttpServletRequest
	 * @return MemberResponseDto
	 */
	@Transactional(readOnly = true)
	public MemberResponseDto getMyMember(HttpServletRequest request) {
		Member member = getMemberByToken(request);
		return MemberResponseDto.toDto(member);
	}

	/**
	 * 페이징 처리된 전체 회원 목록을 조회합니다.
	 *
	 * @param page 조회할 페이지 번호
	 * @param size 한 페이지당 표시할 회원 수
	 * @return 페이징된 회원 목록 DTO
	 */
	@Transactional(readOnly = true)
	public Page<MemberResponseDto> getAllMembers(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
		Page<Member> members = memberRepository.findAll(pageable);
		return members.map(MemberResponseDto::toDto);
	}

	/**
	 * 새로운 회원을 등록합니다.
	 *
	 * @param memberJoinRequestDto 회원 가입 정보를 담은 DTO
	 * @return 등록된 회원 MemberResponseDto
	 */
	public MemberResponseDto registerMember(MemberJoinRequestDto memberJoinRequestDto) {
		checkDuplicates(memberJoinRequestDto);

		String hashedPassword = passwordEncoder.encode(memberJoinRequestDto.getPassword());

		Member member = MemberJoinRequestDto.toEntity(memberJoinRequestDto, hashedPassword);

		Member saveMember = memberRepository.save(member);

		String token = emailService.generateEmailToken(member.getEmail());
		emailService.sendVerificationEmail(member.getEmail(), token);

		return toDto(saveMember);
	}

	/**
	 * 이메일 인증 토큰을 검증하고 해당 회원의 이메일 인증 상태를 업데이트합니다.
	 *
	 * @param token 이메일 인증 토큰
	 */
	public MemberEmailVerifiedResponseDto verifyEmail(String token) {
		try {
			log.debug("get email verify token: {}", token);
			String email = jwtValidator.extractUsername(token);
			Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Member", "email", email));

			if (member.isEmailVerified()) {
				throw new EmailAlreadyVerifiedException("이미 인증된 이메일입니다.");
			}

			member.updateEmailVerified(true);
			memberRepository.save(member);
			return MemberEmailVerifiedResponseDto.builder().email(member.getEmail()).build();

		} catch (ExpiredJwtException e) {
			throw new EmailVerificationTokenExpiredException("이메일 인증 토큰이 만료되었습니다.");
		} catch (JwtException e) {
			throw new InvalidTokenException("유효하지 않은 토큰입니다.");
		}
	}

	/**
	 * 회원 가입 정보의 중복 여부를 확인합니다.
	 *
	 * @param memberJoinRequestDto 회원 가입 정보를 담은 DTO
	 * @throws IllegalArgumentException 중복된 정보가 있을 경우
	 */
	private void checkDuplicates(MemberJoinRequestDto memberJoinRequestDto) {
		List<String> errorMessages = new ArrayList<>();

		if (memberRepository.existsByEmail(memberJoinRequestDto.getEmail())) {
			errorMessages.add(MemberField.EMAIL.description + ": " + memberJoinRequestDto.getEmail());
		}

		if (memberRepository.existsByNickname(memberJoinRequestDto.getNickname())) {
			errorMessages.add(MemberField.NICKNAME.description + ": " + memberJoinRequestDto.getNickname());
		}

		if (memberRepository.existsByPhoneNumber(memberJoinRequestDto.getPhoneNumber())) {
			errorMessages.add(MemberField.PHONE_NUMBER.description + ": " + memberJoinRequestDto.getPhoneNumber());
		}

		if (!errorMessages.isEmpty()) {
			throw new IllegalArgumentException("이미 존재하는 " + String.join(", ", errorMessages) + "입니다.");
		}
	}

	/**
	 * 이메일 인증 이메일을 재발송 합니다.
	 *
	 * @param email email
	 */
	public void resendVerificationEmail(String email) {
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new ResourceNotFoundException("Member", "email", email));
		if (member.isEmailVerified()) {
			throw new EmailAlreadyVerifiedException("Email already verified");
		}

		String token = emailService.generateEmailToken(member.getEmail());
		emailService.sendVerificationEmail(member.getEmail(), token);
	}

	/**
	 * 패스워드 재설정 이메일을 발송합니다.
	 */
	public void sendPasswordResetEmail(String email) {
		Member member = memberRepository.findByEmail(email)
			.orElseThrow(() -> new MemberNotFoundException("회원을 찾지 못했습니다."));
		String resetToken = emailService.generateEmailToken(member.getEmail());
		emailService.sendPasswordResetEmail(member.getEmail(), resetToken);
	}

	/**
	 * 비밀번호를 재설정합니다.
	 *
	 * @param token       비밀번호 재설정 토큰
	 * @param newPassword 새로운 비밀번호
	 * @throws ResourceNotFoundException 해당 이메일을 가진 회원이 없을 경우
	 * @throws InvalidTokenException     유효하지 않은 토큰일 경우
	 */
	public void resetPassword(String token, String newPassword) {
		try {
			String email = jwtValidator.extractUsername(token);
			Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("Member", "email", email));

			String encodedPassword = passwordEncoder.encode(newPassword);
			member.updatePassword(encodedPassword);
			memberRepository.save(member);
		} catch (JwtException e) {
			throw new InvalidTokenException("유효하지 않은 비밀번호 재설정 토큰입니다.");
		}
	}

	/**
	 * JWT 토큰에서 추출된 회원의 탈퇴(soft delete)
	 *
	 * @param request HttpServletRequest
	 */
	public void deleteMember(HttpServletRequest request, HttpServletResponse response) {
		accountManagementService.deactivateAccount(request, response);
	}

	/**
	 * 매일 자정에 삭제된지 3일된 계정을 폐기합니다.
	 */
	@Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
	public void deleteExpiredAccounts() {
		Timestamp expirationThreshold = Timestamp.from(Instant.now().minus(Duration.ofDays(3)));
		List<Member> expiredAccounts = memberRepository.findByDeletedAtBeforeAndDeletedAtIsNotNull(expirationThreshold);

		memberRepository.deleteAll(expiredAccounts);

		log.info("Expired accounts deleted: {}", expiredAccounts.size());
	}

	/**
	 * 요청온 Request 에서 추출한 email 을 가지고 있는 회원을 modify 합니다.
	 *
	 * @param request                요청
	 * @param memberModifyRequestDto 회원 정보 수정 요청 Dto
	 * @return 수정된 회원 응답 Dto
	 */
	public MemberResponseDto modifyMember(HttpServletRequest request, MemberModifyRequestDto memberModifyRequestDto) {
		Member member = getMemberByToken(request);
		member.updateFrom(memberModifyRequestDto);
		Member modifiedMember = memberRepository.save(member);
		return MemberResponseDto.toDto(modifiedMember);
	}

	/**
	 * admin 권한을 가지고 있으면 회원의 권한을 수정할 수 있습니다.
	 *
	 * @param memberId                   수정할 회원 ID
	 * @param memberRoleModifyRequestDto 변경할 권한 요청
	 * @param request                    HttpServletRequest
	 * @return 권한이 변경된 회원 Dto
	 */
	public MemberResponseDto modifyMemberRole(Long memberId, MemberRoleModifyRequestDto memberRoleModifyRequestDto,
		HttpServletRequest request) {
		// 요청을 보낸 관리자 정보 확인
		Member admin = getMemberByToken(request);
		if (!admin.getRole().equals(MemberRole.ADMIN)) {
			throw new AccessDeniedException("권한이 없습니다.");
		}

		// 수정할 회원 정보 조회
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ResourceNotFoundException("Member", "id", memberId));

		// 권한 정보 업데이트
		member.updateRole(memberRoleModifyRequestDto.getRole());

		// 수정된 회원 정보 저장
		Member modifiedMember = memberRepository.save(member);
		return MemberResponseDto.toDto(modifiedMember);
	}

	/**
	 * 토큰으로 멤버 를 찾습니다.
	 *
	 * @param request HttpServletRequest
	 * @return Member 엔티티
	 */
	private Member getMemberByToken(HttpServletRequest request) {
		String token = jwtValidator.extractTokenFromHeader(request);
		String email = jwtValidator.extractUsername(token);
		return memberRepository.findByEmail(email)
			.orElseThrow(() -> new ResourceNotFoundException("Member", "email", email));
	}

	/**
	 * 비밀번호 재설정 토큰의 유효성을 검증합니다.
	 * <p>
	 * 이 메서드는 다음 사항들을 검증합니다:
	 * 1. 토큰의 구조, 서명, 만료 여부
	 * 2. 토큰에 포함된 이메일에 해당하는 사용자의 존재 여부
	 *
	 * @param token 검증할 비밀번호 재설정 토큰
	 * @return 토큰이 유효하면 true, 그렇지 않으면 false
	 * @throws IllegalArgumentException 토큰이 null이거나 빈 문자열인 경우
	 * @see JwtTokenValidator#validateToken(String)
	 * @see JwtTokenValidator#extractUsername(String)
	 */
	@Transactional(readOnly = true)
	public boolean validateResetToken(String token) {
		try {
			// 1. 토큰 구조와 서명 확인
			if (!jwtValidator.validateToken(token)) {
				return false;
			}

			// 2. 토큰에서 이메일 추출
			String email = jwtValidator.extractUsername(token);

			// 3. 해당 이메일을 가진 사용자가 존재하는지 확인
			Member member = memberRepository.findByEmail(email)
				.orElse(null);

			return member != null;
		} catch (Exception e) {
			// 로깅 추가
			log.error("Error validating reset token", e);
			return false;
		}
	}

	@Getter
	private enum MemberField {
		EMAIL("이메일"),
		PASSWORD("비밀번호"),
		NICKNAME("닉네임"),
		NAME("이름"),
		PHONE_NUMBER("전화번호"),
		GENDER("성별");

		private final String description;

		MemberField(String description) {
			this.description = description;
		}
	}
}
