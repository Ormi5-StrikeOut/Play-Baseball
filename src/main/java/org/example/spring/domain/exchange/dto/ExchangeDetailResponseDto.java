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

@Getter
@Builder
public class ExchangeDetailResponseDto {
  private final String title;
  private final int price;
  private final int regularPrice;
  private final String content;
  private final int viewCount;
  private final SalesStatus status;
  private final Timestamp updatedAt;
  private final List<ExchangeImageResponseDto> images;
  private final String writer;
  private final List<ExchangeNavigationResponseDto> recentExchangesByMember;
  private final boolean isWriter;

  public ExchangeDetailResponseDtoBuilder toBuilder() {
    return ExchangeDetailResponseDto.builder()
        .title(this.title)
        .price(this.price)
        .regularPrice(this.regularPrice)
        .content(this.content)
        .viewCount(this.viewCount)
        .status(this.status)
        .updatedAt(this.updatedAt)
        .images(this.images);
  }

  public static ExchangeDetailResponseDto fromExchange(Exchange exchange) {
    List<ExchangeImageResponseDto> images = new ArrayList<>();
    if (!exchange.getImages().isEmpty()) {
      images =
          exchange.getImages().stream()
              .map(ExchangeImageResponseDto::fromImage)
              .collect(Collectors.toList());
    }

    return ExchangeDetailResponseDto.builder()
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
