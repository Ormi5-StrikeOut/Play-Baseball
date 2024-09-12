package org.example.spring.security.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

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

    public String getClientIpAddress(HttpServletRequest request) {
        return IP_HEADERS.stream()
            .map(request::getHeader)
            .filter(ip -> ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip))
            .map(ip -> ip.split(",")[0])
            .findFirst()
            .orElse(request.getRemoteAddr());
    }

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
