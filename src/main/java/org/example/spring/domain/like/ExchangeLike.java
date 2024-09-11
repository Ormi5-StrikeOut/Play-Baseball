package org.example.spring.domain.like;

import java.sql.Timestamp;

import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.member.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exchange_like")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeLike {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "like_id")
	private Long id;

	@JoinColumn(name = "exchange_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Exchange exchange;

	@JoinColumn(name = "member_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Member member;

	@Column(name = "created_at", nullable = false)
	private Timestamp createdAt;

	@Column(name = "canceled_at")
	private Timestamp canceledAt;

	/**
	 * 좋아요 상태를 토글합니다.
	 *
	 * <ul>
	 *   <li>좋아요가 활성 상태인 경우 (canceledAt이 null인 경우), 좋아요를 취소합니다.</li>
	 *   <li>좋아요가 취소된 상태인 경우 (canceledAt이 null이 아닌 경우), 좋아요를 다시 활성화합니다.</li>
	 * </ul>
	 *
	 * <p>좋아요 취소 시, 현재 시간을 canceledAt에 기록합니다.</p>
	 * <p>좋아요 재활성화 시, canceledAt을 null로 설정하고 createdAt을 현재 시간으로 업데이트합니다.</p>
	 */
	public boolean toggleLike() {
		if (this.canceledAt == null) {
			this.canceledAt = new Timestamp(System.currentTimeMillis());
			return false;
		} else {
			this.canceledAt = null;
			this.createdAt = new Timestamp(System.currentTimeMillis());
			return true;
		}
	}
}
