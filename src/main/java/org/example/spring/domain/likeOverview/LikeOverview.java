package org.example.spring.domain.likeOverview;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.spring.domain.exchange.Exchange;

@Entity
@Table(name = "like_overview")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LikeOverview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_overview_id")
    private Long id;

    @JoinColumn(name = "exchange_id", nullable = false)
    @OneToOne(fetch = FetchType.LAZY)
    private Exchange exchange;

    @Column(name = "count", nullable = false)
    private long count;

    /**
     * 좋아요 통계를 업데이트합니다.
     *
     * @param newCount 추가되는 좋아요 수. 양수면 좋아요 증가, 음수면 좋아요 감소를 의미합니다.
     */
    public void updateLikeStats(long newCount) {
        this.count += newCount;
    }
}
