package org.example.spring.domain.review.dto;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.spring.domain.review.Review;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetMyReviewsResponse {
    private Long id;
    private String content;
    private int rate;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private ExchangeInformationResponse exchangeInfo;

    /**
     * 주어진 Review 엔티티로부터 GetMyReviewsResponse 인스턴스를 생성합니다.
     *
     * @param review 변환할 Review 엔티티
     * @return 주어진 Review로부터 데이터를 가져온 새로운 GetMyReviewsResponse 인스턴스,
     *         관련된 Exchange의 ID와 제목도 포함합니다.
     */
    public static GetMyReviewsResponse from(Review review) {
        return GetMyReviewsResponse.builder()
            .id(review.getId())
            .content(review.getContent())
            .rate(review.getRate())
            .createdAt(review.getCreatedAt())
            .updatedAt(review.getUpdatedAt())
            .exchangeInfo(ExchangeInformationResponse.from(review.getExchange()))
            .build();
    }
}
