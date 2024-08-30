package org.example.spring.controller;

import lombok.RequiredArgsConstructor;
import org.example.spring.common.ApiResponseDto;
import org.example.spring.domain.exchange.dto.ExchangeModifyRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeResponseDto;
import org.example.spring.service.ExchangeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchanges")
@RequiredArgsConstructor

public class ExchangeController {

    private final ExchangeService exchangeService;

    /**
     * 중고거래 게시물을 추가합니다.
     *
     * @param request 게시글 추가 요청 자료 DTO
     * @return 생성된 게시물 응답 DTO
     */
    @PostMapping
    public ResponseEntity<ApiResponseDto<ExchangeResponseDto>> addExchange(@RequestBody ExchangeRequestDto request) {
        ExchangeResponseDto response = exchangeService.addExchange(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(response.getTitle(), response));
    }

    /**
     * 기존에 작성되어 있던 중고거래 게시물 내용을 수정합니다.
     *
     * @param id DB에 등록된 수정할 게시글 id
     * @param request 게시글 수정 요청 자료 DTO
     * @return 수정된 게시물 응답 DTO
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ExchangeResponseDto>> modifyExchange(@PathVariable Long id, @RequestBody ExchangeModifyRequestDto request) {
        ExchangeResponseDto response = exchangeService.modifyExchange(id, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDto.success(response.getTitle(), response));
    }

    /**
     * 기존에 작성되어 있던 중고거래 게시물을 삭제합니다.
     * 삭제는 Soft delete 방식으로 이루어지며, DB에 등록된 deleted_at 내용에 값을 추가합니다.
     *
     * @param id DB에 등록된 삭제할 게시글 id
     * @return 삭제된 게시물 응답 DTO
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteExchange(@PathVariable Long id) {
        exchangeService.deleteExchange(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDto.success( "게시물이 삭제 되었습니다.", null));
    }

    /**
     * 작성되어 있는 모든 게시물을 조회합니다.
     *
     * @return 작성되어 있는 모든 게시물
     */
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<ExchangeResponseDto>>> getAllExchanges() {
        List<ExchangeResponseDto> responses = exchangeService.getAllExchanges();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDto.success("모든 게시물 조회 성공.", responses));
    }

    /**
     * 최근에 작성된 게시물 5개를 조회합니다.
     *
     * @return Exchange table 에서 Created_at을 내림차순으로 정렬한 row 5개
     */
    @GetMapping("/five")
    public ResponseEntity<ApiResponseDto<List<ExchangeResponseDto>>> getLatestFiveExchanges() {
        List<ExchangeResponseDto> responses = exchangeService.getLatestFiveExchanges();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDto.success("게시물 5개 조회 성공.", responses));
    }

    /**
     * 엔드포인트: 게시글 상태 수정
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ExchangeResponseDto> modifySalesStatus(@PathVariable Long id) {
        ExchangeResponseDto response = exchangeService.modifySalesStatus(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 회원이 작성한 게시물을 조회합니다.
     *
     * @param memberId 특정 회원 id
     * @return memberId와 일치하는 게시글 목록
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponseDto<List<ExchangeResponseDto>>> getMyExchanges(@PathVariable Long memberId) {
        List<ExchangeResponseDto> responses = exchangeService.getMyExchanges(memberId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDto.success("회원의 게시물 조회 성공", responses));
    }
}
