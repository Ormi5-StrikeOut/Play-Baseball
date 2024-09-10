package org.example.spring.domain.like.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.like.ExchangeLike;
import org.example.spring.domain.member.Member;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddLikeRequest {
    private Long exchangeId;

    /**
     * 이 DTO를 ExchangeLike 엔티티로 변환합니다.
     *
     * @param exchange 좋아요가 연관된 Exchange 객체입니다.
     * @param member   좋아요를 추가하는 사용자를 나타내는 Member 객체입니다.
     * @return         이 DTO와 제공된 매개변수로 생성된 새로운 ExchangeLike 엔티티입니다.
     */
    public ExchangeLike toEntity(Exchange exchange, Member member) {
        return ExchangeLike.builder()
            .exchange(exchange)
            .member(member)
            .build();
    }
}
