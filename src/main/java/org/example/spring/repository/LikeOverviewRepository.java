package org.example.spring.repository;

import java.util.Optional;
import org.example.spring.domain.likeOverview.LikeOverview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * LikeOverview 엔티티에 대한 데이터 접근 작업을 처리하는 리포지토리 인터페이스입니다.
 * 이 인터페이스는 JpaRepository를 확장하여 기본적인 CRUD 작업과 추가적인 커스텀 쿼리 메소드를 제공합니다.
 */
@Repository
public interface LikeOverviewRepository extends JpaRepository<LikeOverview, Long> {
    /**
     * 주어진 교환 게시글 ID에 해당하는 LikeOverview를 찾아 반환합니다.
     *
     * @param exchangeId 조회할 교환 게시글의 ID
     * @return 해당하는 LikeOverview 객체를 포함한 Optional
     */
    Optional<LikeOverview> findByExchangeId(Long exchangeId);
}
