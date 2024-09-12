package org.example.spring.security.service;

import java.time.Duration;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;

/**
 * 토큰 블랙리스트 관리를 위한 서비스 클래스입니다.
 * 무효화된 토큰을 관리하고 검증하는 기능을 제공합니다.
 */
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
	 * @param token          블랙리스트에 추가할 토큰
	 * @param expirationDate 토큰의 만료 일자
	 */
	public void addToBlacklist(String token, Date expirationDate) {
		if (expirationDate.after(new Date())) {
			tokenBlacklist.put(token, expirationDate);
			log.debug("Token added to blacklist. Expires at: {}", expirationDate);
			log.debug("Blacklist count: {} ", tokenBlacklist.estimatedSize());
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

}
