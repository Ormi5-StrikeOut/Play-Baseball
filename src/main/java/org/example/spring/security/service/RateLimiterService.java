package org.example.spring.security.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.constants.RateLimitBucketConstants;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RateLimiterService {

    private final Cache<String, Bucket> rateLimitCache;
    private final JwtTokenValidator jwtTokenValidator;

    public RateLimiterService(JwtTokenValidator jwtTokenValidator) {
        this.jwtTokenValidator = jwtTokenValidator;

        this.rateLimitCache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    }

    public boolean tryConsume(String token, String ip, String userAgent) {
        String key = token != null && jwtTokenValidator.isTokenValid(token)
            ? jwtTokenValidator.extractUsername(token)
            : ip + "|" + userAgent;

        Bucket bucket = rateLimitCache.get(key, this::createBucket);
        boolean consumed = bucket.tryConsume(RateLimitBucketConstants.TOKEN_CONSUME_AMOUNT.getValue());
        log.info("Rate limited user - Key: {}, Consumed: {}", key, consumed);
        return consumed;
    }

    private Bucket createBucket(String key) {
        boolean isAuthenticated = key.contains("|");
        long capacity = isAuthenticated
            ? RateLimitBucketConstants.AUTHENTICATED_CAPACITY.getValue()
            : RateLimitBucketConstants.UNAUTHENTICATED_CAPACITY.getValue();

        Bandwidth limit = Bandwidth.builder()
            .capacity(capacity)
            .refillGreedy(capacity, RateLimitBucketConstants.REFILL_PERIOD_IN_MINUTES.getDuration())
            .build();
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
}