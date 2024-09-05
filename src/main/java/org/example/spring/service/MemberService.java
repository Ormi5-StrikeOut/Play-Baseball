package org.example.spring.service;

import static org.example.spring.domain.member.dto.MemberJoinRequestDto.toEntity;
import static org.example.spring.domain.member.dto.MemberResponseDto.toDto;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.dto.MemberJoinRequestDto;
import org.example.spring.domain.member.dto.MemberResponseDto;
import org.example.spring.exception.ResourceNotFoundException;
import org.example.spring.repository.MemberRepository;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 관련 서비스 로직을 담당하는 클래스입니다. 회원 가입, 회원 목록 조회 등의 기능을 제공합니다.
 */
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

    @Transactional(readOnly = true)
    public MemberResponseDto getMyMember(HttpServletRequest request) {
        String token = jwtValidator.extractTokenFromHeader(request);
        UserDetails userDetails = jwtValidator.getUserDetails(token);
        String email = jwtValidator.extractUsername(token);
        if (userDetails.getUsername().equals(email)) {
            Member member = memberRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Member", "email", email));
            return MemberResponseDto.toDto(member);
        }
        return null;
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

        Member member = toEntity(memberJoinRequestDto, hashedPassword);

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
