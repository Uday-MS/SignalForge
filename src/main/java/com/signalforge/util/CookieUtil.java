package com.signalforge.util;

import org.springframework.http.ResponseCookie;

/**
 * Shared cookie utility to avoid duplicating refresh cookie logic
 * across AuthController and OAuthController.
 */
public final class CookieUtil {

    private CookieUtil() {}

    public static ResponseCookie buildRefreshCookie(String value, long maxAgeSeconds, boolean secure) {
        return ResponseCookie.from("refreshToken", value)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite(secure ? "None" : "Lax")
                .build();
    }
}
