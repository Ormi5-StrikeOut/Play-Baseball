package org.example.spring.service;

import lombok.RequiredArgsConstructor;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.review.Review;
import org.example.spring.domain.review.dto.CreateReviewRequest;
import org.example.spring.domain.review.dto.GetMyReviewsResponse;
import org.example.spring.domain.review.dto.ModifyReviewRequest;
import org.example.spring.repository.ExchangeRepository;
import org.example.spring.repository.MemberRepository;
import org.example.spring.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 리뷰 관련 비즈니스 로직을 담당하는 서비스 클래스입니다.
 * 새로운 리뷰 등록, 특정 리뷰의 내용 수정, 현재 로그인한 회원이 작성한 리뷰 목록 반환 기능을 제공합니다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {
    private final MemberRepository memberRepository;
    private final ExchangeRepository exchangeRepository;
    private final ReviewRepository reviewRepository;

    /**
     * 새로운 리뷰를 등록합니다.
     *
     * @param member              리뷰를 작성한 회원
     * @param createReviewRequest 새로운 리뷰 등록 요청 데이터
     * @return 생성된 리뷰 엔티티
     */
    @Transactional
    public Review addReview(Member member, CreateReviewRequest createReviewRequest) {
        Exchange exchange = exchangeRepository.findById(createReviewRequest.getExchangeId()).orElseThrow(() -> new RuntimeException());
        Member writer = memberRepository.findById(createReviewRequest.getWriterId()).orElseThrow(() -> new RuntimeException());
        Review review = createReviewRequest.toEntity(exchange, writer);
        return reviewRepository.save(review);
    }

    /**
     * 특정 리뷰의 내용을 수정합니다.
     *
     * @param reviewId 수정할 리뷰의 ID
     * @param member   리뷰를 작성한 회원
     * @param request  리뷰 수정 요청 데이터
     */
    @Transactional
    public void modifyReview(Long reviewId, Member member, ModifyReviewRequest request) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException());
        review.modifyContent(request.getContent());
    }

    /**
     * 현재 로그인한 회원이 작성한 리뷰 목록을 페이징 처리하여 조회합니다.
     *
     * @param member   리뷰를 조회할 회원
     * @param pageable 페이징 정보
     * @return         회원의 리뷰 목록 (GetMyReviewsResponse)
     */
    public Page<GetMyReviewsResponse> getMyReviews(Member member, Pageable pageable) {
        return reviewRepository.findByWriter_Id(member.getId(), pageable).map(GetMyReviewsResponse::from);
    }
}
