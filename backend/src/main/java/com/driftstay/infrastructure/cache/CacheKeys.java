package com.driftstay.infrastructure.cache;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CacheKeys {

    private static final String PREFIX = "driftstay:";
    private static final String PROPERTY = PREFIX + "property:";
    private static final String SEARCH = PREFIX + "search:";
    private static final String FEATURED = PREFIX + "featured";
    private static final String AMENITIES = PREFIX + "amenities";
    private static final String CITIES = PREFIX + "cities";
    private static final String REVIEWS = PREFIX + "reviews:";

    public static String propertyById(Long id) {
        return PROPERTY + id;
    }

    public static String propertyByPublicId(String publicId) {
        return PROPERTY + "public:" + publicId;
    }

    public static String search(String queryHash) {
        return SEARCH + queryHash;
    }

    public static String featured() {
        return FEATURED;
    }

    public static String amenities() {
        return AMENITIES;
    }

    public static String cities() {
        return CITIES;
    }

    public static String reviewsByProperty(Long propertyId) {
        return REVIEWS + propertyId;
    }
}
