package com.driftstay.security.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

/**
 * Extracts the real client IP address from HTTP requests.
 * Handles reverse proxies and load balancers by checking standard headers.
 */
@UtilityClass
public class IpAddressUtil {

    private static final String[] PROXY_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    /**
     * Get the real client IP address.
     * Checks proxy headers first, falls back to request.getRemoteAddr().
     */
    public static String getClientIp(HttpServletRequest request) {
        for (String header : PROXY_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs: client, proxy1, proxy2
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
