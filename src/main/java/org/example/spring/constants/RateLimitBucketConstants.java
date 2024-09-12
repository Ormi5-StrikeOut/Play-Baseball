package org.example.spring.constants;

import java.time.Duration;
import lombok.Getter;

@Getter
public enum RateLimitBucketConstants {
    AUTHENTICATED_CAPACITY(150),
    UNAUTHENTICATED_CAPACITY(50),
    REFILL_PERIOD_IN_MINUTES(3),
    TOKEN_CONSUME_AMOUNT(1);

    private final long value;

    RateLimitBucketConstants(long value) {
        this.value = value;
    }

    public Duration getDuration() {
        return Duration.ofMinutes(value);
    }
}
