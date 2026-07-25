package com.driftstay.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.UtilityClass;

/**
 * Basic user-agent parser for extracting device and browser info.
 * For production, consider using a dedicated library like ua-parser.
 */
@UtilityClass
public class UserAgentUtil {

    public static UserAgentInfo parse(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return UserAgentInfo.builder()
                    .deviceType("UNKNOWN")
                    .browser("UNKNOWN")
                    .os("UNKNOWN")
                    .build();
        }

        String ua = userAgent.toLowerCase();

        String browser = "UNKNOWN";
        if (ua.contains("chrome") && !ua.contains("edg")) browser = "CHROME";
        else if (ua.contains("firefox")) browser = "FIREFOX";
        else if (ua.contains("safari") && !ua.contains("chrome")) browser = "SAFARI";
        else if (ua.contains("edg")) browser = "EDGE";
        else if (ua.contains("opera") || ua.contains("opr")) browser = "OPERA";

        String os = "UNKNOWN";
        if (ua.contains("windows")) os = "WINDOWS";
        else if (ua.contains("mac os") || ua.contains("macos")) os = "MACOS";
        else if (ua.contains("linux")) os = "LINUX";
        else if (ua.contains("android")) os = "ANDROID";
        else if (ua.contains("iphone") || ua.contains("ipad")) os = "IOS";

        String deviceType = "DESKTOP";
        if (ua.contains("mobile")) deviceType = "MOBILE";
        else if (ua.contains("tablet") || ua.contains("ipad")) deviceType = "TABLET";

        return UserAgentInfo.builder()
                .deviceType(deviceType)
                .browser(browser)
                .os(os)
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UserAgentInfo {
        private final String deviceType;
        private final String browser;
        private final String os;
    }
}
