package org.example.spring.domain.exchange.dto;

import lombok.Builder;
import lombok.Getter;

/** 회원이 글 작성 요청할 때 사용하는 Dto Exchange Entity의 reaularPrice는 title내용 기반으로 Alan AI 통신을 통해 작성 */
@Getter
@Builder
public class ExchangeAddRequestDto {
  private String title;
  private int price;
  private String content;
}
