package org.example.spring.security.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TokenBlacklistService {
    private final Cache<String, Date> tokenBlacklist;

    public TokenBlacklistService() {
        this.tokenBlacklist = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofDays(1)) // 토큰을 블랙리스트에 추가한 후 1일 후에 자동으로 제거
            .maximumSize(10_000)
            .build();
    }

    /**
     * 토큰을 블랙리스트에 추가합니다.
     *
     * @param token 블랙리스트에 추가할 토큰
     * @param expirationDate 토큰의 만료 일자
     */
    public void addToBlacklist(String token, Date expirationDate) {
        if (expirationDate.after(new Date())) {
            tokenBlacklist.put(token, expirationDate);
            log.info("Token added to blacklist. Expires at: {}", expirationDate);
        } else {
            log.warn("Attempted to blacklist an already expired token");
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인합니다.
     *
     * @param token 확인할 토큰
     * @return 블랙리스트에 있으면 true, 그렇지 않으면 false
     */
    public boolean isTokenBlacklisted(String token) {
        Date expirationDate = tokenBlacklist.getIfPresent(token);
        boolean isBlacklisted = expirationDate != null;
        log.debug("Checking if token is blacklisted. Result: {}", isBlacklisted);
        return isBlacklisted;
    }

    /**
     * 만료된 토큰들을 블랙리스트에서 제거합니다.
     * 이 메서드는 주기적으로 실행되어야 합니다.
     */
    public void removeExpiredTokens() {
        tokenBlacklist.cleanUp();
        log.info("Expired tokens removed from blacklist");
    }

    /**
     * 특정 토큰을 블랙리스트에서 제거합니다.
     *
     * @param token 제거할 토큰
     */
    public void removeFromBlacklist(String token) {
        tokenBlacklist.invalidate(token);
        log.info("Token removed from blacklist: {}", token);
    }
}
