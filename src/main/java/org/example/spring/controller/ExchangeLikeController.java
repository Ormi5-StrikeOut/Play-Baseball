package org.example.spring.controller;

import org.example.spring.common.ApiResponseDto;
import org.example.spring.domain.like.dto.AddLikeRequest;
import org.example.spring.service.ExchangeLikeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * 중고 거래 게시글의 좋아요 기능을 처리하는 컨트롤러 클래스입니다.
 * 이 컨트롤러는 Exchange 시스템에서 좋아요를 관리하는 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
@Tag(name = "Exchange Like API", description = "중고 거래 게시글의 좋아요 기능을 관리하는 API")
public class ExchangeLikeController {
	private final ExchangeLikeService exchangeLikeService;

	/**
	 * 중고 거래 게시글에 좋아요를 추가하거나 취소합니다.
	 * 이 엔드포인트는 사용자가 중고 거래 게시글에 대한 좋아요 상태를 토글할 수 있게 합니다.
	 * 이미 좋아요가 있는 경우 취소되고, 없는 경우 추가됩니다.
	 *
	 * @param request HTTP 요청 객체. 현재 사용자의 인증 정보를 포함하고 있습니다.
	 *                이 정보는 사용자 식별 및 권한 확인에 사용될 수 있습니다.
	 * @param addLikeRequest 좋아요를 추가할 게시글의 정보를 포함하는 요청 객체.
	 *                       이 객체에는 게시글 ID 등의 필요한 정보가 포함되어야 합니다.
	 * @return ResponseEntity<ApiResponseDto < Boolean>> 형태의 응답.
	 *         - HTTP 상태 코드 201 (CREATED)를 반환합니다.
	 *         - 응답 본문에는 작업 성공 여부(Boolean)와 성공 메시지가 포함됩니다.
	 */
	@Operation(summary = "좋아요 추가 또는 취소", description = "중고 거래 게시글에 대한 좋아요를 추가하거나 취소합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "좋아요 상태 변경 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseDto.class))),
		@ApiResponse(responseCode = "401", description = "인증 실패"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")})
	@PostMapping
	public ResponseEntity<ApiResponseDto<Boolean>> addLike(
		@Parameter(description = "HTTP 요청 객체. 사용자의 인증 정보를 포함합니다.") HttpServletRequest request,
		@Parameter(description = "좋아요를 추가할 게시글의 정보", required = true) @RequestBody AddLikeRequest addLikeRequest) {
		boolean result = exchangeLikeService.addLike(request, addLikeRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success("좋아요 상태를 변경하였습니다.", result));
	}
}
