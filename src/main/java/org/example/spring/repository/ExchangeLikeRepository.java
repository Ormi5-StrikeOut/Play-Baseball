package org.example.spring.repository;

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
     * 주어진 중고 거래 게시글과 회원이 이미 좋아요를 눌렀는지에 대한 여부를 확인합니다.
     *
     * @param exchange 확인할 교환 객체
     * @param member   확인할 회원 객체
     * @return         좋아요가 존재하면 true, 그렇지 않으면 false를 반환합니다.
     */
    boolean existsByExchangeAndMember(Exchange exchange, Member member);

    /**
     * 주어진 중고 거래 게시글과 회원에 해당하는 좋아요를 조회합니다.
     *
     * @param exchange 조회할 중고 거래 게시글 객체
     * @param member   조회할 회원 객체
     * @return         해당하는 ExchangeLike 객체를 Optional로 감싸서 반환합니다. 존재하지 않으면 빈 Optional을 반환합니다.
     */
    Optional<ExchangeLike> findByExchangeAndMember(Exchange exchange, Member member);
}
