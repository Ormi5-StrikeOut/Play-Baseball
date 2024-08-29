package org.example.spring.service;

import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.dto.MemberJoinRequestDto;
import org.example.spring.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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

    @Transactional
    public Member registerMember(MemberJoinRequestDto memberJoinRequestDto) {
        validateMemberDto(memberJoinRequestDto);
        checkDuplicates(memberJoinRequestDto);

        String hashedPassword = passwordEncoder.encode(memberJoinRequestDto.getPassword());

        Member member = toEntity(memberJoinRequestDto, hashedPassword);

        return memberRepository.save(member);
    }

    private Member toEntity(MemberJoinRequestDto memberJoinRequestDto,
        String hashedPassword) {
        return Member.builder()
            .email(memberJoinRequestDto.getEmail())
            .password(hashedPassword)
            .nickname(memberJoinRequestDto.getNickname())
            .name(memberJoinRequestDto.getName())
            .phoneNumber(memberJoinRequestDto.getPhoneNumber())
            .gender(memberJoinRequestDto.getGender())
            .build();
    }

    private void validateMemberDto(MemberJoinRequestDto memberJoinRequestDto) {
        if (memberJoinRequestDto == null) {
            throw new IllegalArgumentException("회원 정보가 null입니다.");
        }
        validateField(memberJoinRequestDto.getEmail(), MemberField.EMAIL);
        validateField(memberJoinRequestDto.getPassword(), MemberField.PASSWORD);
        validateField(memberJoinRequestDto.getNickname(), MemberField.NICKNAME);
        validateField(memberJoinRequestDto.getName(), MemberField.NAME);
        validateField(memberJoinRequestDto.getPhoneNumber(), MemberField.PHONE_NUMBER);
        validateField(memberJoinRequestDto.getGender().name(), MemberField.GENDER);
    }

    private void validateField(String field, MemberField fieldName) {
        if (field == null || field.isEmpty()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수 입력 항목입니다.");
        }

        switch (fieldName) {
            case EMAIL:
                validateEmail(field);
                break;
            case PASSWORD:
                validatePassword(field);
                break;
            case NICKNAME:
                validateNickname(field);
                break;
            case PHONE_NUMBER:
                validatePhoneNumber(field);
                break;
        }
    }

    private void validateEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("유효한 이메일 주소를 입력해주세요.");
        }
    }

    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 최소 8자 이상이어야 합니다.");
        }
        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$";
        if (!password.matches(passwordRegex)) {
            throw new IllegalArgumentException("비밀번호는 문자, 숫자, 특수문자를 포함해야 합니다.");
        }
    }

    private void validateNickname(String nickname) {
        if (nickname.length() < 2 || nickname.length() > 20) {
            throw new IllegalArgumentException("닉네임은 2자에서 20자 사이여야 합니다.");
        }
    }

    private void validatePhoneNumber(String phoneNumber) {
        String phoneRegex = "^\\d{2,3}-\\d{3,4}-\\d{4}$";
        if (!phoneNumber.matches(phoneRegex)) {
            throw new IllegalArgumentException("올바른 전화번호 형식이 아닙니다. (예: 010-1234-5678)");
        }
    }

    private void checkDuplicates(MemberJoinRequestDto memberJoinRequestDto) {
        if (memberRepository.existsByEmail(memberJoinRequestDto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        if (memberRepository.existsByNickname(memberJoinRequestDto.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        if (memberRepository.existsByPhoneNumber(memberJoinRequestDto.getPhoneNumber())) {
            throw new IllegalArgumentException("이미 존재하는 전화번호입니다.");
        }
    }
}