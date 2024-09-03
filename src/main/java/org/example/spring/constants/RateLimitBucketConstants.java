package org.example.spring.constants;

import java.time.Duration;
import lombok.Getter;

@Getter
public enum RateLimitBucketConstants {
    AUTHENTICATED_CAPACITY(100),
    UNAUTHENTICATED_CAPACITY(30),
    REFILL_PERIOD_IN_MINUTES(1),
    TOKEN_CONSUME_AMOUNT(1);

    private final long value;

    RateLimitBucketConstants(long value) {
        this.value = value;
    }

    public Duration getDuration() {
        return Duration.ofMinutes(value);
    }
}
