package org.example.spring.repository;

import java.util.Optional;
import org.example.spring.domain.reviewOverview.ReviewOverview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ReviewOverview 엔티티를 다루는 JPA 리포지토리 인터페이스입니다.
 * 이 리포지토리는 ReviewOverview 데이터를 조회하고 관리하는 메서드를 제공합니다.
 */
@Repository
public interface ReviewOverviewRepository extends JpaRepository<ReviewOverview, Long> {
    /**
     * 주어진 memberId로 ReviewOverview 엔티티를 찾습니다.
     *
     * @param memberId ReviewOverview와 연관된 Member의 ID
     * @return         memberId에 해당하는 ReviewOverview가 있으면 Optional에 담아 반환하고, 없으면 빈 Optional을 반환합니다.
     */
    Optional<ReviewOverview> findByMemberId(Long memberId);
}
