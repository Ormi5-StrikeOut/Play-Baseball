package org.example.spring.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.review.dto.CreateReviewRequest;
import org.example.spring.domain.review.dto.GetMyReviewsResponse;
import org.example.spring.domain.review.dto.ModifyReviewRequest;
import org.example.spring.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 리뷰 관련 API 엔드포인트를 담당하는 컨트롤러 클래스입니다.
 * 새로운 리뷰 등록, 특정 리뷰의 내용 수정, 현재 로그인한 회원이 작성한 리뷰 목록 반환 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    /**
     * 새로운 리뷰를 등록합니다.
     *
     * @param member              현재 로그인한 회원 객체
     * @param createReviewRequest 신규 리뷰 작성 정보를 담은 DTO
     * @return                    새로운 리뷰 추가 성공 여부와 요청에 사용된 DTO를 포함한 HTTP 응답
     */
    @PostMapping
    public ResponseEntity<CreateReviewRequest> addReview(Member member, @Valid @RequestBody CreateReviewRequest createReviewRequest) {
        reviewService.addReview(member, createReviewRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createReviewRequest);
    }

    /**
     * 특정 리뷰의 내용을 수정합니다.
     *
     * @param id                  수정할 리뷰의 ID
     * @param member              현재 로그인한 회원 객체
     * @param modifyReviewRequest 수정할 리뷰 정보를 담은 DTO
     * @return                    리뷰 수정 성공을 나타내는 HTTP 상태 코드 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> modifyReview(@PathVariable Long id, Member member, @Valid @RequestBody ModifyReviewRequest modifyReviewRequest) {
        reviewService.modifyReview(id, member, modifyReviewRequest);
        return ResponseEntity.ok().build();
    }

    /**
     * 현재 로그인한 회원이 작성한 리뷰 목록을 반환합니다.
     *
     * @param member   현재 로그인한 회원 객체
     * @param pageable 페이지네이션 정보를 담고 있는 Pageable 객체.
     *                 기본값: 페이지 크기 10, 'createdAt' 필드 기준 내림차순 정렬.
     *                 사용자 정의 페이지네이션 매개변수가 제공되지 않을 경우 이 기본값이 적용됩니다.
     * @return         현재 로그인한 회원이 작성한 리뷰 목록을 담은 페이지화된 응답
     */
    @GetMapping("/my")
    public ResponseEntity<Page<GetMyReviewsResponse>> getMyReviews(Member member, @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
        Page<GetMyReviewsResponse> reviews = reviewService.getMyReviews(member, pageable);
        return ResponseEntity.ok(reviews);
    }
}
