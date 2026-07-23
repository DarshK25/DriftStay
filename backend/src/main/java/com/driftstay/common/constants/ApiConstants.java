package com.driftstay.common.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiConstants {

    public static final String API_V1 = "/v1";

    // Auth
    public static final String AUTH_BASE = API_V1 + "/auth";
    public static final String REGISTER = "/register";
    public static final String LOGIN = "/login";
    public static final String REFRESH = "/refresh";
    public static final String LOGOUT = "/logout";
    public static final String LOGOUT_ALL = "/logout-all";
    public static final String FORGOT_PASSWORD = "/forgot-password";
    public static final String RESET_PASSWORD = "/reset-password";
    public static final String VERIFY_EMAIL = "/verify-email";
    public static final String SESSIONS = "/sessions";
    public static final String ADMIN_LOGOUT_USER = "/admin/logout-user/{userId}";

    // Properties
    public static final String PROPERTIES_BASE = API_V1 + "/properties";
    public static final String SEARCH = "/search";
    public static final String FEATURED = "/featured";
    public static final String CITIES = "/cities";
    public static final String SLUG = "/slug/{slug}";

    // Bookings
    public static final String BOOKINGS_BASE = API_V1 + "/bookings";
    public static final String CANCEL = "/{publicId}/cancel";

    // Reviews
    public static final String REVIEWS_BASE = API_V1 + "/reviews";
    public static final String APPROVE = "/{publicId}/approve";
    public static final String REJECT = "/{publicId}/reject";
    public static final String MY_REVIEWS = "/my";
    public static final String PROPERTY_REVIEWS = "/property/{propertyId}";

    // Users
    public static final String USERS_BASE = API_V1 + "/users";
    public static final String ME = "/me";

    // Payments
    public static final String PAYMENTS_BASE = API_V1 + "/payments";
    public static final String CONFIRM = "/{providerPaymentId}/confirm";
    public static final String FAIL = "/{providerPaymentId}/fail";

    // Pagination defaults
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;
    public static final String DEFAULT_SORT = "createdAt";
    public static final String DEFAULT_SORT_DIR = "desc";

    // Common
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
}
