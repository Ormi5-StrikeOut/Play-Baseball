package org.example.spring.security.service;

import java.util.concurrent.TimeUnit;

import org.example.spring.constants.RateLimitBucketConstants;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;

/**
 * Bucket4j와 Caffeine 캐시를 사용하여 요청 속도 제한을 관리하는 서비스 클래스.
 * 이 서비스는 다양한 사용자 또는 IP 주소에 대한 속도 제한 버킷을 생성하고 관리하는 메서드를 제공합니다.
 * <p>
 * 이 클래스는 인증된 사용자와 인증되지 않은 사용자에 대해 서로 다른 속도 제한을 적용합니다.
 * 기본적으로 인증된 사용자에게는 더 높은 요청 한도가 주어집니다.
 * <p>
 * 속도 제한 버킷은 Caffeine 캐시에 저장되며, 1시간 동안 액세스되지 않으면 만료됩니다.
 */
@Slf4j
@Service
public class RateLimiterService {

	private final Cache<String, Bucket> rateLimitCache;
	private final JwtTokenValidator jwtTokenValidator;

	/**
	 * RateLimiterService의 새 인스턴스를 구성합니다.
	 * <p>
	 * 이 생성자는 JWT 토큰 검증기를 주입받아 인증 확인에 사용하고,
	 * Caffeine 캐시를 초기화하여 속도 제한 버킷을 저장합니다.
	 *
	 * @param jwtTokenValidator 인증 확인에 사용할 JWT 토큰 검증기
	 */
	public RateLimiterService(JwtTokenValidator jwtTokenValidator) {
		this.jwtTokenValidator = jwtTokenValidator;

		this.rateLimitCache = Caffeine.newBuilder()
			.expireAfterAccess(1, TimeUnit.HOURS)
			.build();

	}

	/**
	 * 주어진 요청에 대한 속도 제한 버킷에서 토큰 소비를 시도합니다.
	 * <p>
	 * 이 메서드는 요청의 JWT 토큰, IP 주소, 사용자 에이전트를 기반으로 고유한 키를 생성하고,
	 * 해당 키에 대한 버킷에서 토큰을 소비하려고 시도합니다.
	 *
	 * @param token     JWT 토큰 (있는 경우)
	 * @param ip        요청의 IP 주소
	 * @param userAgent 요청의 User-Agent
	 * @return 토큰이 성공적으로 소비되었으면 true, 속도 제한이 초과되었으면 false
	 */
	public boolean tryConsume(String token, String ip, String userAgent) {
		String key = getKey(token, ip, userAgent);
		Bucket bucket = rateLimitCache.get(key, this::createBucket);
		boolean consumed = bucket.tryConsume(RateLimitBucketConstants.TOKEN_CONSUME_AMOUNT.getValue());

		long remainingTokens = bucket.getAvailableTokens();
		long capacity = remainingTokens + RateLimitBucketConstants.TOKEN_CONSUME_AMOUNT.getValue();

		log.debug("Rate limit check - Key: {}, Consumed: {}, Remaining: {}/{}",
			key, consumed, remainingTokens, capacity);

		if (!consumed) {
			log.warn("Rate limit exceeded - Key: {}", key);
		}
		return consumed;
	}

	/**
	 * 제공된 매개변수를 기반으로 속도 제한 버킷에 대한 고유 키를 생성합니다.
	 * <p>
	 * 인증된 요청의 경우 사용자 이름을 키로 사용하고,
	 * 인증되지 않은 요청의 경우 IP 주소와 사용자 에이전트의 조합을 사용합니다.
	 *
	 * @param token     JWT 토큰 (있는 경우)
	 * @param ip        요청의 IP 주소
	 * @param userAgent 요청의 User-Agent
	 * @return 속도 제한 버킷을 식별하는 데 사용되는 문자열 키
	 */
	private String getKey(String token, String ip, String userAgent) {
		return token != null && jwtTokenValidator.validateToken(token)
			? jwtTokenValidator.extractUsername(token)
			: ip + "|" + userAgent;
	}

	/**
	 * 주어진 키에 대한 새로운 속도 제한 버킷을 생성합니다.
	 * <p>
	 * 이 메서드는 요청이 인증되었는지 여부에 따라 다른 용량의 버킷을 생성합니다.
	 * 인증된 요청에는 더 높은 용량의 버킷이 할당됩니다.
	 *
	 * @param key 버킷을 식별하는 키
	 * @return 요청이 인증되었는지 여부에 따라 구성된 새로운 Bucket 인스턴스
	 */
	private Bucket createBucket(String key) {
		boolean isAuthenticated = !key.contains("|");
		long capacity = isAuthenticated
			? RateLimitBucketConstants.AUTHENTICATED_CAPACITY.getValue()
			: RateLimitBucketConstants.UNAUTHENTICATED_CAPACITY.getValue();

		Bandwidth limit = Bandwidth.builder()
			.capacity(capacity)
			.refillGreedy(capacity, RateLimitBucketConstants.REFILL_PERIOD_IN_MINUTES.getDuration())
			.build();
		return Bucket.builder().addLimit(limit).build();
	}
}