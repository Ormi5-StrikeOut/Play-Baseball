package org.example.spring.service;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.constants.SalesStatus;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.exchange.dto.ExchangeAddRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeModifyRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeResponseDto;
import org.example.spring.domain.exchangeImage.ExchangeImage;
import org.example.spring.domain.member.Member;
import org.example.spring.exception.AuthenticationFailedException;
import org.example.spring.repository.ExchangeRepository;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class ExchangeService {

  private final ExchangeRepository exchangeRepository;
  private final ExchangeImageService exchangeImageService;
  private final JwtTokenValidator jwtTokenValidator;

  public ExchangeService(
      ExchangeRepository exchangeRepository,
      ExchangeImageService exchangeImageService,
      JwtTokenValidator jwtTokenValidator) {
    this.exchangeRepository = exchangeRepository;
    this.exchangeImageService = exchangeImageService;
    this.jwtTokenValidator = jwtTokenValidator;
  }

  /**
   * 게시글 추가 로직을 수행합니다. 상세 내용 추가 예정
   *
   * @param exchangeAddRequestDto
   * @param images
   * @return
   */
  @Transactional
  public ExchangeResponseDto addExchange(
      HttpServletRequest request,
      ExchangeAddRequestDto exchangeAddRequestDto,
      List<MultipartFile> images) {

    // token 유효성 검사 후 요청한 member 정보
    Member member =
        jwtTokenValidator.validateTokenAndGetMember(
            jwtTokenValidator.extractTokenFromHeader(request));

    Exchange exchange =
        Exchange.builder()
            .member(member)
            .title(exchangeAddRequestDto.getTitle())
            .price(exchangeAddRequestDto.getPrice())
            .regularPrice(123123123) // todo: Alan api 연결
            .content(exchangeAddRequestDto.getContent())
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
    return exchangeRepository
        .findByDeletedAtIsNull(pageable)
        .map(ExchangeResponseDto::fromExchange);
  }

  /** 최근 5개 게시글 조회 */
  @Transactional(readOnly = true)
  public List<ExchangeResponseDto> getLatestFiveExchanges() {
    return exchangeRepository.findTop5ByDeletedAtIsNullOrderByCreatedAtDesc().stream()
        .map(ExchangeResponseDto::fromExchange)
        .collect(Collectors.toList());
  }

  /** 특정회원 판매 게시글 조회 */
  @Transactional(readOnly = true)
  public Page<ExchangeResponseDto> getUserExchanges(Long memberId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return exchangeRepository
        .findByMemberIdAndDeletedAtIsNull(memberId, pageable)
        .map(ExchangeResponseDto::fromExchange);
  }

  @Transactional
  public Page<ExchangeResponseDto> getExchangesByTitleContaining(String title, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    return exchangeRepository
        .findByTitleContainingAndDeletedAtIsNull(title, pageable)
        .map(ExchangeResponseDto::fromExchange);
  }

  /** 게시글 수청 요청 응답 DTO 게시글을 찾을 수 없는경우 게시글을 찾을수 없습니다. 출력 */
  @Transactional
  public ExchangeResponseDto modifyExchange(
      HttpServletRequest request,
      Long id,
      ExchangeModifyRequestDto exchangeModifyRequestDto,
      List<MultipartFile> images) {

    // token 유효성 검사 후 요청한 member 정보
    Member member =
        jwtTokenValidator.validateTokenAndGetMember(
            jwtTokenValidator.extractTokenFromHeader(request));

    Exchange exchange =
        exchangeRepository.findById(id).orElseThrow(() -> new RuntimeException("게시글을 찾을수 없습니다."));

    try {
      if (!member.equals(exchange.getMember())) {
        throw new AccessDeniedException("Warning: Another user attempted to modify the post.");
      }

      List<ExchangeImage> imagesCopy = new ArrayList<>(exchange.getImages());
      for (ExchangeImage image : imagesCopy) {
        exchange.removeImage(image);
      }

      Exchange updateExchange =
          exchange.toBuilder()
              .title(exchangeModifyRequestDto.getTitle())
              .price(exchangeModifyRequestDto.getPrice())
              .content(exchangeModifyRequestDto.getContent())
              .updatedAt(new Timestamp(System.currentTimeMillis()))
              .status(exchangeModifyRequestDto.getStatus())
              .build();

      if (!images.isEmpty()) {
        for (MultipartFile image : images) {
          ExchangeImage exchangeImage = exchangeImageService.uploadImage(image, updateExchange);
          updateExchange.addImage(exchangeImage);
        }
      }

      return ExchangeResponseDto.fromExchange(exchangeRepository.save(updateExchange));
    } catch (AccessDeniedException e) {
      log.error("{} [{} -> {}]", e.getMessage(), member.getEmail(), exchange.getTitle());
      throw new AuthenticationFailedException(
          "Warning: Access denied. You do not have permission to modify the post.");
    }
  }

  /** 게시글 삭제 요청 게시글을 찾을 수 없는경우 게시글을 찾을수 없습니다. 출력 */
  @Transactional
  public void deleteExchange(HttpServletRequest request, Long id) {

    // token 유효성 검사 후 요청한 member 정보
    Member member =
        jwtTokenValidator.validateTokenAndGetMember(
            jwtTokenValidator.extractTokenFromHeader(request));

    Exchange exchange =
        exchangeRepository.findById(id).orElseThrow(() -> new RuntimeException("게시글을 찾을수 없습니다."));

    try {
      if (!member.equals(exchange.getMember())) {
        throw new AccessDeniedException("Warning: Another user has attempted to delete the post.");
      }

      Exchange deletedExchange =
          exchange.toBuilder().deletedAt(new Timestamp(System.currentTimeMillis())).build();
      exchangeRepository.save(deletedExchange);

    } catch (AccessDeniedException e) {
      log.error("{} [{} -> {}]", e.getMessage(), member.getEmail(), exchange.getTitle());
      throw new AuthenticationFailedException(
          "Warning: Access denied. You do not have permission to delete the post.");
    }
  }
}
