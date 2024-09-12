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
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("dev") // push 전에 지우기
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

    private Member member1;
    private Member member2;
    private Exchange exchange1;
    private Exchange exchange2;
    private Review review1;
    private Review review2;

    @BeforeEach
    void setUp() {
        member1 = Member.builder()
            .email("member1@example.com")
            .password("Password7!")
            .nickname("member1")
            .name("member1")
            .phoneNumber("010-8948-6414")
            .gender(Gender.MALE)
            .role(MemberRole.USER)
            .build();
        memberRepository.save(member1);

        member2 = Member.builder()
            .email("member2@example.com")
            .password("Password7!")
            .nickname("member2")
            .name("member2")
            .phoneNumber("010-8948-6415")
            .gender(Gender.MALE)
            .role(MemberRole.USER)
            .build();
        memberRepository.save(member2);

        exchange1 = Exchange.builder()
            .member(member1)
            .title("테스트 중고 거래 게시물 1")
            .price(18000)
            .regularPrice(20000)
            .content("테스트 중고 거래 게시물 1")
            .viewCount(0)
            .status(SalesStatus.SALE)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();
        exchangeRepository.save(exchange1);

        exchange2 = Exchange.builder()
            .member(member1)
            .title("테스트 중고 거래 게시물 2")
            .price(36000)
            .regularPrice(40000)
            .content("테스트 중고 거래 게시물 2")
            .viewCount(0)
            .status(SalesStatus.SALE)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();
        exchangeRepository.save(exchange2);

        review1 = Review.builder()
            .exchange(exchange1)
            .writer(member2)
            .content("좋은 거래였습니다.")
            .rate(4)
            .isSecret(false)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();
        reviewRepository.save(review1);

        review2 = Review.builder()
            .exchange(exchange2)
            .writer(member2)
            .content("최악이에요")
            .rate(1)
            .isSecret(true)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();
        reviewRepository.save(review2);
    }

    @Test
    @DisplayName("작성자 ID로 조회 테스트")
    void findByWriterId() {

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Review> reviews = reviewRepository.findByWriter_Id(member2.getId(), pageable);

        // then
        assertThat(reviews.getTotalElements()).isEqualTo(2);
        assertThat(reviews.getContent()).hasSize(2);
        assertThat(reviews.getContent().get(0).getContent()).isEqualTo("좋은 거래였습니다.");
        assertThat(reviews.getContent().get(1).getContent()).isEqualTo("최악이에요");
    }

    @Test
    @DisplayName("중고 거래 게시물 ID로 조회 테스트")
    void findByExchange_Id() {

        // when
        Review reviewForExchange1 = reviewRepository.findByExchange_Id(exchange1.getId());
        Review reviewForExchange2 = reviewRepository.findByExchange_Id(exchange2.getId());

        // then
        assertThat(reviewForExchange1).isNotNull();
        assertThat(reviewForExchange1.getContent()).isEqualTo("좋은 거래였습니다.");

        assertThat(reviewForExchange2).isNotNull();
        assertThat(reviewForExchange2.getContent()).isEqualTo("최악이에요");

    }
}
