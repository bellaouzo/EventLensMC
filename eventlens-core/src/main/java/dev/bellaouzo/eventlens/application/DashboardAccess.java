package dev.bellaouzo.eventlens.application;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class DashboardAccess {

    public static final String QUERY_PARAM = "token";
    public static final String COOKIE_NAME = "eventlens_dashboard";
    public static final String BEARER_PREFIX = "Bearer ";

    private DashboardAccess() {}

    public static Optional<String> presentedToken(String authorization, String cookieHeader, String rawQuery) {
        String bearer = bearerToken(authorization);
        if (bearer != null) {
            return Optional.of(bearer);
        }
        String query = queryToken(rawQuery);
        if (query != null) {
            return Optional.of(query);
        }
        String cookie = cookieToken(cookieHeader);
        if (cookie != null) {
            return Optional.of(cookie);
        }
        return Optional.empty();
    }

    public static String queryToken(String rawQuery) {
        return firstNamedValue(rawQuery, "&", QUERY_PARAM);
    }

    public static String sessionCookie(String token) {
        return COOKIE_NAME
                + "="
                + URLEncoder.encode(token, StandardCharsets.UTF_8)
                + "; Path=/; HttpOnly; SameSite=Strict; Max-Age=86400";
    }

    private static String bearerToken(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }
        String value = authorization.substring(BEARER_PREFIX.length()).trim();
        return value.isEmpty() ? null : value;
    }

    private static String cookieToken(String cookieHeader) {
        return firstNamedValue(cookieHeader, ";", COOKIE_NAME);
    }

    private static String firstNamedValue(String raw, String separator, String expectedName) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        for (String pair : raw.split(separator, -1)) {
            int equals = pair.indexOf('=');
            if (equals <= 0) {
                continue;
            }
            String name = decode(pair.substring(0, equals).trim());
            if (!expectedName.equals(name)) {
                continue;
            }
            String value = decode(pair.substring(equals + 1).trim());
            return value.isEmpty() ? null : value;
        }
        return null;
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ignored) {
            return value;
        }
    }
}
