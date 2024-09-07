package org.example.spring.service;

import static org.example.spring.domain.member.dto.MemberResponseDto.toDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.MemberRole;
import org.example.spring.domain.member.dto.MemberJoinRequestDto;
import org.example.spring.domain.member.dto.MemberModifyRequestDto;
import org.example.spring.domain.member.dto.MemberResponseDto;
import org.example.spring.domain.member.dto.MemberRoleModifyRequestDto;
import org.example.spring.exception.ResourceNotFoundException;
import org.example.spring.repository.MemberRepository;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 관련 서비스 로직을 담당하는 클래스입니다. 회원 가입, 회원 목록 조회 등의 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenValidator jwtValidator;

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
     * JWT 토큰에서 추출된 회원의 탈퇴(soft delete)
     *
     * @param request HttpServletRequest
     */
    public void deleteMember(HttpServletRequest request, HttpServletResponse response) {
        jwtValidator.deactivateAccount(request, response);
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

        return toDto(saveMember);
    }

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
     * 요청온 Request 에서 추출한 email 을 가지고 있는 회원을 modify 합니다.
     *
     * @param request 요청
     * @param memberModifyRequestDto 회원 정보 수정 요청 Dto
     * @return 수정된 회원 응답 Dto
     */
    public MemberResponseDto modifyMember(HttpServletRequest request, MemberModifyRequestDto memberModifyRequestDto) {
        Member member = getMemberByToken(request);
        member.updateFrom(memberModifyRequestDto);
        Member modifiedMember = memberRepository.save(member);
        return MemberResponseDto.toDto(modifiedMember);
    }

    private Member getMemberByToken(HttpServletRequest request) {
        String token = jwtValidator.extractTokenFromHeader(request);
        String email = jwtValidator.extractUsername(token);
        return memberRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Member", "email", email));
    }

    /**
     * admin 권한을 가지고 있으면 회원의 권한을 수정할 수 있습니다.
     * @param memberId 수정할 회원 ID
     * @param memberRoleModifyRequestDto 변경할 권한 요청
     * @param request HttpServletRequest
     * @return 권한이 변경된 회원 Dto
     */
    public MemberResponseDto modifyMemberRole(Long memberId, MemberRoleModifyRequestDto memberRoleModifyRequestDto, HttpServletRequest request) {
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
