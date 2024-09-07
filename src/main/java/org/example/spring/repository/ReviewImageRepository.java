package org.example.spring.repository;

import org.example.spring.domain.reviewImage.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    int countByReview_Id(Long reviewId);
}
