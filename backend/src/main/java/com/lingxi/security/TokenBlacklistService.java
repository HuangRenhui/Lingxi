package com.lingxi.security;

import com.lingxi.common.RedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * 已注销令牌黑名单。
 *
 * <p>黑名单仅用于让未过期的 JWT 提前失效；Redis 不可用时忽略相关操作，避免阻断登录与业务请求。</p>
 */
@Slf4j
@Component
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 将令牌加入黑名单，TTL 为令牌剩余有效期；已过期的令牌无需写入。
     */
    public void blacklist(String token, long ttlSeconds) {
        if (!StringUtils.hasText(token) || ttlSeconds <= 0) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(RedisKeys.tokenBlacklist(token), "1", Duration.ofSeconds(ttlSeconds));
        } catch (RuntimeException ex) {
            // 不记录 token 本身，避免日志泄露凭证；用 error 级别保证监控能告警，
            // 否则「注销成功但令牌仍可用」会静默发生。
            log.error("Redis 不可用，令牌未能加入黑名单，该令牌在过期前仍然有效", ex);
        }
    }

    /**
     * 判断令牌是否已注销。
     *
     * <p>Redis 异常时放行（fail-open）：黑名单只是「提前失效」的优化，
     * 若改为 fail-closed，一次 Redis 抖动会让全站已登录用户同时被踢下线。
     * 代价是故障期间已注销的令牌会恢复可用，因此这里用 error 级别记录并依赖告警
     * 促使运维尽快恢复 Redis。生产环境已配置连接与读取超时，避免请求被长时间阻塞。</p>
     */
    public boolean isBlacklisted(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeys.tokenBlacklist(token)));
        } catch (RuntimeException ex) {
            log.error("Redis 不可用，本次跳过令牌黑名单校验（已注销令牌可能短暂可用）", ex);
            return false;
        }
    }
}
