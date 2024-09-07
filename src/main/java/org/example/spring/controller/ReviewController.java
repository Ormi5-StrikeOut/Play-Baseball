package org.example.spring.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.spring.domain.review.dto.CreateReviewRequest;
import org.example.spring.domain.review.dto.GetMyReviewsResponse;
import org.example.spring.domain.review.dto.ModifyReviewRequest;
import org.example.spring.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * ReviewController는 리뷰 관련 API 엔드포인트를 처리하는 REST 컨트롤러입니다.
 * 이 컨트롤러는 리뷰의 생성, 수정, 조회 및 리뷰 이미지 삭제 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    /**
     * 새로운 리뷰를 등록합니다.
     *
     * @param request             현재 HTTP 요청 객체
     * @param createReviewRequest 신규 리뷰 작성 정보를 담은 DTO
     * @param images              리뷰에 첨부할 이미지 파일 목록 (선택사항)
     * @return                    새로운 리뷰 추가 성공 여부와 요청에 사용된 DTO를 포함한 HTTP 응답
     * @throws IOException        파일 업로드 중 I/O 오류가 발생한 경우
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateReviewRequest> addReview(HttpServletRequest request, @RequestPart("review") CreateReviewRequest createReviewRequest, @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        reviewService.addReview(request, createReviewRequest, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(createReviewRequest);
    }

    /**
     * 특정 리뷰의 내용을 수정합니다.
     *
     * @param id                  수정할 리뷰의 ID
     * @param request             현재 HTTP 요청 객체
     * @param modifyReviewRequest 수정할 리뷰 정보를 담은 DTO (선택사항)
     * @param images              추가할 새로운 이미지 파일 목록 (선택사항)
     * @return                    리뷰 수정 성공을 나타내는 HTTP 상태 코드 200 OK
     * @throws IOException        파일 업로드 중 I/O 오류가 발생한 경우
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> modifyReview(@PathVariable Long id, HttpServletRequest request, @Valid @RequestPart(value = "review", required = false) ModifyReviewRequest modifyReviewRequest, @RequestPart(value = "images", required = false) List<MultipartFile> images) throws IOException {
        reviewService.modifyReview(id, request, modifyReviewRequest, images);
        return ResponseEntity.ok().build();
    }

    /**
     * 현재 로그인한 회원이 작성한 리뷰 목록을 페이징 처리하여 반환합니다.
     *
     * @param request  현재 HTTP 요청 객체
     * @param pageable 페이지네이션 정보를 담고 있는 Pageable 객체.
     *                 기본값: 페이지 크기 10, 'createdAt' 필드 기준 내림차순 정렬.
     * @return         현재 로그인한 회원이 작성한 리뷰 목록을 담은 페이지화된 응답
     */
    @GetMapping("/my")
    public ResponseEntity<Page<GetMyReviewsResponse>> getMyReviews(HttpServletRequest request, @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
        Page<GetMyReviewsResponse> reviews = reviewService.getMyReviews(request, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 특정 리뷰의 이미지를 삭제합니다.
     *
     * @param reviewId     이미지가 속한 리뷰의 ID
     * @param imageId      삭제할 이미지의 ID
     * @param request      현재 HTTP 요청 객체
     * @return             이미지 삭제 성공을 나타내는 HTTP 상태 코드 200 OK
     * @throws IOException 파일 삭제 중 I/O 오류가 발생한 경우
     */
    @DeleteMapping("/{reviewId}/images/{imageId}")
    public ResponseEntity<Void> deleteReviewImage(@PathVariable Long reviewId, @PathVariable Long imageId, HttpServletRequest request) throws IOException {
        reviewService.deleteReviewImage(reviewId, imageId, request);
        return ResponseEntity.ok().build();
    }
}
