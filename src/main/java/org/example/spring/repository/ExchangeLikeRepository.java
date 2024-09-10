package org.example.spring.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.like.ExchangeLike;
import org.example.spring.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ExchangeLike 엔티티에 대한 데이터 접근 작업을 처리하는 리포지토리 인터페이스입니다.
 * 이 인터페이스는 JpaRepository를 확장하여 기본적인 CRUD 작업과 추가적인 커스텀 쿼리 메소드를 제공합니다.
 */
@Repository
public interface ExchangeLikeRepository extends JpaRepository<ExchangeLike, Long> {
    /**
     * 주어진 Exchange와 Member에 해당하는 ExchangeLike를 찾아 반환합니다.
     *
     * @param exchange 조회할 Exchange 객체
     * @param member   조회할 Member 객체
     * @return 해당하는 ExchangeLike 객체를 포함한 Optional
     */
    Optional<ExchangeLike> findByExchangeAndMember(Exchange exchange, Member member);

    /**
     * 주어진 시간 범위 내에 생성된 모든 ExchangeLike를 찾아 반환합니다.
     * 이 메소드는 주로 어제 생성된 좋아요를 조회하는 데 사용됩니다.
     *
     * @param startOfYesterdayTimestamp 조회 시작 시간
     * @param endOfYesterdayTimestamp   조회 종료 시간
     * @return 해당 시간 범위 내에 생성된 ExchangeLike 목록
     */
    List<ExchangeLike> findByCreatedAtBetween(Timestamp startOfYesterdayTimestamp, Timestamp endOfYesterdayTimestamp);

    /**
     * 주어진 시간 범위 내에 취소된 모든 ExchangeLike를 찾아 반환합니다.
     * 이 메소드는 주로 어제 취소된 좋아요를 조회하는 데 사용됩니다.
     *
     * @param startOfYesterdayTimestamp 조회 시작 시간
     * @param endOfYesterdayTimestamp 조회 종료 시간
     * @return 해당 시간 범위 내에 취소된 ExchangeLike 목록
     */
    List<ExchangeLike> findByCanceledAtBetween(Timestamp startOfYesterdayTimestamp, Timestamp endOfYesterdayTimestamp);
}
