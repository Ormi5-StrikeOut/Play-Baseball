package org.example.spring.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.transaction.Transactional;
import java.sql.Timestamp;
import org.example.spring.constants.Gender;
import org.example.spring.constants.SalesStatus;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.MemberRole;
import org.example.spring.domain.review.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Transactional
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ReviewRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ExchangeRepository exchangeRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private Member testMember;
    private Exchange testExchange1;
    private Exchange testExchange2;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .email("test@example.com")
            .password("password")
            .nickname("nickname")
            .name("name")
            .phoneNumber("010-1234-5678")
            .gender(Gender.MALE)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .role(MemberRole.USER)
            .build();
        memberRepository.save(testMember);

        testExchange1 = Exchange.builder()
            .member(testMember)
            .title("테스트 중고 거래 게시물 1")
            .price(18000)
            .regularPrice(20000)
            .content("테스트")
            .viewCount(0)
            .status(SalesStatus.COMPLETE)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();
        exchangeRepository.save(testExchange1);

        testExchange2 = Exchange.builder()
            .member(testMember)
            .title("테스트 중고 거래 게시물 2")
            .price(36000)
            .regularPrice(40000)
            .content("테스트")
            .viewCount(0)
            .status(SalesStatus.COMPLETE)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();
        exchangeRepository.save(testExchange2);
    }

    /*@Test
    @DisplayName("작성자 ID로 조회 테스트")
    void findByWriterId() {
        // Given
        Review testReview1 = Review.builder()
            .exchange(testExchange1)
            .writer(testMember)
            .content("좋은 거래였습니다.")
            .rate(4)
            .isSecret(false)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();
        reviewRepository.save(testReview1);

        Review testReview2 = Review.builder()
            .exchange(testExchange2)
            .writer(testMember)
            .content("최악이에요")
            .rate(1)
            .isSecret(true)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();
        reviewRepository.save(testReview2);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Review> reviews = reviewRepository.findByWriter_Id(testMember.getId(), pageable);

        // then
        assertThat(reviews.getTotalElements()).isEqualTo(2);
        assertThat(reviews.getContent()).hasSize(2);
        assertThat(reviews.getContent().get(0).getContent()).isEqualTo("좋은 거래였습니다.");
        assertThat(reviews.getContent().get(1).getContent()).isEqualTo("최악이에요");
    }*/
}
