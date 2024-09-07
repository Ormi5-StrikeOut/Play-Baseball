package org.example.spring.domain.review;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.member.Member;

@Entity
@Table(name = "review")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @JoinColumn(name = "exchange_id", nullable = false)
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Exchange exchange;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member writer;

    @Column(name = "content", length = 300, nullable = false)
    private String content;

    @Column(name = "rate", nullable = false)
    private int rate;

    @Column(name = "is_secret", nullable = false)
    private boolean isSecret;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    /**
     * 특정 리뷰의 내용을 수정하고, 업데이트 시각을 현재 시각으로 설정합니다.
     *
     * @param content 수정할 새로운 리뷰 내용
     */
    public void modifyContent(String content) {
        this.content = content;
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
