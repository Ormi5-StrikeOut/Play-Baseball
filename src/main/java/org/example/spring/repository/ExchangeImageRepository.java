package org.example.spring.repository;

import org.example.spring.domain.exchangeImage.ExchangeImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeImageRepository extends JpaRepository<ExchangeImage, Long> {
}
