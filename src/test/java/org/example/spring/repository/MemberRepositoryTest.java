package org.example.spring.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import jakarta.transaction.Transactional;
import java.util.List;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = Replace.NONE)
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        long timestamp = System.currentTimeMillis();
        String uniqueEmail = "test" + timestamp + "@example.com";
        String uniqueNickname = "testNick" + timestamp;
        String uniquePhoneNumber = "010-" + timestamp;
        testMember = Member.builder()
            .name("test99Member")
            .email(uniqueEmail)
            .nickname(uniqueNickname)
            .password("password123!@#")
            .phoneNumber(uniquePhoneNumber)
            .gender(Gender.MALE)
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

        boolean notExists = memberRepository.existsByPhoneNumber("123-9999-9999");
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("회원 전체 조회 테스트")
    void findAll() {
        // given
        Member member1 = Member.builder()
            .name("Member 1")
            .email("member1@example.com")
            .nickname("member1")
            .password("password123!")
            .phoneNumber("010-1234-5678")
            .gender(Gender.MALE)
            .role(MemberRole.USER)
            .build();
        Member member2 = Member.builder()
            .name("Member 2")
            .email("member2@example.com")
            .nickname("member2")
            .password("password123!")
            .phoneNumber("010-2345-6789")
            .gender(Gender.FEMALE)
            .role(MemberRole.USER)
            .build();
        Member member3 = Member.builder()
            .name("Member 3")
            .email("member3@example.com")
            .nickname("member3")
            .password("password123!")
            .phoneNumber("010-3456-7890")
            .gender(Gender.MALE)
            .role(MemberRole.USER)
            .build();
        memberRepository.saveAll(List.of(member1, member2, member3));

        // when
        Page<Member> result = memberRepository.findAll(PageRequest.of(0, 10, Sort.by(Direction.DESC, "createdAt"))); // 페이지 크기를 10으로 설정

        // then
        assertThat(result.getContent())
            .extracting("name", "email", "nickname", "gender", "role")
            .containsAnyOf(
                tuple("Member 1", "member1@example.com", "member1", Gender.MALE, MemberRole.USER),
                tuple("Member 2", "member2@example.com", "member2", Gender.FEMALE, MemberRole.USER),
                tuple("Member 3", "member3@example.com", "member3", Gender.MALE, MemberRole.USER)
            );
    }

}