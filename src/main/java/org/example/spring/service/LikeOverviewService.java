package org.example.spring.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.like.ExchangeLike;
import org.example.spring.domain.likeOverview.LikeOverview;
import org.example.spring.repository.ExchangeLikeRepository;
import org.example.spring.repository.ExchangeRepository;
import org.example.spring.repository.LikeOverviewRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 중고 거래 게시글의 좋아요 통계 정보를 관리하는 서비스 클래스입니다.
 * 이 클래스는 일일 단위로 좋아요 통계를 업데이트하는 스케줄링된 작업을 수행합니다.
 */
@Service
@RequiredArgsConstructor
public class LikeOverviewService {
    private final ExchangeRepository exchangeRepository;
    private final ExchangeLikeRepository exchangeLikeRepository;
    private final LikeOverviewRepository likeOverviewRepository;

    /**
     * 매 분마다 실행되어 지난 1분 동안의 좋아요 정보를 기반으로
     * 중고 거래 게시글별 좋아요 개요(LikeOverview)를 업데이트합니다
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void updateLikeOverviews() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMinuteAgo = now.minusMinutes(1);

        Timestamp startOfInterval = Timestamp.valueOf(oneMinuteAgo);
        Timestamp endOfInterval = Timestamp.valueOf(now);

        List<ExchangeLike> allLikes = exchangeLikeRepository.findByCreatedAtBetween(startOfInterval, endOfInterval);
        List<ExchangeLike> canceledLikes = exchangeLikeRepository.findByCanceledAtBetween(startOfInterval, endOfInterval);

        Map<Long, List<ExchangeLike>> likesForExchange = allLikes.stream().collect(Collectors.groupingBy(exchangeLike -> exchangeLike.getExchange().getId()));
        Map<Long, List<ExchangeLike>> canceledLikesForExchange = canceledLikes.stream().collect(Collectors.groupingBy(exchangeLike -> exchangeLike.getExchange().getId()));

        for (Map.Entry<Long, List<ExchangeLike>> entry : likesForExchange.entrySet()) {
            Long exchangeId = entry.getKey();
            List<ExchangeLike> exchangeLikes = entry.getValue();
            List<ExchangeLike> exchangeCanceledLikes = canceledLikesForExchange.getOrDefault(exchangeId, List.of());

            long totalLikesCount = exchangeLikes.size();
            long canceledLikesCount = exchangeCanceledLikes.size();
            long resultLikesCount = totalLikesCount - canceledLikesCount;

            Exchange exchange = exchangeRepository.findById(exchangeId).orElseThrow(() -> new RuntimeException("Exchange not found for ID: " + exchangeId));

            LikeOverview likeOverview = likeOverviewRepository.findByExchangeId(exchangeId).orElse(LikeOverview.builder()
                .exchange(exchange)
                .count(0L)
                .build());

            likeOverview.updateLikeStats(resultLikesCount);
            likeOverviewRepository.save(likeOverview);
        }
    }
}
