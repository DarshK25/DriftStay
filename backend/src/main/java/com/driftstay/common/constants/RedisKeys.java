package com.driftstay.common.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RedisKeys {

    private static final String PREFIX = "driftstay:";
    private static final String PROPERTY = PREFIX + "property:";
    private static final String SEARCH = PREFIX + "search:";
    private static final String FEATURED = PREFIX + "featured";
    private static final String AMENITIES = PREFIX + "amenities";
    private static final String CITIES = PREFIX + "cities";
    private static final String REVIEWS = PREFIX + "reviews:";
    private static final String TOKEN_BLACKLIST = PREFIX + "blacklist:";
    private static final String RATE_LIMIT = PREFIX + "ratelimit:";
    private static final String BRUTE_FORCE = PREFIX + "bruteforce:";
    private static final String LOCKOUT = PREFIX + "lockout:";

    public static String propertyById(Long id) { return PROPERTY + id; }
    public static String propertyByPublicId(String publicId) { return PROPERTY + "public:" + publicId; }
    public static String search(String hash) { return SEARCH + hash; }
    public static String featured() { return FEATURED; }
    public static String amenities() { return AMENITIES; }
    public static String cities() { return CITIES; }
    public static String reviewsByProperty(Long propertyId) { return REVIEWS + propertyId; }
    public static String tokenBlacklist(String jti) { return TOKEN_BLACKLIST + jti; }
    public static String rateLimit(String ip) { return RATE_LIMIT + ip; }
    public static String bruteForce(String ip) { return BRUTE_FORCE + ip; }
    public static String lockout(String ip) { return LOCKOUT + ip; }
}
