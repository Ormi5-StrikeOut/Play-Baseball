package org.example.spring.service;

import lombok.RequiredArgsConstructor;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.exchange.dto.ExchangeModifyRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeResponseDto;
import org.example.spring.domain.member.Member;
import org.example.spring.repository.ExchangeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeRepository exchangeRepository;

    /**
     * 새로운 게시글 생성요청
     * 응답 DTO
     */
    @Transactional
    public ExchangeResponseDto addExchange(ExchangeRequestDto request) {
        Exchange exchange = Exchange.builder()
                .writer(Member.builder().id(request.getMemberId()).build())
                .title(request.getTitle())
                .price(request.getPrice())
                .regularPrice(request.getRegularPrice())
                .content(request.getContent())
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        Exchange savedExchange = exchangeRepository.save(exchange);
        return convertToResponseDto(savedExchange);
    }

    /**
     * 게시글 수청 요청
     * 응답 DTO
     * 게시글을 찾을 수 없는경우 게시글을 찾을수 없습니다. 출력
     */
    @Transactional
    public ExchangeResponseDto modifyExchange(Long id, ExchangeModifyRequestDto request) {
        Exchange exchange = exchangeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을수 없습니다."));

        Exchange updatedExchange = updateExchangeFields(exchange, request);

        return convertToResponseDto(exchangeRepository.save(updatedExchange));
    }

    private Exchange updateExchangeFields(Exchange exchange, ExchangeModifyRequestDto request) {
        return Exchange.builder()
                .id(exchange.getId())
                .writer(exchange.getWriter())
                .title(request.getTitle())
                .price(request.getPrice())
                .regularPrice(request.getRegularPrice())
                .content(request.getContent())
                .viewCount(exchange.getViewCount())
                .createdAt(exchange.getCreatedAt())
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    /**
     * 게시글 삭제 요청
     * 게시글을 찾을 수 없는경우 게시글을 찾을수 없습니다. 출력
     */
    @Transactional
    public void deleteExchange(Long id) {
        Exchange exchange = exchangeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을수 없습니다."));
        Exchange deletedExchange = Exchange.builder()
                .id(exchange.getId())
                .writer(exchange.getWriter())
                .title(exchange.getTitle())
                .price(exchange.getPrice())
                .regularPrice(exchange.getRegularPrice())
                .content(exchange.getContent())
                .viewCount(exchange.getViewCount())
                .createdAt(exchange.getCreatedAt())
                .updatedAt(exchange.getUpdatedAt())
                .deletedAt(new Timestamp(System.currentTimeMillis()))
                .build();
        exchangeRepository.save(deletedExchange);
    }

    /**
     * 모든 게시글을 조회
     */
    @Transactional(readOnly = true)
    public List<ExchangeResponseDto> getAllExchanges() {
        List<Exchange> exchanges = exchangeRepository.findAll();
        List<ExchangeResponseDto> responseDto = new ArrayList<>();
        for (Exchange exchange : exchanges) {
            responseDto.add(convertToResponseDto(exchange));
        }
        return responseDto;
    }

    /**
     * 최근 5개 게시글 조회
     */
    @Transactional(readOnly = true)
    public List<ExchangeResponseDto> getLatestFiveExchanges() {
        List<Exchange> exchanges = exchangeRepository.findTop5ByOrderByCreatedAtDesc();
        List<ExchangeResponseDto> responseDto = new ArrayList<>();
        for (Exchange exchange : exchanges) {
            responseDto.add(convertToResponseDto(exchange));
        }
        return responseDto;
    }

    /**
     * 게시글 판매상태 수정
     */
    @Transactional
    public ExchangeResponseDto modifySalesStatus(Long id) {
        Exchange exchange = exchangeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을수 없습니다."));
        Exchange updatedExchange = Exchange.builder()
                .id(exchange.getId())
                .writer(exchange.getWriter())
                .title(exchange.getTitle())
                .price(exchange.getPrice())
                .regularPrice(exchange.getRegularPrice())
                .content(exchange.getContent())
                .viewCount(exchange.getViewCount())
                .createdAt(exchange.getCreatedAt())
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                // .status(newStatus) // Add this line when implementing status
                .build();
        return convertToResponseDto(exchangeRepository.save(updatedExchange));
    }

    /**
     * 특정회원 판매 게시글 조회
     */
    @Transactional(readOnly = true)
    public List<ExchangeResponseDto> getMyExchanges(Long memberId) {
        List<Exchange> exchanges = exchangeRepository.findByWriterId(memberId);
        List<ExchangeResponseDto> responseDto = new ArrayList<>();
        for (Exchange exchange : exchanges) {
            responseDto.add(convertToResponseDto(exchange));
        }
        return responseDto;
    }

    private ExchangeResponseDto convertToResponseDto(Exchange exchange) {
        return ExchangeResponseDto.builder()
                .id(exchange.getId())
                .memberId(exchange.getWriter().getId())
                .title(exchange.getTitle())
                .price(exchange.getPrice())
                .regularPrice(exchange.getRegularPrice())
                .content(exchange.getContent())
                .viewCount(exchange.getViewCount())
                .createdAt(exchange.getCreatedAt())
                .updatedAt(exchange.getUpdatedAt())
                .deletedAt(exchange.getDeletedAt())
                .build();
    }
}

