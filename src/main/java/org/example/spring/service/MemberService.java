package org.example.spring.service;

import jakarta.transaction.Transactional;
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

    @Transactional
    public Member registerMember(MemberJoinRequestDto memberJoinRequestDto) {
        String hashedPassword = passwordEncoder.encode(memberJoinRequestDto.getPassword());

        Member member = Member.builder()
            .email(memberJoinRequestDto.getEmail())
            .password(hashedPassword)
            .nickname(memberJoinRequestDto.getNickname())
            .name(memberJoinRequestDto.getName())
            .phoneNumber(memberJoinRequestDto.getPhoneNumber())
            .gender(memberJoinRequestDto.getGender())
            .build();

        return memberRepository.save(member);
    }
}
