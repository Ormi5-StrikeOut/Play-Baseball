package org.example.spring.repository;

import org.example.spring.domain.review.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 리뷰 엔티티에 대한 데이터 액세스 계층을 제공하는 Spring Data JPA 리포지토리 인터페이스입니다.
 * 이 인터페이스에는 리뷰 관련 CRUD 및 검색 메서드가 정의되어 있습니다.
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    /**
     * 특정 회원이 작성한 리뷰 목록을 페이징 처리하여 조회합니다.
     *
     * @param writerId 리뷰를 작성한 회원의 ID
     * @param pageable 페이징 정보
     * @return         특정 회원의 리뷰 목록 (페이지 단위)
     */
    Page<Review> findByWriter_Id(Long writerId, Pageable pageable);
}
