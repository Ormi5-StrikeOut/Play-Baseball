package org.example.spring.service;

import java.sql.Timestamp;

import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.like.ExchangeLike;
import org.example.spring.domain.like.dto.AddLikeRequest;
import org.example.spring.domain.member.Member;
import org.example.spring.repository.ExchangeLikeRepository;
import org.example.spring.repository.ExchangeRepository;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * 중고 거래 게시글에 대한 좋아요 기능을 처리하는 서비스 클래스입니다.
 * 이 클래스는 좋아요 추가 및 삭제와 관련된 비즈니스 로직을 구현합니다.
 */
@Service
@RequiredArgsConstructor
public class ExchangeLikeService {
	private final ExchangeLikeRepository exchangeLikeRepository;
	private final ExchangeRepository exchangeRepository;
	private final JwtTokenValidator jwtTokenValidator;

	/**
	 * HTTP 요청으로부터 인증된 사용자 정보를 추출합니다.
	 *
	 * @param request           HTTP 요청 객체
	 * @return 인증된 Member 객체
	 * @throws RuntimeException 토큰이 유효하지 않거나 사용자를 찾을 수 없는 경우
	 */
	private Member getAuthenticatedMember(HttpServletRequest request) {
		String token = jwtTokenValidator.extractTokenFromHeader(request);
		return jwtTokenValidator.validateTokenAndGetMember(token);
	}

	/**
	 * 중고 거래 게시글에 대한 좋아요를 추가하거나 상태를 변경합니다.
	 * 이미 좋아요가 존재하는 경우 상태를 토글하고, 존재하지 않는 경우 새로 추가합니다.
	 *
	 * @param request HTTP 요청 객체. 사용자 인증 정보를 포함합니다.
	 * @param addLikeRequest 좋아요 추가 요청 DTO. 좋아요를 추가할 중고 거래 게시글의 ID를 포함합니다.
	 * @return 좋아요 처리 결과. 성공적으로 처리된 경우 true를 반환합니다.
	 */
	@Transactional
	public boolean addLike(HttpServletRequest request, AddLikeRequest addLikeRequest) {
		Member member = getAuthenticatedMember(request);

		Exchange exchange = exchangeRepository.findById(addLikeRequest.getExchangeId())
			.orElseThrow(() -> new RuntimeException("교환 거래를 찾을 수 없습니다."));

		ExchangeLike existingLike = exchangeLikeRepository.findByExchangeAndMember(exchange, member).orElse(null);

		boolean isLike = false;

		if (existingLike != null) {
			if (existingLike.getCanceledAt() == null) {
				isLike = existingLike.toggleLike();
			} else {
				isLike = existingLike.toggleLike();
			}
			exchangeLikeRepository.save(existingLike);
		} else {
			ExchangeLike newLike = ExchangeLike.builder()
				.exchange(exchange)
				.member(member)
				.createdAt(new Timestamp(System.currentTimeMillis()))
				.build();
			exchangeLikeRepository.save(newLike);
			isLike = true;
		}

		return isLike;
	}
}
