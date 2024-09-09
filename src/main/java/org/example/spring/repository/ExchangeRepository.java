package org.example.spring.repository;

import java.sql.Timestamp;
import java.util.List;
import org.example.spring.domain.exchange.Exchange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** DeletedAt이 null인 게시물을 대상으로 글을 가져옵니다. 게시물 삭제는 Soft Delete를 사용합니다. */
@Repository
public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
  Page<Exchange> findByDeletedAtIsNull(Pageable pageable);

  // 검색 키워드에 포함되어 있는 게시글 목록 조회
  Page<Exchange> findByTitleContainingAndDeletedAtIsNull(String title, Pageable pageable);

  // 최근 5개의 게시글 조회
  List<Exchange> findTop5ByDeletedAtIsNullOrderByCreatedAtDesc();

  // 특정 회원이 작성한 게시글 조회
  Page<Exchange> findByMemberIdAndDeletedAtIsNull(Long memberId, Pageable pageable);

  // reviewedAt 필드가 주어진 두 Timestamp 사이에 있는 Exchange를 조회
  List<Exchange> findByReviewedAtBetween(Timestamp startTimestamp, Timestamp endTimestamp);
}
