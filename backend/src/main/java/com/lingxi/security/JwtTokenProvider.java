package com.lingxi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与解析。
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /** 会话起始时间戳（秒）claim：刷新令牌时原样传递，用于计算绝对会话上限。 */
    private static final String CLAIM_SESSION_START = "ss";

    private final SecretKey secretKey;
    private final long expireMinutes;
    /** 绝对会话上限（小时）；<=0 表示不限制。 */
    private final long maxSessionHours;

    public JwtTokenProvider(@Value("${lingxi.jwt.secret:}") String secret,
                            @Value("${lingxi.jwt.expire-minutes:720}") long expireMinutes,
                            @Value("${lingxi.jwt.max-session-hours:168}") long maxSessionHours) {
        // 不再提供生产可用的内置密钥：未配置时启动直接失败，避免误用弱密钥上线。
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("JWT 密钥未配置：请通过环境变量 JWT_SECRET 或配置项 lingxi.jwt.secret 设置密钥");
        }
        // HS256 要求密钥长度不少于 256 bit，提前给出明确错误而不是暴露底层 WeakKeyException。
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT 密钥长度不足：HS256 至少需要 32 字节（256 bit）");
        }
        // 开发默认密钥可以启动，但必须显式告警：生产误用等同于公开签名密钥
        if (secret.contains("change-in-production") || secret.contains("lingxi-dev-secret")) {
            log.warn("正在使用开发默认 JWT 密钥，请勿用于生产环境；生产必须通过 JWT_SECRET 注入独立密钥");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMinutes = expireMinutes;
        this.maxSessionHours = maxSessionHours;
    }

    public String generateToken(Long userId, String username, String role) {
        return generateToken(userId, username, role, System.currentTimeMillis() / 1000L);
    }

    /**
     * 签发令牌。
     *
     * @param sessionStartSeconds 会话起始时间（秒）。刷新时必须透传旧令牌中的值，
     *                            否则「无限刷新」会让令牌永久有效。
     */
    public String generateToken(Long userId, String username, String role, long sessionStartSeconds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireMinutes * 60_000L);
        // subject 使用用户 ID 作为唯一标识；用户名和角色放入自定义 claim，减少鉴权时的数据库查询。
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .claim(CLAIM_SESSION_START, sessionStartSeconds)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 会话起始时间（秒）。老令牌没有 ss claim 时回退到 iat。
     */
    public long sessionStartSeconds(Claims claims) {
        Number raw = claims.get(CLAIM_SESSION_START, Number.class);
        if (raw != null) {
            return raw.longValue();
        }
        Date issuedAt = claims.getIssuedAt();
        return issuedAt == null ? System.currentTimeMillis() / 1000L : issuedAt.getTime() / 1000L;
    }

    /**
     * 是否已超过绝对会话上限。
     *
     * <p>仅有既有的「滑动过期」时，只要持续活跃就能无限续期；
     * 加上绝对上限后，即使令牌一直有效，超过 maxSessionHours 也必须重新登录。</p>
     */
    public boolean isSessionExpired(Claims claims) {
        if (maxSessionHours <= 0) {
            return false;
        }
        long elapsed = System.currentTimeMillis() / 1000L - sessionStartSeconds(claims);
        return elapsed > maxSessionHours * 3600L;
    }

    public Claims parseToken(String token) {
        // parseClaimsJws 会同时校验签名与过期时间，任一不通过都会抛出 JwtException 的子类。
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getExpireMinutes() {
        return expireMinutes;
    }
}
