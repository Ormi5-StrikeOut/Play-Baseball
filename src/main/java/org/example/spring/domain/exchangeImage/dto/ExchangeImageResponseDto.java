package org.example.spring.domain.exchangeImage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.spring.domain.exchangeImage.ExchangeImage;

/** 게시물에 속한 image 응답에 대해 사용하는 Dto ExchangeResponseDto와 주로 함께 사용됨 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeImageResponseDto {
  private Long id;
  private String url;

  public static ExchangeImageResponseDto fromImage(ExchangeImage image) {
    return ExchangeImageResponseDto.builder().id(image.getId()).url(image.getUrl()).build();
  }
}
