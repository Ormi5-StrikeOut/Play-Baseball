package org.example.spring.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.spring.domain.exchange.Exchange;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeInformationResponse {
    private Long id;
    private String title;

    /**
     * 주어진 Exchange 엔티티로부터 ExchangeInformationResponse 인스턴스를 생성합니다.
     *
     * @param exchange 변환할 Exchange 엔티티
     * @return 주어진 Exchange로부터 데이터를 가져온 새로운 ExchangeInformationResponse 인스턴스
     */
    public static ExchangeInformationResponse from(Exchange exchange) {
        return ExchangeInformationResponse.builder()
            .id(exchange.getId())
            .title(exchange.getTitle())
            .build();
    }
}
