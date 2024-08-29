package org.example.spring.controller;

import lombok.RequiredArgsConstructor;
import org.example.spring.domain.exchange.dto.ExchangeModifyRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeResponseDto;
import org.example.spring.service.ExchangeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchanges")
@RequiredArgsConstructor

public class ExchangeController {

    private final ExchangeService exchangeService;

    /**
     * 엔드포인트: 게시글 추가
     */
    @PostMapping
    public ResponseEntity<ExchangeResponseDto> addExchange(@RequestBody ExchangeRequestDto request) {
        ExchangeResponseDto response = exchangeService.addExchange(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 엔드포인트: 게시글 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<ExchangeResponseDto> modifyExchange(@PathVariable Long id, @RequestBody ExchangeModifyRequestDto request) {
        ExchangeResponseDto response = exchangeService.modifyExchange(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 엔드포인트: 게시글 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExchange(@PathVariable Long id) {
        exchangeService.deleteExchange(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 엔드포인트: 게시글 조회(모든사용자)
     */
    @GetMapping
    public ResponseEntity<List<ExchangeResponseDto>> getAllExchanges() {
        List<ExchangeResponseDto> responses = exchangeService.getAllExchanges();
        return ResponseEntity.ok(responses);
    }

    /**
     * 엔드포인트: 최근게시글 5개 조회
     */
    @GetMapping("/five")
    public ResponseEntity<List<ExchangeResponseDto>> getLatestFiveExchanges() {
        List<ExchangeResponseDto> responses = exchangeService.getLatestFiveExchanges();
        return ResponseEntity.ok(responses);
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
     * 엔드포인트: 특정회원 게시글 조회
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<List<ExchangeResponseDto>> getMyExchanges(@PathVariable Long memberId) {
        List<ExchangeResponseDto> responses = exchangeService.getMyExchanges(memberId);
        return ResponseEntity.ok(responses);
    }
}
