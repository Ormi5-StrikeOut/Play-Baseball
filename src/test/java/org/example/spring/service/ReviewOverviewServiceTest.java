/*
package org.example.spring.service;

import static jdk.internal.org.jline.reader.impl.LineReaderImpl.CompletionType.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.example.spring.constants.Gender;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.MemberRole;
import org.example.spring.domain.review.Review;
import org.example.spring.domain.reviewOverview.ReviewOverview;
import org.example.spring.repository.ExchangeRepository;
import org.example.spring.repository.MemberRepository;
import org.example.spring.repository.ReviewOverviewRepository;
import org.example.spring.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class ReviewOverviewServiceTest {
    @InjectMocks
    private ReviewOverviewService reviewOverviewService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ExchangeRepository exchangeRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewOverviewRepository reviewOverviewRepository;

    private Member testMember1;
    private Member testMember2;
    private Exchange testExchange;
    private Review testReview;
    private ReviewOverview reviewOverview;

    @BeforeEach
    void setUp() {
        testMember1 = Member.builder()
            .email("t1@example.com")
            .password("password")
            .nickname("t1")
            .name("t1")
            .phoneNumber("010-7777-7777")
            .gender(Gender.MALE)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .role(MemberRole.USER)
            .build();

        testMember2 = Member.builder()
            .email("t2@example.com")
            .password("password")
            .nickname("t2")
            .name("t2")
            .phoneNumber("010-8888-8888")
            .gender(Gender.MALE)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .role(MemberRole.USER)
            .build();

        testExchange = Exchange.builder()
            .member(testMember1)
            .title("테스트 중고 거래 게시물 1")
            .price(18000)
            .regularPrice(20000)
            .content("테스트")
            .viewCount(0)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();

        testReview = Review.builder()
            .exchange(testExchange)
            .writer(testMember2)
            .content("좋습니다.")
            .rate(5)
            .isSecret(false)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();

        reviewOverview = ReviewOverview.builder()
            .id(1L)
            .member(testMember1)
            .count(10)
            .average(4.0)
            .build();
    }

    @Test
    void updateReviewOverviews() {
        // Given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        LocalDateTime startOfYesterday = yesterday.atStartOfDay();
        LocalDateTime endOfYesterday = today.atStartOfDay().minusNanos(1);

        Timestamp startOfYesterdayTimestamp = Timestamp.valueOf(startOfYesterday);
        Timestamp endOfYesterdayTimestamp = Timestamp.valueOf(endOfYesterday);

        // 설정: 최근 리뷰된 거래를 조회
        when(exchangeRepository.findByReviewedAtBetween(startOfYesterdayTimestamp, endOfYesterdayTimestamp))
            .thenReturn(List.of(testExchange));

        // 설정: 특정 거래에 대한 리뷰 조회
        when(reviewRepository.findByExchange_Id(testExchange.getId()))
            .thenReturn(testReview);

        // 설정: 멤버 조회
        when(memberRepository.findById(testMember1.getId()))
            .thenReturn(java.util.Optional.of(testMember1));

        // 설정: ReviewOverview 조회 및 반환
        when(reviewOverviewRepository.findByMemberId(testMember1.getId()))
            .thenReturn(java.util.Optional.of(reviewOverview));

        // When
        reviewOverviewService.updateReviewOverviews();

        // Then
        verify(reviewOverviewRepository, times(1)).findByMemberId(testMember1.getId());
        verify(reviewOverviewRepository, times(1)).save(any(ReviewOverview.class));

        // Validate the updated values
        ReviewOverview updatedReviewOverview = reviewOverviewRepository.findByMemberId(testMember1.getId()).orElseThrow();

        // Calculate the expected average
        double expectedAverage = (4.0 * 10 + 5) / 11; // (oldAverage * oldCount + newRate) / newCount
        assertEquals(11, updatedReviewOverview.getCount()); // Updated count should be 11
        assertEquals(expectedAverage, updatedReviewOverview.getAverage(), 0.001); // Updated average with one review with rate 5
    }
    }
}*/
