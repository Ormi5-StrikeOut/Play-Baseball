package org.example.spring.security.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.constants.RateLimitBucketConstants;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RateLimiterService {

    private final Cache<String, Bucket> authenticatedCache;
    private final Cache<String, Bucket> unauthenticatedCache;
    private final JwtTokenValidator jwtTokenValidator;

    public RateLimiterService(JwtTokenValidator jwtTokenValidator) {
        this.jwtTokenValidator = jwtTokenValidator;

        this.authenticatedCache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

        this.unauthenticatedCache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();
    }

    public boolean tryConsume(String token, String ip, String userAgent) {
        if (token != null && jwtTokenValidator.isTokenValid(token)) {
            String email = jwtTokenValidator.extractUsername(token);
            log.info("Authenticated user - Email: {}", email);
            return tryConsumeForUser(email, authenticatedCache, this::createAuthenticatedBucket);
        } else {
            log.info("Unauthenticated user - IP: {}, User-Agent: {}", ip, userAgent);
            String key = ip + "|" + userAgent;
            return tryConsumeForUser(key, unauthenticatedCache, this::createUnauthenticatedBucket);
        }
    }

    private boolean tryConsumeForUser(String key, Cache<String, Bucket> cache, BucketSupplier bucketSupplier) {
        Bucket bucket = cache.get(key, k -> bucketSupplier.get());
        boolean consumed = bucket.tryConsume(RateLimitBucketConstants.TOKEN_CONSUME_AMOUNT.getValue());
        log.info("Rate limited user - Key: {}, Consumed: {}", key, consumed);
        return consumed;
    }

    private Bucket createAuthenticatedBucket() {
        return createBucket(
            RateLimitBucketConstants.AUTHENTICATED_CAPACITY.getValue(),
            RateLimitBucketConstants.REFILL_PERIOD_IN_MINUTES.getDuration()
        );
    }

    private Bucket createUnauthenticatedBucket() {
        return createBucket(
            RateLimitBucketConstants.UNAUTHENTICATED_CAPACITY.getValue(),
            RateLimitBucketConstants.REFILL_PERIOD_IN_MINUTES.getDuration()
        );
    }

    private Bucket createBucket(long capacity, Duration period) {
        Bandwidth limit = Bandwidth.builder()
            .capacity(capacity)
            .refillGreedy(capacity, period)
            .build();
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    @FunctionalInterface
    private interface BucketSupplier {

        Bucket get();
    }
}