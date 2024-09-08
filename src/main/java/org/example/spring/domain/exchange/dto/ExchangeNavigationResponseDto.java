package org.example.spring.domain.exchange.dto;

import java.sql.Timestamp;
import lombok.Builder;
import lombok.Getter;
import org.example.spring.domain.exchange.Exchange;

@Getter
@Builder
public class ExchangeNavigationResponseDto {
  private String title;
  private int price;
  private String url;
  private String imageUrl;
  private Timestamp updatedAt;

  public static ExchangeNavigationResponseDto fromExchange(Exchange exchange) {
    String imageUrl = "";
    if (!exchange.getImages().isEmpty()) {
      imageUrl = exchange.getImages().getFirst().getUrl();
    }

    return ExchangeNavigationResponseDto.builder()
        .title(exchange.getTitle())
        .price(exchange.getPrice())
        .url("https://www.ioshane.com/exchange/" + exchange.getId())
        .imageUrl(imageUrl)
        .updatedAt(exchange.getUpdatedAt())
        .build();
  }
}
