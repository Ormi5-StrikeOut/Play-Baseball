package org.example.spring.domain.exchange.dto;

import org.example.spring.constants.SalesStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ExchangeModifyRequestDto {
	private final String title;
	private final int price;
	private final String content;
	private final SalesStatus status;
}
