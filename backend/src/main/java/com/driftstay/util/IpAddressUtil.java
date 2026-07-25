package com.driftstay.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

/**
 * Utility for extracting the real client IP address from HTTP requests.
 * Handles requests that pass through proxies, load balancers, and CDNs.
 */
@UtilityClass
public class IpAddressUtil {

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    /**
     * Extract the real client IP address from the request.
     * Checks common proxy headers before falling back to getRemoteAddr().
     */
    public static String getClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs: client, proxy1, proxy2
                // Take the first one (the actual client)
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Check if the IP is a private/internal address.
     */
    public static boolean isInternalIp(String ip) {
        return ip.startsWith("10.") ||
                ip.startsWith("172.16.") ||
                ip.startsWith("192.168.") ||
                ip.equals("127.0.0.1") ||
                ip.equals("::1") ||
                ip.equals("0:0:0:0:0:0:0:1");
    }
}
