package com.lingxi.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 令牌签发与绝对会话上限回归。
 */
class JwtTokenProviderTest {

    private static final String SECRET = "lingxi-test-secret-key-with-32-bytes-minimum";

    @Test
    void rejectsBlankOrShortSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider("", 720, 168));
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider("short", 720, 168));
    }

    @Test
    void keepsSessionStartAcrossRotation() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 720, 168);
        long sessionStart = System.currentTimeMillis() / 1000L - 3600;
        String token = provider.generateToken(1L, "demo", "USER", sessionStart);

        Claims claims = provider.parseToken(token);
        assertEquals(sessionStart, provider.sessionStartSeconds(claims));
        assertFalse(provider.isSessionExpired(claims));
    }

    @Test
    void detectsSessionBeyondAbsoluteLimit() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 720, 1);
        // 会话已持续 2 小时，超过 1 小时上限：即使令牌本身未过期也必须重新登录
        long sessionStart = System.currentTimeMillis() / 1000L - 2 * 3600;
        String token = provider.generateToken(1L, "demo", "USER", sessionStart);
        assertTrue(provider.isSessionExpired(provider.parseToken(token)));
    }

    @Test
    void zeroLimitDisablesAbsoluteSessionCheck() {
        JwtTokenProvider provider = new JwtTokenProvider(SECRET, 720, 0);
        long sessionStart = System.currentTimeMillis() / 1000L - 365L * 24 * 3600;
        String token = provider.generateToken(1L, "demo", "USER", sessionStart);
        assertFalse(provider.isSessionExpired(provider.parseToken(token)));
    }
}
