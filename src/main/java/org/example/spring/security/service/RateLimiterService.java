package org.example.spring.security.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BandwidthBuilder;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.constants.RateLimitBucketConstants;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RateLimiterService {

    private final Map<String, Bucket> unauthenticatedBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> authenticatedBuckets = new ConcurrentHashMap<>();
    private final JwtTokenValidator jwtTokenValidator;

    public RateLimiterService(JwtTokenValidator jwtTokenValidator) {
        this.jwtTokenValidator = jwtTokenValidator;
    }

    public boolean tryConsume(String token, String ip, String userAgent) {
        if (token != null && !jwtTokenValidator.isTokenExpired(token) && jwtTokenValidator.isTokenValid(token)) {
            // 인증된 사용자
            String email = jwtTokenValidator.extractUsername(token);
            log.info("Authenticated user - Email: {}", email);
            return tryConsumeAuthenticated(email);
        } else {
            // 인증되지 않은 사용자
            log.info("Unauthenticated user - IP: {}, User-Agent: {}", ip, userAgent);
            return tryConsumeUnauthenticated(ip, userAgent);
        }
    }

    private boolean tryConsumeAuthenticated(String email) {
        Bucket bucket = authenticatedBuckets.computeIfAbsent(email, this::createAuthenticatedBucket);
        boolean consumed = bucket.tryConsume(RateLimitBucketConstants.TOKEN_CONSUME_AMOUNT.getValue());
        log.info("Authenticated user - Email: {}, Consumed: {}", email, consumed);
        return consumed;
    }

    private boolean tryConsumeUnauthenticated(String ip, String userAgent) {
        String key = ip + "|" + userAgent;
        Bucket bucket = unauthenticatedBuckets.computeIfAbsent(key, this::createUnauthenticatedBucket);
        boolean consumed = bucket.tryConsume(RateLimitBucketConstants.TOKEN_CONSUME_AMOUNT.getValue());
        log.info("Unauthenticated user - IP: {}, User-Agent: {}, Consumed: {}", ip, userAgent, consumed);
        return consumed;
    }

    private Bucket createAuthenticatedBucket(String email) {
        long capacity = RateLimitBucketConstants.AUTHENTICATED_CAPACITY.getValue();
        Duration period = RateLimitBucketConstants.REFILL_PERIOD_IN_MINUTES.getDuration();

        Bandwidth limit = BandwidthBuilder.builder()
            .capacity(capacity)
            .refillGreedy(capacity, period)
            .build();

        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    private Bucket createUnauthenticatedBucket(String key) {
        long capacity = RateLimitBucketConstants.UNAUTHENTICATED_CAPACITY.getValue();
        Duration period = RateLimitBucketConstants.REFILL_PERIOD_IN_MINUTES.getDuration();

        Bandwidth limit = BandwidthBuilder.builder()
            .capacity(capacity)
            .refillGreedy(capacity, period)
            .build();

        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

}
