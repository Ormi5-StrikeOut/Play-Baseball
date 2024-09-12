package org.example.spring.domain.exchange.dto;

import java.sql.Timestamp;

import org.example.spring.constants.SalesStatus;
import org.example.spring.domain.exchange.Exchange;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExchangeNavigationResponseDto {
	private final String title;
	private final int price;
	private final String url;
	private final String imageUrl;
	private final Timestamp updatedAt;
	private final int viewCount;
	private final SalesStatus status;

	public static ExchangeNavigationResponseDto fromExchange(Exchange exchange, String url) {
		String imageUrl = "";
		if (!exchange.getImages().isEmpty()) {
			imageUrl = exchange.getImages().getFirst().getUrl();
		}

		return ExchangeNavigationResponseDto.builder()
			.title(exchange.getTitle())
			.price(exchange.getPrice())
			.url(url + "/" + exchange.getId())
			.imageUrl(imageUrl)
			.updatedAt(exchange.getUpdatedAt())
			.viewCount(exchange.getViewCount())
			.status(exchange.getStatus())
			.build();
	}
}
