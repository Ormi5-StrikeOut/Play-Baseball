package org.example.spring.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.review.Review;
import org.example.spring.domain.reviewOverview.ReviewOverview;
import org.example.spring.repository.ExchangeRepository;
import org.example.spring.repository.MemberRepository;
import org.example.spring.repository.ReviewOverviewRepository;
import org.example.spring.repository.ReviewRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원별 리뷰 개요(ReviewOverview) 정보를 관리하는 서비스 클래스입니다.
 * 이 클래스는 지정된 스케줄에 따라 자동으로 리뷰 통계를 업데이트합니다.
 */
@Service
@RequiredArgsConstructor
public class ReviewOverviewService {
    private final MemberRepository memberRepository;
    private final ExchangeRepository exchangeRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewOverviewRepository reviewOverviewRepository;

    /**
     * 매일 자정(00:00:00)에 실행되어 지난 24시간 동안 완료된 리뷰 정보를 기반으로
     * 회원별 리뷰 개요(ReviewOverview)를 업데이트합니다.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void updateReviewOverviews() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        LocalDateTime startOfYesterday = yesterday.atStartOfDay();
        LocalDateTime endOfYesterday = today.atStartOfDay().minusNanos(1);

        Timestamp startOfYesterdayTimestamp = Timestamp.valueOf(startOfYesterday);
        Timestamp endOfYesterdayTimestamp = Timestamp.valueOf(endOfYesterday);

        List<Exchange> recentlyReviewedExchanges = exchangeRepository.findByReviewedAtBetween(startOfYesterdayTimestamp, endOfYesterdayTimestamp);

        Map<Long, List<Exchange>> exchangesByWriter = recentlyReviewedExchanges.stream().collect(Collectors.groupingBy(exchange -> exchange.getMember().getId()));

        for (Map.Entry<Long, List<Exchange>> entry : exchangesByWriter.entrySet()) {
            Long writerId = entry.getKey();
            List<Exchange> exchanges = entry.getValue();

            long reviewCount = 0;
            double totalRate = 0.0;

            for (Exchange exchange : exchanges) {
                reviewCount++;

                Review review = reviewRepository.findByExchange_Id(exchange.getId());
                totalRate += review.getRate();
            }

            double averageRate = totalRate / reviewCount;

            Member member = memberRepository.findById(writerId).orElseThrow(() -> new RuntimeException("Member not found"));

            ReviewOverview reviewOverview = reviewOverviewRepository.findByMemberId(writerId)
                .orElse(ReviewOverview.builder()
                    .member(member)
                    .count(0L)
                    .average(0.0)
                    .build());

            reviewOverview.updateReviewStats(reviewCount, averageRate);

            reviewOverviewRepository.save(reviewOverview);
        }
    }
}

