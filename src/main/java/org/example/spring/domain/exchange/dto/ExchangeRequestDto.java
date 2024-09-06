package org.example.spring.domain.exchange.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.spring.domain.exchange.Exchange;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 회원이 글을 작성 및 수정할 때 사용하는 Dto
 */
@Getter
@Builder
public class ExchangeRequestDto {
    private final Long memberId;
    private String title;
    private int price;
    private int regularPrice;
    private String content;
    private List<ExchangeImageDto> images;

    public static ExchangeRequestDto fromExchange(Exchange exchange){
        List<ExchangeImageDto> images = new ArrayList<>();
        if(exchange.getImages() != null){
            images = exchange
                    .getImages()
                    .stream()
                    .map(ExchangeImageDto::fromImage)
                    .collect(Collectors.toList());
        }

        return ExchangeRequestDto.builder()
                .memberId(exchange.getMemberId().getId())
                .title(exchange.getTitle())
                .price(exchange.getPrice())
                .regularPrice(exchange.getRegularPrice())
                .content(exchange.getContent())
                .images(images)
                .build();
    }
}
