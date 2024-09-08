package org.example.spring.domain.exchange.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import org.example.spring.constants.SalesStatus;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.exchangeImage.dto.ExchangeImageResponseDto;

/** 게시글 관련 요청을 보낸 후 응답을 받을 때 사용하는 Dto 관련 요청 목록: 생성, 조회, 수정 */
@Getter
@Builder
public class ExchangeResponseDto {
  private final Long id;
  private final Long memberId;
  private final String title;
  private final int price;
  private final int regularPrice;
  private final String content;
  private final int viewCount;
  private final SalesStatus status;
  private final Timestamp updatedAt;
  private final List<ExchangeImageResponseDto> images;

  public static ExchangeResponseDto fromExchange(Exchange exchange) {
    // 이미지가 없을 경우 기본 이미지 url 변환
    String defaultImageUrl =
        "http://localhost:8080/uploads/c2ba53a3-c5d2-458d-beea-584384ad88c1_ad.jpg";

    List<ExchangeImageResponseDto> images = new ArrayList<>();
    if (!exchange.getImages().isEmpty()) {
      images =
          exchange.getImages().stream()
              .map(ExchangeImageResponseDto::fromImage)
              .collect(Collectors.toList());
    } else {
      images = List.of(ExchangeImageResponseDto.builder().url(defaultImageUrl).build());
    }

    return ExchangeResponseDto.builder()
        .id(exchange.getId())
        .memberId(exchange.getMember().getId())
        .title(exchange.getTitle())
        .price(exchange.getPrice())
        .regularPrice(exchange.getRegularPrice())
        .content(exchange.getContent())
        .viewCount(exchange.getViewCount())
        .status(exchange.getStatus())
        .updatedAt(exchange.getUpdatedAt())
        .images(images)
        .build();
  }
}
