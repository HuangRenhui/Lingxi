package com.lingxi.security;

import com.lingxi.common.RedisKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 用户级令牌失效标记回归：改密/禁用/删除后，存量 JWT 必须立即被拒绝；
 * Redis 故障时必须 fail-open，避免一次抖动踢掉全站用户。
 */
class TokenBlacklistServiceTest {

    private static final Long USER_ID = 1L;

    private ValueOperations<String, String> valueOperations;
    private TokenBlacklistService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new TokenBlacklistService(redisTemplate);
    }

    @Test
    void allowsWhenNoInvalidationMark() {
        when(valueOperations.get(RedisKeys.userTokenInvalidBefore(USER_ID))).thenReturn(null);
        assertFalse(service.isIssuedBeforeInvalidation(USER_ID, new Date()));
    }

    @Test
    void rejectsTokenIssuedBeforeMarkAndAllowsTokenIssuedAfter() {
        long mark = System.currentTimeMillis() / 1000L;
        when(valueOperations.get(RedisKeys.userTokenInvalidBefore(USER_ID))).thenReturn(String.valueOf(mark));

        assertTrue(service.isIssuedBeforeInvalidation(USER_ID, new Date((mark - 10) * 1000L)));
        assertFalse(service.isIssuedBeforeInvalidation(USER_ID, new Date((mark + 10) * 1000L)));
    }

    @Test
    void rejectsTokenWithoutIssuedAtWhenMarkExists() {
        when(valueOperations.get(RedisKeys.userTokenInvalidBefore(USER_ID))).thenReturn("1000");
        assertTrue(service.isIssuedBeforeInvalidation(USER_ID, null));
    }

    @Test
    void ignoresNullUserId() {
        assertFalse(service.isIssuedBeforeInvalidation(null, new Date()));
        verifyNoInteractions(valueOperations);
    }

    @Test
    void failsOpenWhenRedisUnavailable() {
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("redis down"));
        assertFalse(service.isIssuedBeforeInvalidation(USER_ID, new Date()));
    }

    @Test
    void writesInvalidationMarkWithTtl() {
        service.invalidateUserTokens(USER_ID, 3600);
        verify(valueOperations).set(eq(RedisKeys.userTokenInvalidBefore(USER_ID)), anyString(),
                eq(Duration.ofSeconds(3600)));
    }

    @Test
    void skipsWriteForInvalidArguments() {
        service.invalidateUserTokens(null, 3600);
        service.invalidateUserTokens(USER_ID, 0);
        verifyNoInteractions(valueOperations);
    }

    @Test
    void swallowsRedisFailureWhenWritingMark() {
        doThrow(new RuntimeException("redis down"))
                .when(valueOperations).set(anyString(), anyString(), eq(Duration.ofSeconds(60)));

        service.invalidateUserTokens(USER_ID, 60);

        verify(valueOperations).set(eq(RedisKeys.userTokenInvalidBefore(USER_ID)), anyString(),
                eq(Duration.ofSeconds(60)));
    }
}
