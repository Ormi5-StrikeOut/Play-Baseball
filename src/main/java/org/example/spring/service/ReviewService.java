package org.example.spring.service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.review.Review;
import org.example.spring.domain.review.dto.CreateReviewRequest;
import org.example.spring.domain.review.dto.GetMyReviewsResponse;
import org.example.spring.domain.review.dto.ModifyReviewRequest;
import org.example.spring.domain.reviewImage.ReviewImage;
import org.example.spring.repository.ExchangeRepository;
import org.example.spring.repository.MemberRepository;
import org.example.spring.repository.ReviewImageRepository;
import org.example.spring.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * ReviewService는 리뷰 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 이 클래스는 리뷰의 생성, 수정, 조회 및 리뷰 이미지 관리 기능을 제공합니다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewService {
    private static final int MAX_IMAGES = 5;

    private final MemberRepository memberRepository;
    private final ExchangeRepository exchangeRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final FileUploadService fileUploadService;

    /**
     * 새로운 리뷰를 등록합니다.
     *
     * @param member                    리뷰를 작성하는 회원
     * @param createReviewRequest       새로운 리뷰 등록 요청 데이터
     * @param images                    리뷰에 첨부할 이미지 파일 목록
     * @return                          생성된 리뷰 엔티티
     * @throws IOException              파일 업로드 중 I/O 오류가 발생한 경우
     * @throws RuntimeException         교환 정보나 작성자 정보를 찾을 수 없는 경우
     * @throws IllegalArgumentException 이미지 개수가 최대 허용 개수를 초과한 경우
     */
    @Transactional
    public Review addReview(Member member, CreateReviewRequest createReviewRequest, List<MultipartFile> images) throws IOException {
        Exchange exchange = exchangeRepository.findById(createReviewRequest.getExchangeId()).orElseThrow(() -> new RuntimeException());
        Member writer = memberRepository.findById(createReviewRequest.getWriterId()).orElseThrow(() -> new RuntimeException());

        Review review = createReviewRequest.toEntity(exchange, writer);
        Review savedReview = reviewRepository.save(review);

        if(images != null && !images.isEmpty()) {
            if(images.size() > MAX_IMAGES) {
                throw new IllegalArgumentException("리뷰 이미지는 최대 " + MAX_IMAGES + "장까지 등록 가능합니다.");
            }

            List<String> imageUrls = fileUploadService.uploadFiles(images);

            for(String imageUrl : imageUrls) {
                ReviewImage reviewImage = ReviewImage.builder()
                    .review(savedReview)
                    .url(imageUrl)
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();
                reviewImageRepository.save(reviewImage);
            }
        }

        return review;
    }

    /**
     * 특정 리뷰의 내용을 수정합니다.
     *
     * @param reviewId                  수정할 리뷰의 ID
     * @param member                    리뷰를 수정하는 회원
     * @param request                   리뷰 수정 요청 데이터
     * @param images                    추가할 새로운 이미지 파일 목록
     * @throws IOException              파일 업로드 중 I/O 오류가 발생한 경우
     * @throws RuntimeException         리뷰를 찾을 수 없는 경우
     * @throws IllegalArgumentException 이미지 개수가 최대 허용 개수를 초과한 경우
     */
    @Transactional
    public void modifyReview(Long reviewId, Member member, ModifyReviewRequest request, List<MultipartFile> images) throws IOException {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException());

        if(request != null) {
            String content = request.getContent();
            if(content != null) {
                review.modifyContent(content);
            }
        }

        int reviewImageCount = reviewImageRepository.countByReview_Id(reviewId);

        if(images != null && !images.isEmpty()) {
            if((reviewImageCount + images.size()) > MAX_IMAGES) {
                throw new IllegalArgumentException("리뷰 이미지는 최대 " + MAX_IMAGES + "장까지 등록 가능합니다.");
            }

            List<String> imageUrls = fileUploadService.uploadFiles(images);

            for(String imageUrl : imageUrls) {
                ReviewImage reviewImage = ReviewImage.builder()
                    .review(review)
                    .url(imageUrl)
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .build();
                reviewImageRepository.save(reviewImage);
            }
        }
    }

    /**
     * 현재 로그인한 회원이 작성한 리뷰 목록을 페이징 처리하여 조회합니다.
     *
     * @param member   리뷰를 조회할 회원
     * @param pageable 페이징 정보
     * @return         회원의 리뷰 목록 (GetMyReviewsResponse의 Page 객체)
     */
    public Page<GetMyReviewsResponse> getMyReviews(Member member, Pageable pageable) {
        return reviewRepository.findByWriter_Id(member.getId(), pageable).map(GetMyReviewsResponse::from);
    }

    /**
     * 특정 리뷰의 이미지를 삭제합니다.
     *
     * @param reviewId          이미지가 속한 리뷰의 ID
     * @param imageId           삭제할 이미지의 ID
     * @throws IOException      파일 삭제 중 I/O 오류가 발생한 경우
     * @throws RuntimeException 이미지를 찾을 수 없는 경우
     */
    @Transactional
    public void deleteReviewImage(Long reviewId, Long imageId) throws IOException {
        ReviewImage image = reviewImageRepository.findById(imageId).orElseThrow(() -> new RuntimeException("Image not found"));

        fileUploadService.deleteFile(image.getUrl());
        reviewImageRepository.delete(image);
    }
}
