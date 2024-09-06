package org.example.spring.repository;

import org.example.spring.domain.exchange.Exchange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {

    Page<Exchange> findAll(Pageable pageable);

    // 검색 키워드에 포함되어 있는 게시글 목록 조회
    Page<Exchange> findByTitleContaining(String title, Pageable pageable);

    // 최근 5개의 게시글 조회
    List<Exchange> findTop5ByOrderByCreatedAtDesc();

    // 특정 회원이 작성한 게시글 조회
    Page<Exchange> findByWriterId(Long memberId, Pageable pageable);
}
