package org.example.spring.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.example.spring.constants.Gender;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.MemberRole;
import org.example.spring.domain.review.Review;
import org.example.spring.domain.review.dto.CreateReviewRequest;
import org.example.spring.domain.review.dto.GetMyReviewsResponse;
import org.example.spring.domain.review.dto.ModifyReviewRequest;
import org.example.spring.repository.ExchangeRepository;
import org.example.spring.repository.MemberRepository;
import org.example.spring.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ExchangeRepository exchangeRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

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

        testExchange1 = Exchange.builder()
            .writer(testMember)
            .title("테스트 중고 거래 게시물 1")
            .price(18000)
            .regularPrice(20000)
            .content("테스트")
            .viewCount(0)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();

        testExchange2 = Exchange.builder()
            .writer(testMember)
            .title("테스트 중고 거래 게시물 2")
            .price(36000)
            .regularPrice(40000)
            .content("테스트")
            .viewCount(0)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();
    }

    /*@Test
    @DisplayName("존재하는 Exchange와 Member로 리뷰 작성 시 성공하는 테스트")
    void addReview() {
        // Given
        CreateReviewRequest createReviewRequest = CreateReviewRequest.builder()
            .content("테스트")
            .rate(3)
            .isSecret(false)
            .build();

        when(exchangeRepository.findById(testExchange1.getId())).thenReturn(Optional.of(testExchange1));
        when(memberRepository.findById(testMember.getId())).thenReturn(Optional.of(testMember));

        Review newReview = Review.builder()
            .exchange(testExchange1)
            .writer(testMember)
            .content(createReviewRequest.getContent())
            .rate(createReviewRequest.getRate())
            .isSecret(createReviewRequest.isSecret())
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();

        when(reviewRepository.save(any(Review.class))).thenReturn(newReview);

        // When
        Review result = reviewService.addReview(testMember, createReviewRequest);

        // Then
        assertNotNull(result);
        assertEquals(newReview.getId(), result.getId());
        assertEquals(testExchange1, result.getExchange());
        assertEquals(testMember, result.getWriter());
        assertEquals(createReviewRequest.getContent(), result.getContent());
        assertEquals(createReviewRequest.getRate(), result.getRate());
        assertEquals(createReviewRequest.isSecret(), result.isSecret());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    @DisplayName("존재하는 Exchange에 작성한 리뷰 수정 시 성공하는 테스트")
    void modifyReview() {
        // Given
        ModifyReviewRequest modifyReviewRequest = ModifyReviewRequest.builder()
            .content("수정된 리뷰 내용")
            .build();

        Review existReview = Review.builder()
            .exchange(testExchange1)
            .writer(testMember)
            .content("기존 리뷰 내용")
            .rate(3)
            .isSecret(false)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();

        when(reviewRepository.findById(existReview.getId())).thenReturn(Optional.of(existReview));

        // When
        reviewService.modifyReview(existReview.getId(), testMember, modifyReviewRequest);

        // Then
        Review updatedReview = reviewRepository.findById(existReview.getId()).orElseThrow();
        assertEquals(modifyReviewRequest.getContent(), updatedReview.getContent());
    }

    @Test
    @DisplayName("내가 작성한 리뷰 가져오기 성공하는 테스트")
    void getMyReviews() {
        // Given
        CreateReviewRequest createReviewRequest = CreateReviewRequest.builder()
            .content("테스트")
            .rate(3)
            .isSecret(false)
            .build();

        Review newReview1 = Review.builder()
            .exchange(testExchange1)
            .writer(testMember)
            .content(createReviewRequest.getContent())
            .rate(createReviewRequest.getRate())
            .isSecret(createReviewRequest.isSecret())
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();

        Review newReview2 = Review.builder()
            .exchange(testExchange2)
            .writer(testMember)
            .content(createReviewRequest.getContent())
            .rate(createReviewRequest.getRate())
            .isSecret(createReviewRequest.isSecret())
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();

        List<Review> newReviews = Arrays.asList(newReview1, newReview2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Review> reviewPage = new PageImpl<>(newReviews, pageable, newReviews.size());
        when(reviewRepository.findByWriter_Id(testMember.getId(), pageable)).thenReturn(reviewPage);

        // When
        Page<GetMyReviewsResponse> result = reviewService.getMyReviews(testMember, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("테스트", result.getContent().get(0).getContent());
        assertEquals("테스트", result.getContent().get(1).getContent());

        verify(reviewRepository, times(1)).findByWriter_Id(testMember.getId(), pageable);
    }
     */
}
