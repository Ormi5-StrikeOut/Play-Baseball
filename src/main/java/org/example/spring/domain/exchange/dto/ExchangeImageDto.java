package org.example.spring.domain.exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.spring.domain.exchangeImage.ExchangeImage;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeImageDto {
    private Long id;
    private String url;

    public static ExchangeImageDto fromImage(ExchangeImage image){
        return ExchangeImageDto.builder()
                .id(image.getId())
                .url(image.getUrl())
                .build();
    }
}
