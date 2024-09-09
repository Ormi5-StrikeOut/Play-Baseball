package org.example.spring.service;

import jakarta.servlet.http.HttpServletRequest;
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
import org.example.spring.security.jwt.JwtTokenValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * ReviewService는 리뷰 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 이 클래스는 리뷰의 생성, 수정, 조회 및 리뷰 이미지 관리 기능을 제공합니다.
 * JWT 토큰을 통한 사용자 인증을 포함하고 있습니다.
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
    private final JwtTokenValidator jwtTokenValidator;

    /**
     * HTTP 요청으로부터 인증된 사용자 정보를 추출합니다.
     *
     * @param request           HTTP 요청 객체
     * @return                  인증된 Member 객체
     * @throws RuntimeException 토큰이 유효하지 않거나 사용자를 찾을 수 없는 경우
     */
    private Member getAuthenticatedMember(HttpServletRequest request) {
        String token = jwtTokenValidator.extractTokenFromHeader(request);
        return jwtTokenValidator.validateTokenAndGetMember(token);
    }

    /**
     * 새로운 리뷰를 등록합니다.
     *
     * @param request                   HTTP 요청 객체 (인증된 사용자 정보 추출용)
     * @param createReviewRequest       새로운 리뷰 등록 요청 데이터
     * @param images                    리뷰에 첨부할 이미지 파일 목록 (최대 5개)
     * @return                          생성된 리뷰 엔티티
     * @throws IOException              파일 업로드 중 I/O 오류가 발생한 경우
     * @throws RuntimeException         교환 정보를 찾을 수 없는 경우
     * @throws IllegalArgumentException 이미지 개수가 최대 허용 개수(5개)를 초과한 경우
     */
    @Transactional
    public Review addReview(HttpServletRequest request, CreateReviewRequest createReviewRequest, List<MultipartFile> images) throws IOException {
        Member member = getAuthenticatedMember(request);

        Exchange exchange = exchangeRepository.findById(createReviewRequest.getExchangeId()).orElseThrow(() -> new RuntimeException());

        Review review = createReviewRequest.toEntity(exchange, member);
        Review savedReview = reviewRepository.save(review);

        exchange.markAsReviewed();
        exchangeRepository.save(exchange);

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
     * @param request                   HTTP 요청 객체 (인증된 사용자 정보 추출용)
     * @param modifyReviewRequest       리뷰 수정 요청 데이터
     * @param images                    추가할 새로운 이미지 파일 목록
     * @throws IOException              파일 업로드 중 I/O 오류가 발생한 경우
     * @throws RuntimeException         리뷰를 찾을 수 없는 경우
     * @throws IllegalArgumentException 이미지 개수가 최대 허용 개수(5개)를 초과한 경우
     * @throws SecurityException        리뷰 작성자와 현재 인증된 사용자가 일치하지 않는 경우
     */
    @Transactional
    public void modifyReview(Long reviewId, HttpServletRequest request, ModifyReviewRequest modifyReviewRequest, List<MultipartFile> images) throws IOException {
        Member member = getAuthenticatedMember(request);

        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException());

        if (!review.getWriter().getId().equals(member.getId())) {
            throw new SecurityException("리뷰 작성자 정보와 일치하지 않은 사용자입니다..");
        }

        if(modifyReviewRequest != null) {
            String content = modifyReviewRequest.getContent();
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
     * @param request  HTTP 요청 객체 (인증된 사용자 정보 추출용)
     * @param pageable 페이징 정보
     * @return         회원의 리뷰 목록 (GetMyReviewsResponse의 Page 객체)
     */
    public Page<GetMyReviewsResponse> getMyReviews(HttpServletRequest request, Pageable pageable) {
        Member member = getAuthenticatedMember(request);
        return reviewRepository.findByWriter_Id(member.getId(), pageable).map(GetMyReviewsResponse::from);
    }

    /**
     * 특정 리뷰의 이미지를 삭제합니다.
     *
     * @param reviewId           이미지가 속한 리뷰의 ID
     * @param imageId            삭제할 이미지의 ID
     * @param request            HTTP 요청 객체 (인증된 사용자 정보 추출용)
     * @throws IOException       파일 삭제 중 I/O 오류가 발생한 경우
     * @throws RuntimeException  이미지나 리뷰를 찾을 수 없는 경우
     * @throws SecurityException 리뷰 작성자와 현재 인증된 사용자가 일치하지 않는 경우
     */
    @Transactional
    public void deleteReviewImage(Long reviewId, Long imageId, HttpServletRequest request) throws IOException {
        Member member = getAuthenticatedMember(request);

        ReviewImage image = reviewImageRepository.findById(imageId).orElseThrow(() -> new RuntimeException("Image not found"));
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getWriter().getId().equals(member.getId())) {
            throw new SecurityException("리뷰 작성자 정보와 일치하지 않은 사용자입니다.");
        }

        fileUploadService.deleteFile(image.getUrl());
        reviewImageRepository.delete(image);
    }
}
