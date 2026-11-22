package com.lingxi.security;

import com.lingxi.common.RedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 已注销令牌黑名单。
 *
 * <p>黑名单仅用于让未过期的 JWT 提前失效；Redis 不可用时忽略相关操作，避免阻断登录与业务请求。</p>
 */
@Slf4j
@Component
public class TokenBlacklistService {

    /** 单实例兜底：Redis 不可用时，本机仍能拒绝凭据变更前签发的令牌。 */
    private static final long LOCAL_RETENTION_SECONDS = 7 * 24 * 60 * 60L;
    private final Map<Long, Long> localInvalidBefore = new ConcurrentHashMap<>();

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

    /**
     * 让某用户在当前时刻之前签发的所有令牌立即失效。
     *
     * <p>用于改密、禁用、删除账号等凭据变更场景：单点注销无法枚举该用户名下的全部令牌，
     * 因此记录一个「失效时间点」，由过滤器按令牌签发时间统一拒绝。</p>
     *
     * @param ttlSeconds 失效标记保留时长，应覆盖令牌最长剩余有效期
     */
    public void invalidateUserTokens(Long userId, long ttlSeconds) {
        if (userId == null || ttlSeconds <= 0) {
            return;
        }
        long nowSeconds = System.currentTimeMillis() / 1000L;
        // 先写本机兜底，Redis 故障时同一实例上的校验仍能生效
        purgeLocalIfNeeded(nowSeconds);
        localInvalidBefore.put(userId, nowSeconds);
        try {
            redisTemplate.opsForValue().set(RedisKeys.userTokenInvalidBefore(userId),
                    String.valueOf(nowSeconds), Duration.ofSeconds(ttlSeconds));
        } catch (RuntimeException ex) {
            // 与黑名单策略保持一致：Redis 故障时不阻断业务，但必须告警
            log.error("Redis 不可用，用户 {} 的存量令牌未能立即失效", userId, ex);
        }
    }

    /**
     * 判断令牌是否签发于用户凭据变更之前。
     *
     * <p>按秒比较：JWT 的 iat 精度为秒，使用秒级时间点可避免误伤同一秒内新签发的令牌。</p>
     */
    public boolean isIssuedBeforeInvalidation(Long userId, Date issuedAt) {
        if (userId == null) {
            return false;
        }
        Long local = localInvalidBefore.get(userId);
        if (local != null && (issuedAt == null || issuedAt.getTime() / 1000L < local)) {
            return true;
        }
        try {
            String value = redisTemplate.opsForValue().get(RedisKeys.userTokenInvalidBefore(userId));
            if (!StringUtils.hasText(value)) {
                return false;
            }
            long invalidBefore = Long.parseLong(value);
            // 无签发时间的令牌无法证明自己是变更后签发的，直接拒绝
            return issuedAt == null || issuedAt.getTime() / 1000L < invalidBefore;
        } catch (RuntimeException ex) {
            log.error("Redis 不可用，本次跳过用户令牌失效校验", ex);
            return false;
        }
    }

    /**
     * 防止本机兜底数据无限增长：仅在写入时清理过期条目。
     */
    private void purgeLocalIfNeeded(long nowSeconds) {
        if (localInvalidBefore.size() < 1024) {
            return;
        }
        long expireBefore = nowSeconds - LOCAL_RETENTION_SECONDS;
        localInvalidBefore.entrySet().removeIf(entry -> entry.getValue() < expireBefore);
    }
}
