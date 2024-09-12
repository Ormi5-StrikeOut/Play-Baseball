package org.example.spring.security.utils;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 인증 관련 유틸리티 메서드를 제공하는 클래스입니다.
 * 주로 클라이언트 IP 주소 추출 및 검증과 관련된 기능을 포함합니다.
 */
@Component
public class AuthUtils {

	private static final List<String> IP_HEADERS = Arrays.asList(
		"X-Forwarded-For",
		"Proxy-Client-IP",
		"WL-Proxy-Client-IP",
		"HTTP_X_FORWARDED_FOR",
		"HTTP_X_FORWARDED",
		"HTTP_X_CLUSTER_CLIENT_IP",
		"HTTP_CLIENT_IP",
		"HTTP_FORWARDED_FOR",
		"HTTP_FORWARDED",
		"HTTP_VIA",
		"REMOTE_ADDR"
	);

	/**
	 * HTTP 요청에서 클라이언트의 실제 IP 주소를 추출합니다.
	 * 여러 헤더를 순차적으로 확인하여 유효한 IP 주소를 찾습니다.
	 *
	 * @param request HTTP 요청 객체
	 * @return 클라이언트의 IP 주소. 유효한 IP를 찾지 못한 경우 요청의 원격 주소를 반환합니다.
	 */
	public String getClientIpAddress(HttpServletRequest request) {
		return IP_HEADERS.stream()
			.map(request::getHeader)
			.filter(ip -> ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip))
			.map(ip -> ip.split(",")[0])
			.findFirst()
			.orElse(request.getRemoteAddr());
	}

	/**
	 * 주어진 문자열이 유효한 IPv4 주소 형식인지 검증합니다.
	 *
	 * @deprecated 이 메서드는 현재 사용되지 않으며, 향후 릴리스에서 제거될 수 있습니다.
	 * IP 주소 검증이 필요한 경우 {@link java.net.InetAddress#getByName(String)}를 사용하는 것을 고려해보세요.
	 *
	 * @param ip 검증할 IP 주소 문자열
	 * @return 유효한 IPv4 주소 형식이면 true, 그렇지 않으면 false
	 */
	@Deprecated
	public boolean isValidIpAddress(String ip) {
		if (ip == null || ip.isEmpty()) {
			return false;
		}

		String[] octets = ip.split("\\.");
		if (octets.length != 4) {
			return false;
		}

		return Arrays.stream(octets)
			.allMatch(octet -> {
				try {
					int value = Integer.parseInt(octet);
					return value >= 0 && value <= 255;
				} catch (NumberFormatException e) {
					return false;
				}
			});
	}
}
