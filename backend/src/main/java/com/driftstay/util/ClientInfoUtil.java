package com.driftstay.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Extracts and structures client information from HTTP requests.
 * Used for analytics, security auditing, and personalization.
 */
public class ClientInfoUtil {

    public static ClientInfo extract(HttpServletRequest request) {
        return ClientInfo.builder()
                .ipAddress(IpAddressUtil.getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .referer(request.getHeader("Referer"))
                .acceptLanguage(request.getHeader("Accept-Language"))
                .method(request.getMethod())
                .path(request.getRequestURI())
                .queryString(request.getQueryString())
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ClientInfo {
        private final String ipAddress;
        private final String userAgent;
        private final String referer;
        private final String acceptLanguage;
        private final String method;
        private final String path;
        private final String queryString;
    }
}
