package org.example.spring.controller;

import java.util.List;

import org.example.spring.common.ApiResponseDto;
import org.example.spring.constants.SalesStatus;
import org.example.spring.domain.exchange.dto.ExchangeAddRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeDetailResponseDto;
import org.example.spring.domain.exchange.dto.ExchangeModifyRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeNavigationResponseDto;
import org.example.spring.domain.exchange.dto.ExchangeResponseDto;
import org.example.spring.exception.InvalidTokenException;
import org.example.spring.service.ExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

/**
 * ExchangeController는 중고 거래 게시물과 관련된 API를 처리하는 컨트롤러입니다.
 * 중고 거래 게시물의 추가, 수정, 삭제, 조회 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/exchanges")
@Tag(name = "Exchange API", description = "중고 거래 게시물 관련 API")
public class ExchangeController {

	/** 페이지 기본값 */
	private final String PAGE_DEFAULT = "0";

	/** 페이지 크기 기본값 */
	private final String PAGE_SIZE_DEFAULT = "16";

	private final ExchangeService exchangeService;

	/**
	 * ExchangeController의 생성자입니다.
	 *
	 * @param exchangeService 중고 거래 게시물과 관련된 서비스 클래스
	 */
	@Autowired
	public ExchangeController(ExchangeService exchangeService) {
		this.exchangeService = exchangeService;
	}

	/**
	 * 새로운 중고 거래 게시물을 추가합니다.
	 *
	 * @param request              사용자 요청 정보를 포함한 객체
	 * @param exchangeAddRequestDto 게시물 추가 요청 데이터를 포함한 DTO
	 * @param images               게시물에 포함될 이미지 리스트 (선택사항)
	 * @return 게시물 추가 성공 여부를 포함한 응답
	 */
	@Operation(summary = "중고거래 게시물 추가", description = "새로운 중고거래 게시물을 추가합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "게시물 생성 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExchangeResponseDto.class))),
		@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	@PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<ApiResponseDto<ExchangeResponseDto>> addExchange(
		@Parameter(description = "회원 여부 확인용 http request") HttpServletRequest request,
		@Parameter(description = "게시글 추가 요청 자료 DTO", required = true, content = @Content(mediaType = "application/json")) @RequestPart("exchangeRequestDto") ExchangeAddRequestDto exchangeAddRequestDto,
		@Parameter(description = "게시글에 포함된 이미지 리스트", required = false, content = @Content(mediaType = "multipart/form-data")) @RequestPart(value = "images", required = false) List<MultipartFile> images) {

		try {
			ExchangeResponseDto exchangeResponseDto = exchangeService.addExchange(request, exchangeAddRequestDto,
				images);
			return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponseDto.success(exchangeResponseDto.getTitle(), exchangeResponseDto));
		} catch (InvalidTokenException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponseDto.success(e.getMessage(), null));
		}
	}

	/**
	 * 모든 중고 거래 게시물 목록을 조회합니다.
	 *
	 * @param status 게시물의 판매 상태 필터 (옵션)
	 * @param page   페이지 번호 (옵션)
	 * @param size   페이지 크기 (옵션)
	 * @return 페이지화된 게시물 목록을 포함한 응답
	 */
	@Operation(summary = "모든 게시물 목록 조회", description = "삭제되지 않은 모든 게시물을 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "모든 게시물 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
	})
	@GetMapping
	public ResponseEntity<ApiResponseDto<Page<ExchangeNavigationResponseDto>>> getAllExchanges(
		@Parameter(description = "게시물 판매 상태", example = "NONE") @RequestParam(required = false, defaultValue = "NONE") SalesStatus status,
		@Parameter(description = "페이지 번호", example = PAGE_DEFAULT) @RequestParam(required = false, defaultValue = PAGE_DEFAULT) int page,
		@Parameter(description = "페이지 크기", example = PAGE_SIZE_DEFAULT) @RequestParam(required = false, defaultValue = PAGE_SIZE_DEFAULT) int size) {

		Page<ExchangeNavigationResponseDto> responses = exchangeService.getAllExchanges(status, page, size);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("모든 게시물 조회 성공.", responses));
	}

	/**
	 * 최근 5개의 중고 거래 게시물을 조회합니다.
	 *
	 * @return 최근 작성된 5개의 게시물을 포함한 응답
	 */
	@Operation(summary = "최근 5개 게시물 조회", description = "최근 작성된 게시물 5개를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "게시물 5개 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
	})
	@GetMapping("/five")
	public ResponseEntity<ApiResponseDto<List<ExchangeNavigationResponseDto>>> getLatestFiveExchanges() {
		List<ExchangeNavigationResponseDto> responses = exchangeService.getLatestFiveExchanges();
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("게시물 5개 조회 성공.", responses));
	}

	/**
	 * 특정 회원이 작성한 게시물 목록을 조회합니다.
	 *
	 * @param memberId 조회할 회원의 ID
	 * @param page     페이지 번호 (옵션)
	 * @param size     페이지 크기 (옵션)
	 * @return 회원이 작성한 게시물 목록을 포함한 응답
	 */
	@Operation(summary = "특정 회원 게시물 목록 조회", description = "특정 회원이 작성한 게시물 목록을 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "회원의 게시물 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
	})
	@GetMapping("/member/{memberId}")
	public ResponseEntity<ApiResponseDto<Page<ExchangeNavigationResponseDto>>> getMyExchanges(
		@Parameter(description = "회원 ID", example = "1", required = true) @PathVariable Long memberId,
		@Parameter(description = "페이지 번호", example = PAGE_DEFAULT) @RequestParam(required = false, defaultValue = PAGE_DEFAULT) int page,
		@Parameter(description = "페이지 크기", example = PAGE_SIZE_DEFAULT) @RequestParam(required = false, defaultValue = PAGE_SIZE_DEFAULT) int size) {

		Page<ExchangeNavigationResponseDto> responses = exchangeService.getUserExchanges(memberId, page, size);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("회원의 게시물 조회 성공", responses));
	}

	/**
	 * 제목에 특정 키워드가 포함된 게시물 목록을 조회합니다.
	 *
	 * @param keyword 검색할 키워드
	 * @param status  게시물의 판매 상태 필터 (옵션)
	 * @param page    페이지 번호 (옵션)
	 * @param size    페이지 크기 (옵션)
	 * @return 제목에 키워드가 포함된 게시물 목록을 포함한 응답
	 */
	@Operation(summary = "게시물 검색", description = "제목에 키워드가 포함된 게시물 목록을 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "검색 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
	})
	@GetMapping("/search")
	public ResponseEntity<ApiResponseDto<Page<ExchangeNavigationResponseDto>>> getExchangesByTitleContaining(
		@Parameter(description = "검색 키워드", example = "") @RequestParam(required = false, defaultValue = "") String keyword,
		@Parameter(description = "게시물 판매 상태", example = "NONE") @RequestParam(required = false, defaultValue = "NONE") SalesStatus status,
		@Parameter(description = "페이지 번호", example = PAGE_DEFAULT) @RequestParam(required = false, defaultValue = PAGE_DEFAULT) int page,
		@Parameter(description = "페이지 크기", example = PAGE_SIZE_DEFAULT) @RequestParam(required = false, defaultValue = PAGE_SIZE_DEFAULT) int size) {

		Page<ExchangeNavigationResponseDto> responses = exchangeService.getExchangesByTitleContaining(keyword, status,
			page, size);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("검색 성공", responses));
	}

	/**
	 * 특정 게시물의 상세 정보를 조회합니다.
	 *
	 * @param request HTTP 요청 객체
	 * @param id      조회할 게시물의 ID
	 * @return 게시물 상세 정보를 포함한 응답
	 */
	@Operation(summary = "특정 게시물 상세 조회", description = "특정 게시물의 상세 정보를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "게시물 조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExchangeDetailResponseDto.class)))
	})
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponseDto<ExchangeDetailResponseDto>> getExchangeDetail(
		@Parameter(description = "회원 여부 확인용 http request") HttpServletRequest request,
		@Parameter(description = "게시물 ID", example = "1", required = true) @PathVariable Long id) {

		ExchangeDetailResponseDto response = exchangeService.getExchangeDetail(request, id);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("게시물 조회 성공", response));
	}

	/**
	 * 기존 중고 거래 게시물을 수정합니다.
	 *
	 * @param request               HTTP 요청 객체
	 * @param id                    수정할 게시물의 ID
	 * @param exchangeModifyRequestDto 수정할 게시물의 요청 데이터
	 * @param images                수정할 이미지 리스트 (선택사항)
	 * @return 게시물 수정 성공 여부를 포함한 응답
	 */
	@Operation(summary = "게시물 수정", description = "기존에 작성한 중고거래 게시물을 수정합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "게시물 수정 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExchangeResponseDto.class))),
		@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	@PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<ApiResponseDto<ExchangeResponseDto>> modifyExchange(
		@Parameter(description = "회원 여부 확인용 http request") HttpServletRequest request,
		@Parameter(description = "게시물 ID", required = true) @PathVariable Long id,
		@Parameter(description = "게시글 수정 요청 자료 DTO") @RequestPart("exchangeRequestDto") ExchangeModifyRequestDto exchangeModifyRequestDto,
		@Parameter(description = "게시글에 포함된 이미지 리스트", required = false) @RequestPart(value = "images", required = false) List<MultipartFile> images) {

		try {
			ExchangeResponseDto exchangeResponseDto = exchangeService.modifyExchange(request, id,
				exchangeModifyRequestDto, images);
			return ResponseEntity.status(HttpStatus.OK)
				.body(ApiResponseDto.success(exchangeResponseDto.getTitle(), exchangeResponseDto));
		} catch (InvalidTokenException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponseDto.success(e.getMessage(), null));
		}
	}

	/**
	 * 기존 중고 거래 게시물을 삭제합니다.
	 *
	 * @param request HTTP 요청 객체
	 * @param id      삭제할 게시물의 ID
	 * @return 게시물 삭제 성공 여부를 포함한 응답
	 */
	@Operation(summary = "게시물 삭제", description = "기존에 작성된 중고거래 게시물을 삭제합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "게시물 삭제 성공"),
		@ApiResponse(responseCode = "401", description = "인증 실패")
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponseDto<Void>> deleteExchange(
		@Parameter(description = "회원 여부 확인용 http request") HttpServletRequest request,
		@Parameter(description = "게시물 ID", required = true) @PathVariable Long id) {
		exchangeService.deleteExchange(request, id);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("게시물이 삭제되었습니다.", null));
	}
}
