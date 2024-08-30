package org.example.spring.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.example.spring.constants.Gender;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.MemberRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ExchangeRepositoryTest {

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    @BeforeEach
    void setUp() {
        // Given
        testMember = Member.builder()
                .name("User")
                .email("testuser@example.com")
                .nickname("testuser")
                .password("password123")
                .phoneNumber("010-0000-0000")
                .gender(Gender.MALE)
                .role(MemberRole.USER)
                .build();
        memberRepository.save(testMember);

        for (int i = 0; i < 10; i++) {
            Exchange exchange = Exchange.builder()
                    .title("Title " + i)
                    .price(1000 + i)
                    .regularPrice(1200 + i)
                    .content("Test Content " + i)
                    .viewCount(i)
                    .createdAt(Timestamp.from(Instant.now()))
                    .writer(testMember)
                    .build();
            exchangeRepository.save(exchange);
        }
    }

    @Test
    @DisplayName("최근 5개의 게시글 조회")
    void findTop5ByOrderByCreatedAtDesc() {
        // When
        List<Exchange> latestExchanges = exchangeRepository.findTop5ByOrderByCreatedAtDesc();

        // Then
        assertThat(latestExchanges).hasSize(5);
        assertThat(latestExchanges.get(0).getTitle()).isEqualTo("Title 9");
        assertThat(latestExchanges.get(4).getTitle()).isEqualTo("Title 5");
    }

    @Test
    @DisplayName("특정 회원이 작성한 게시글 조회")
    void findByWriterId() {
        // When
        List<Exchange> memberExchanges = exchangeRepository.findByWriterId(testMember.getId());

        // Then
        assertThat(memberExchanges).hasSize(10);
        assertThat(memberExchanges.get(0).getWriter().getId()).isEqualTo(testMember.getId());
    }
}