package com.driftstay.common.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityConstants {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String ROLE_PREFIX = "ROLE_";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MODERATOR = "MODERATOR";
    public static final String ROLE_USER = "USER";

    public static final String ADMIN_AUTHORITY = ROLE_PREFIX + ROLE_ADMIN;

    // Public endpoints that don't require authentication
    public static final String[] PUBLIC_ENDPOINTS = {
            ApiConstants.AUTH_BASE + "/**",
            ApiConstants.PROPERTIES_BASE + "/**",
            ApiConstants.REVIEWS_BASE + "/property/**",
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
    };

    // Rate limiting
    public static final int RATE_LIMIT_MAX_REQUESTS = 100;
    public static final int RATE_LIMIT_WINDOW_SECONDS = 60;

    // Brute force
    public static final int BRUTE_FORCE_MAX_ATTEMPTS = 5;
    public static final int BRUTE_FORCE_LOCKOUT_MINUTES = 15;
    public static final int BRUTE_FORCE_WINDOW_SECONDS = 60;
}
