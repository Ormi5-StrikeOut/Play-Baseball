package org.example.spring.repository;

import org.example.spring.domain.exchange.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {

    // 최근 5개의 게시글 조회
    List<Exchange> findTop5ByOrderByCreatedAtDesc();

    // 특정 회원이 작성한 게시글 조회
    List<Exchange> findByWriterId(Long memberId);

}
