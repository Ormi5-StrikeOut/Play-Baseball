package org.example.spring.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.transaction.Transactional;
import java.util.Optional;
import org.example.spring.constants.Gender;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.MemberRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = Member.builder().name("testMember").email("test@example.com")
            .nickname("testNick")
            .password("password123!@#").phoneNumber("010-0001-0000").gender(
                Gender.MALE)
            .role(MemberRole.USER)
            .build();
    }

    @Test
    @DisplayName("멤버가 DB에 저잘이 잘 되는지 확인")
    void saveMember() {

        Member savedMember = memberRepository.save(testMember);


        assertThat(savedMember).isNotNull();
        assertThat(savedMember.getId()).isNotNull();
        assertThat(savedMember.getName()).isEqualTo(testMember.getName());
        assertThat(savedMember.getEmail()).isEqualTo(testMember.getEmail());
        assertThat(savedMember.getNickname()).isEqualTo(testMember.getNickname());
        assertThat(savedMember.getPassword()).isEqualTo(testMember.getPassword());
        assertThat(savedMember.getPhoneNumber()).isEqualTo(testMember.getPhoneNumber());
        assertThat(savedMember.getGender()).isEqualTo(testMember.getGender());
        assertThat(savedMember.getRole()).isEqualTo(testMember.getRole());
    }

    @Test
    @DisplayName("id로 멤버 찾기")
    void findById() {

        Member savedMember = memberRepository.save(testMember);

        Optional<Member> memberById = memberRepository.findById(savedMember.getId());

        assertThat(memberById).isPresent(); // 값이 존재 하는지 확인
        assertThat(memberById.get().getEmail()).isEqualTo(testMember.getEmail());

    }
    @Test
    @DisplayName("이메일 존재 여부 확인")
    void existsByEmail() {
        memberRepository.save(testMember);

        boolean exists = memberRepository.existsByEmail(testMember.getEmail());
        assertThat(exists).isTrue();

        boolean notExists = memberRepository.existsByEmail("nonexistent@example.com");
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("닉네임 존재 여부 확인")
    void existsByNickname() {
        memberRepository.save(testMember);

        boolean exists = memberRepository.existsByNickname(testMember.getNickname());
        assertThat(exists).isTrue();

        boolean notExists = memberRepository.existsByNickname("nonexistentNick");
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("전화번호 존재 여부 확인")
    void existsByPhoneNumber() {
        memberRepository.save(testMember);

        boolean exists = memberRepository.existsByPhoneNumber(testMember.getPhoneNumber());
        assertThat(exists).isTrue();

        boolean notExists = memberRepository.existsByPhoneNumber("010-9999-9999");
        assertThat(notExists).isFalse();
    }

}