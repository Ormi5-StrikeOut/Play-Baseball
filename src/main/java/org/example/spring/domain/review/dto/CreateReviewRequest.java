package org.example.spring.domain.review.dto;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.review.Review;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {
    private Long exchangeId;
    private Long writerId;
    private String content;
    private int rate;
    private boolean isSecret;

    /**
     * CreateReviewRequest를 Review 엔티티로 변환합니다.
     *
     * @param exchange 리뷰와 연관된 Exchange 엔티티
     * @param member   리뷰 작성자 Member 엔티티
     * @return         변환된 Review 엔티티
     */
    public Review toEntity(Exchange exchange, Member member) {
        return Review.builder()
            .exchange(exchange)
            .writer(member)
            .content(this.content)
            .rate(this.rate)
            .isSecret(this.isSecret)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();
    }
}
