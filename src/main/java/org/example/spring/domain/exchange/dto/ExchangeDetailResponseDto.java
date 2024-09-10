package org.example.spring.domain.exchange.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.example.spring.constants.SalesStatus;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.exchangeImage.dto.ExchangeImageResponseDto;

import lombok.Builder;
import lombok.Getter;
import org.example.spring.domain.reviewOverview.ReviewOverview;

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
	private final String isWriter;
	private final long reviewCount;
	private final double average;
	private final long likeCount;

	public static ExchangeDetailResponseDto fromExchange(Exchange exchange,
		List<ExchangeNavigationResponseDto> recentExchangesByMember, boolean isWriter, long reviewCount, double average, long likeCount) {
		List<ExchangeImageResponseDto> images = new ArrayList<>();
		if (!exchange.getImages().isEmpty()) {
			images = exchange.getImages()
				.stream()
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
			.writer(exchange.getMember().getNickname())
			.recentExchangesByMember(recentExchangesByMember)
			.isWriter(isWriter ? "TRUE" : "FALSE")
			.reviewCount(reviewCount)
			.average(average)
			.likeCount(likeCount)
			.build();
	}
}
