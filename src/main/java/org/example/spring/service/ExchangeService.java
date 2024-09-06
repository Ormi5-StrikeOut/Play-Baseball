package org.example.spring.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.example.spring.constants.SalesStatus;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.exchange.dto.ExchangeAddRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeModifyRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeResponseDto;
import org.example.spring.domain.exchangeImage.ExchangeImage;
import org.example.spring.domain.member.Member;
import org.example.spring.repository.ExchangeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ExchangeService {

  private final ExchangeRepository exchangeRepository;
  private final ExchangeImageService exchangeImageService;

  public ExchangeService(
      ExchangeRepository exchangeRepository, ExchangeImageService exchangeImageService) {
    this.exchangeRepository = exchangeRepository;
    this.exchangeImageService = exchangeImageService;
  }

  /**
   * 게시글 추가 로직을 수행합니다. 상세 내용 추가 예정
   *
   * @param request
   * @param images
   * @return
   */
  @Transactional
  public ExchangeResponseDto addExchange(
      ExchangeAddRequestDto request, List<MultipartFile> images) {

    Exchange exchange =
        Exchange.builder()
            .member(Member.builder().id(request.getMemberId()).build())
            .title(request.getTitle())
            .price(request.getPrice())
            .regularPrice(123123123) // todo: Alan api 연결
            .content(request.getContent())
            .status(SalesStatus.SALE)
            .build();

    exchangeRepository.save(exchange);

    if (!images.isEmpty()) {
      for (MultipartFile image : images) {
        ExchangeImage exchangeImage = exchangeImageService.uploadImage(image, exchange);
        exchange.addImage(exchangeImage);
      }
    }

    exchangeRepository.save(exchange);

    return ExchangeResponseDto.fromExchange(exchange);
  }

  /**
   * 작성된 모든 게시글 대상으로 페이지별로 조회
   *
   * @param page 불러올 페이지를 작성
   * @return pageable에 해당하는 페이지의 게시글을 리턴합니다.
   */
  @Transactional(readOnly = true)
  public Page<ExchangeResponseDto> getAllExchanges(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return exchangeRepository.findAll(pageable).map(ExchangeResponseDto::fromExchange);
  }

  /** 최근 5개 게시글 조회 */
  @Transactional(readOnly = true)
  public List<ExchangeResponseDto> getLatestFiveExchanges() {
    return exchangeRepository.findTop5ByOrderByCreatedAtDesc().stream()
        .map(ExchangeResponseDto::fromExchange)
        .collect(Collectors.toList());
  }

  /** 특정회원 판매 게시글 조회 */
  @Transactional(readOnly = true)
  public Page<ExchangeResponseDto> getUserExchanges(Long memberId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return exchangeRepository
        .findByMemberId(memberId, pageable)
        .map(ExchangeResponseDto::fromExchange);
  }

  @Transactional
  public Page<ExchangeResponseDto> getExchangesByTitleContaining(String title, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return exchangeRepository
        .findByTitleContaining(title, pageable)
        .map(ExchangeResponseDto::fromExchange);
  }

  /** 게시글 수청 요청 응답 DTO 게시글을 찾을 수 없는경우 게시글을 찾을수 없습니다. 출력 */
  @Transactional
  public ExchangeResponseDto modifyExchange(
      Long id, ExchangeModifyRequestDto request, List<MultipartFile> images) {

    Exchange exchange =
        exchangeRepository.findById(id).orElseThrow(() -> new RuntimeException("게시글을 찾을수 없습니다."));

    List<ExchangeImage> imagesCopy = new ArrayList<>(exchange.getImages());
    for (ExchangeImage image : imagesCopy) {
      exchange.removeImage(image);
    }

    Exchange updateExchange =
        exchange.toBuilder()
            .title(request.getTitle())
            .price(request.getPrice())
            .content(request.getContent())
            .updatedAt(new Timestamp(System.currentTimeMillis()))
            .status(request.getStatus())
            .build();

    if (!images.isEmpty()) {
      for (MultipartFile image : images) {
        ExchangeImage exchangeImage = exchangeImageService.uploadImage(image, updateExchange);
        updateExchange.addImage(exchangeImage);
      }
    }

    return ExchangeResponseDto.fromExchange(exchangeRepository.save(updateExchange));
  }

  /** 게시글 삭제 요청 게시글을 찾을 수 없는경우 게시글을 찾을수 없습니다. 출력 */
  @Transactional
  public void deleteExchange(Long id) {
    Exchange exchange =
        exchangeRepository.findById(id).orElseThrow(() -> new RuntimeException("게시글을 찾을수 없습니다."));
    Exchange deletedExchange =
        exchange.toBuilder().deletedAt(new Timestamp(System.currentTimeMillis())).build();
    exchangeRepository.save(deletedExchange);
  }
}
