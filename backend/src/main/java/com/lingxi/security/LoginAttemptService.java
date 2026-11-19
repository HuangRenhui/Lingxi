package com.lingxi.security;

import com.lingxi.common.BusinessException;
import com.lingxi.common.RedisKeys;
import com.lingxi.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录失败限流：5 次失败锁定 5 分钟。
 *
 * <p>计数优先写入 Redis；Redis 异常时降级为进程内计数，保证认证链路不因缓存故障中断。</p>
 */
@Slf4j
@Component
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;
    /** Redis 不可用时的进程内兜底计数，重启后自然失效。 */
    private final ConcurrentHashMap<String, FallbackCounter> fallbackCounters = new ConcurrentHashMap<>();

    public LoginAttemptService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 登录前检查是否已锁定；Redis 异常时按本地计数判断，不阻断登录。
     */
    public void checkLocked(String username) {
        if (getCount(username) >= MAX_ATTEMPTS) {
            throw new BusinessException(ResultCode.LOGIN_LOCKED);
        }
    }

    /**
     * 记录一次登录失败。
     */
    public void onFailure(String username) {
        String key = RedisKeys.loginFail(username);
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            // 首次失败时设置过期时间；后续失败复用同一窗口，避免每次都延长锁定时长。
            if (count != null && count == 1L) {
                redisTemplate.expire(key, LOCK_DURATION);
            }
        } catch (RuntimeException ex) {
            log.warn("Redis 不可用，登录失败计数降级为进程内计数: {}", ex.getMessage());
            incrementFallback(username);
        }
    }

    /**
     * 登录成功后清除失败计数。
     */
    public void onSuccess(String username) {
        try {
            redisTemplate.delete(RedisKeys.loginFail(username));
        } catch (RuntimeException ex) {
            log.warn("Redis 不可用，跳过登录失败计数清理: {}", ex.getMessage());
        }
        fallbackCounters.remove(username);
    }

    private int getCount(String username) {
        try {
            String value = redisTemplate.opsForValue().get(RedisKeys.loginFail(username));
            return value == null ? 0 : Integer.parseInt(value);
        } catch (RuntimeException ex) {
            log.warn("Redis 不可用，使用进程内计数判断登录锁定: {}", ex.getMessage());
            return fallbackCount(username);
        }
    }

    private void incrementFallback(String username) {
        long now = System.currentTimeMillis();
        fallbackCounters.compute(username, (key, existing) -> {
            if (existing == null || existing.expireAt <= now) {
                return new FallbackCounter(now + LOCK_DURATION.toMillis(), 1);
            }
            return new FallbackCounter(existing.expireAt, existing.count + 1);
        });
    }

    private int fallbackCount(String username) {
        FallbackCounter counter = fallbackCounters.get(username);
        if (counter == null) {
            return 0;
        }
        if (counter.expireAt <= System.currentTimeMillis()) {
            fallbackCounters.remove(username, counter);
            return 0;
        }
        return counter.count;
    }

    private static final class FallbackCounter {
        private final long expireAt;
        private final int count;

        private FallbackCounter(long expireAt, int count) {
            this.expireAt = expireAt;
            this.count = count;
        }
    }
}
