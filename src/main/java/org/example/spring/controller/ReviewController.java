package org.example.spring.controller;

import java.io.IOException;
import java.util.List;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * ReviewController는 리뷰 관련 API 엔드포인트를 처리하는 REST 컨트롤러입니다.
 * 이 컨트롤러는 리뷰의 생성, 수정, 조회 및 리뷰 이미지 삭제 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review API", description = "리뷰 관련 API")
public class ReviewController {
	private final ReviewService reviewService;

	/**
	 * 새로운 리뷰를 등록합니다.
	 *
	 * @param request             현재 HTTP 요청 객체
	 * @param createReviewRequest 신규 리뷰 작성 정보를 담은 DTO
	 * @param images              리뷰에 첨부할 이미지 파일 목록 (선택사항)
	 * @return 새로운 리뷰 추가 성공 여부와 요청에 사용된 DTO를 포함한 HTTP 응답
	 * @throws IOException        파일 업로드 중 I/O 오류가 발생한 경우
	 */
	@Operation(summary = "리뷰 등록", description = "새로운 리뷰를 등록합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "리뷰 등록 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateReviewRequest.class))),
		@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<CreateReviewRequest> addReview(
		@Parameter(description = "HTTP 요청 객체") HttpServletRequest request,
		@Parameter(description = "리뷰 등록 요청 DTO") @RequestPart("review") CreateReviewRequest createReviewRequest,
		@Parameter(description = "리뷰 이미지 목록", content = @Content(mediaType = "multipart/form-data")) @RequestPart(value = "images", required = false) List<MultipartFile> images
	) throws IOException {
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
	 * @return 리뷰 수정 성공을 나타내는 HTTP 상태 코드 200 OK
	 * @throws IOException        파일 업로드 중 I/O 오류가 발생한 경우
	 */
	@Operation(summary = "리뷰 수정", description = "기존 리뷰를 수정합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
	})
	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Void> modifyReview(
		@Parameter(description = "리뷰 ID") @PathVariable Long id,
		@Parameter(description = "HTTP 요청 객체") HttpServletRequest request,
		@Parameter(description = "리뷰 수정 요청 DTO") @Valid @RequestPart(value = "review", required = false) ModifyReviewRequest modifyReviewRequest,
		@Parameter(description = "수정할 이미지 목록", content = @Content(mediaType = "multipart/form-data")) @RequestPart(value = "images", required = false) List<MultipartFile> images
	) throws IOException {
		reviewService.modifyReview(id, request, modifyReviewRequest, images);
		return ResponseEntity.ok().build();
	}

	/**
	 * 현재 로그인한 회원이 작성한 리뷰 목록을 페이징 처리하여 반환합니다.
	 *
	 * @param request  현재 HTTP 요청 객체
	 * @param pageable 페이지네이션 정보를 담고 있는 Pageable 객체.
	 *                 기본값: 페이지 크기 10, 'createdAt' 필드 기준 내림차순 정렬.
	 * @return 현재 로그인한 회원이 작성한 리뷰 목록을 담은 페이지화된 응답
	 */
	@Operation(summary = "내 리뷰 목록 조회", description = "로그인한 사용자가 작성한 리뷰 목록을 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetMyReviewsResponse.class))),
		@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	@GetMapping("/my")
	public ResponseEntity<Page<GetMyReviewsResponse>> getMyReviews(
		@Parameter(description = "HTTP 요청 객체") HttpServletRequest request,
		@PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable
	) {
		Page<GetMyReviewsResponse> reviews = reviewService.getMyReviews(request, pageable);
		return ResponseEntity.ok(reviews);
	}

	/**
	 * 특정 리뷰의 이미지를 삭제합니다.
	 *
	 * @param reviewId     이미지가 속한 리뷰의 ID
	 * @param imageId      삭제할 이미지의 ID
	 * @param request      현재 HTTP 요청 객체
	 * @return 이미지 삭제 성공을 나타내는 HTTP 상태 코드 200 OK
	 * @throws IOException 파일 삭제 중 I/O 오류가 발생한 경우
	 */
	@Operation(summary = "리뷰 이미지 삭제", description = "리뷰에 포함된 이미지를 삭제합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "이미지 삭제 성공"),
		@ApiResponse(responseCode = "404", description = "리뷰 또는 이미지를 찾을 수 없음")
	})
	@DeleteMapping("/{reviewId}/images/{imageId}")
	public ResponseEntity<Void> deleteReviewImage(
		@Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
		@Parameter(description = "이미지 ID") @PathVariable Long imageId,
		@Parameter(description = "HTTP 요청 객체") HttpServletRequest request
	) throws IOException {
		reviewService.deleteReviewImage(reviewId, imageId, request);
		return ResponseEntity.ok().build();
	}
}
