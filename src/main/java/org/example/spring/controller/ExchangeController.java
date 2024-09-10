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

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/exchanges")
public class ExchangeController {
	private final String PAGE_DEFAULT = "0";
	private final String PAGE_SIZE_DEFAULT = "16";
	private final ExchangeService exchangeService;

	@Autowired
	public ExchangeController(ExchangeService exchangeService) {
		this.exchangeService = exchangeService;
	}

	/**
	 * 중고거래 게시물을 추가합니다.
	 *
	 * @param request 회원 여부 확인용 http request
	 * @param exchangeAddRequestDto 게시글 추가 요청 자료 DTO
	 * @param images 게시글에 포함된 이미지
	 * @return 처리 결과에 따른 응답을 반환합니다.
	 */
	@PostMapping
	public ResponseEntity<ApiResponseDto<ExchangeResponseDto>> addExchange(HttpServletRequest request,
		@RequestPart("exchangeRequestDto") ExchangeAddRequestDto exchangeAddRequestDto,
		@RequestPart(value = "images", required = false) List<MultipartFile> images) {

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
	 * 작성된 글 중 삭제처리되지 않은 모든 글 목록을 조회합니다.
	 *
	 * @param page 게시물이 포함된 페이지
	 * @param size 한 번에 렌더링할 게시물 개수
	 * @return 게시글 목록을 page와 size에 따라 반환
	 */
	@GetMapping
	public ResponseEntity<ApiResponseDto<Page<ExchangeNavigationResponseDto>>> getAllExchanges(
		@RequestParam(required = false, defaultValue = "NONE") SalesStatus status,
		@RequestParam(required = false, defaultValue = PAGE_DEFAULT) int page,
		@RequestParam(required = false, defaultValue = PAGE_SIZE_DEFAULT) int size) {

		Page<ExchangeNavigationResponseDto> responses = exchangeService.getAllExchanges(status, page, size);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("모든 게시물 조회 성공.", responses));
	}

	/**
	 * 최근에 작성된 게시물 5개를 조회합니다.
	 *
	 * @return Exchange table 에서 Created_at을 내림차순으로 정렬한 row 5개
	 */
	@GetMapping("/five")
	public ResponseEntity<ApiResponseDto<List<ExchangeNavigationResponseDto>>> getLatestFiveExchanges() {

		List<ExchangeNavigationResponseDto> responses = exchangeService.getLatestFiveExchanges();
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("게시물 5개 조회 성공.", responses));
	}

	/**
	 * 특정 회원이 작성한 게시물 목록을 조회합니다.
	 *
	 * @param memberId 대상 회원 id
	 * @param page 게시물이 포함된 페이지
	 * @param size 한 번에 렌더링할 게시물 개수
	 * @return memberId와 일치하는 게시글 목록을 page와 size에 따라 반환
	 */
	@GetMapping("/member/{memberId}")
	public ResponseEntity<ApiResponseDto<Page<ExchangeNavigationResponseDto>>> getMyExchanges(
		@PathVariable Long memberId, @RequestParam(required = false, defaultValue = PAGE_DEFAULT) int page,
		@RequestParam(required = false, defaultValue = PAGE_SIZE_DEFAULT) int size) {
		Page<ExchangeNavigationResponseDto> responses = exchangeService.getUserExchanges(memberId, page, size);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("회원의 게시물 조회 성공", responses));
	}

	/**
	 * 제목에 대한 검색 기능입니다. 제목이 키워드에 포함되어있는 게시물 목록을 조회합니다.
	 *
	 * @param keyword 검색에 사용할 키워드.
	 * @param page 게시물이 포함된 페이지
	 * @param size 한 번에 렌더링할 게시물 개수
	 * @return 제목이 키워드에 포함되어있는 게시물 목록을 page와 size에 따라 반환
	 */
	@GetMapping("/search")
	public ResponseEntity<ApiResponseDto<Page<ExchangeNavigationResponseDto>>> getExchangesByTitleContaining(
		@RequestParam(required = false, defaultValue = "") String keyword,
		@RequestParam(required = false, defaultValue = "NONE") SalesStatus status,
		@RequestParam(required = false, defaultValue = PAGE_DEFAULT) int page,
		@RequestParam(required = false, defaultValue = PAGE_SIZE_DEFAULT) int size) {
		Page<ExchangeNavigationResponseDto> responses = exchangeService.getExchangesByTitleContaining(keyword, status,
			page,
			size);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("검색 성공", responses));
	}

	/**
	 * 특정 게시물 1개 및 작성자 여부를 조회합니다.
	 *
	 * @param request 작성자 여부 조회
	 * @param id 게시물 id
	 * @return 게시물 id와 일치하는 게시물 정보와 작성자 여부
	 */
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponseDto<ExchangeDetailResponseDto>> getExchangeDetail(HttpServletRequest request,
		@PathVariable Long id) {
		ExchangeDetailResponseDto response = exchangeService.getExchangeDetail(request, id);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("회원의 게시물 조회 성공", response));
	}

	/**
	 * 기존에 작성되어 있던 중고거래 게시물 내용을 수정합니다.
	 *
	 * @param request 수정 권한 확인용 http request
	 * @param id DB에 등록된 수정할 게시글 id
	 * @param exchangeModifyRequestDto 수정할 게시물 내용
	 * @param images 수정할 게시물 이미지
	 * @return 처리 결과에 따른 응답을 반환합니다.
	 */
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponseDto<ExchangeResponseDto>> modifyExchange(HttpServletRequest request,
		@PathVariable Long id, @RequestPart("exchangeRequestDto") ExchangeModifyRequestDto exchangeModifyRequestDto,
		@RequestPart(value = "images", required = false) List<MultipartFile> images) {

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
	 * 기존에 작성되어 있던 중고거래 게시물을 삭제합니다. 삭제는 Soft delete 방식으로 이루어지며, DB에 등록된 deleted_at 내용에 값을 추가합니다.
	 *
	 * @param request 삭제 처리 권한 확인용 http request
	 * @param id DB에 등록된 삭제할 게시글 id
	 * @return 응답에 상태에 따른 ResponseDto
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponseDto<Void>> deleteExchange(HttpServletRequest request, @PathVariable Long id) {
		exchangeService.deleteExchange(request, id);
		return ResponseEntity.status(HttpStatus.OK).body(ApiResponseDto.success("게시물이 삭제 되었습니다.", null));
	}
}
