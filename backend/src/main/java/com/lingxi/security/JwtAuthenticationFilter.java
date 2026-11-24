package com.lingxi.security;

import com.lingxi.entity.SysUser;
import com.lingxi.mapper.SysUserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * JWT 认证过滤器：解析 Authorization: Bearer &lt;token&gt; 并写入 SecurityContext。
 *
 * <p>为避免「账号被禁用/降级后旧令牌仍可用到过期」，请求会回查用户状态与角色，
 * 身份信息以数据库为准；查询失败时按未认证处理（fail-closed），不放行可疑请求。</p>
 *
 * <p>回查结果带 5 秒本地缓存：每个请求都打一次库会让 DB 成为吞吐瓶颈（尤其是管理端并发轮询），
 * 5 秒的可接受代价是「禁用/降级最迟 5 秒后生效」。</p>
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    /** 用户状态回查缓存有效期。取值越小越实时，越大越省库。 */
    private static final long USER_CACHE_TTL_MS = 5000L;
    /** 缓存容量上限，超出后先清理过期项，防止长期运行下无限增长。 */
    private static final int USER_CACHE_MAX_SIZE = 2000;

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final SysUserMapper sysUserMapper;
    private final ConcurrentMap<Long, CachedUser> userCache = new ConcurrentHashMap<>();

    /** 缓存项：loginUser 为 null 表示用户不存在或已被禁用。 */
    private static final class CachedUser {
        private final LoginUser loginUser;
        private final long expiresAt;

        private CachedUser(LoginUser loginUser, long expiresAt) {
            this.loginUser = loginUser;
            this.expiresAt = expiresAt;
        }

        private boolean isExpired(long now) {
            return now >= expiresAt;
        }
    }

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   TokenBlacklistService tokenBlacklistService,
                                   SysUserMapper sysUserMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HEADER);
        // 未携带 Bearer Token 时不设置认证信息，直接放行：
        // 白名单接口可正常访问，受保护接口由 Spring Security 的 EntryPoint 统一返回 401。
        if (StringUtils.hasText(header) && header.startsWith(PREFIX)) {
            String token = header.substring(PREFIX.length());
            try {
                Claims claims = jwtTokenProvider.parseToken(token);
                // 已注销的 Token 视作未认证；Redis 异常时黑名单服务返回 false，保证请求可继续。
                Long userId = Long.valueOf(claims.getSubject());
                if (tokenBlacklistService.isBlacklisted(token)) {
                    SecurityContextHolder.clearContext();
                } else if (tokenBlacklistService.isIssuedBeforeInvalidation(userId, claims.getIssuedAt())) {
                    // 改密/禁用/删除后，变更前签发的令牌一律失效，避免旧凭证继续使用
                    log.warn("JWT rejected: issued before credential invalidation, user {}", userId);
                    SecurityContextHolder.clearContext();
                } else {
                    authenticate(claims, request);
                }
            } catch (ExpiredJwtException ex) {
                // 过期属于正常现象，用 debug 避免刷日志
                log.debug("JWT expired: {}", ex.getMessage());
                SecurityContextHolder.clearContext();
            } catch (JwtException | IllegalArgumentException ex) {
                // 签名错误、格式非法、subject 非数字：记录 warn 便于发现伪造尝试
                log.warn("JWT rejected: {}", ex.getMessage());
                SecurityContextHolder.clearContext();
            } catch (RuntimeException ex) {
                // 用户状态查询异常时 fail-closed，避免数据库故障期间放行
                log.error("JWT authentication failed, request treated as unauthenticated", ex);
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(Claims claims, HttpServletRequest request) {
        Long userId = Long.valueOf(claims.getSubject());
        LoginUser loginUser = loadUser(userId);
        // 身份与角色以数据库为准：禁用、删除、降级在令牌有效期内同样生效（最迟 5 秒）
        if (loginUser == null) {
            log.warn("JWT rejected: user {} is disabled or removed", userId);
            SecurityContextHolder.clearContext();
            return;
        }
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                loginUser, null,
                // 角色统一加 ROLE_ 前缀，便于后续使用 hasRole('ADMIN') 之类的表达式。
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + loginUser.getRole())));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 回查用户并返回可用的登录主体；用户不存在或被禁用时返回 null。
     * 结果缓存在本地，命中时不再访问数据库。
     */
    private LoginUser loadUser(Long userId) {
        long now = System.currentTimeMillis();
        CachedUser cached = userCache.get(userId);
        if (cached != null && !cached.isExpired(now)) {
            return cached.loginUser;
        }
        SysUser user = sysUserMapper.selectById(userId);
        LoginUser loginUser = null;
        if (user != null && Integer.valueOf(1).equals(user.getStatus())) {
            loginUser = new LoginUser(user.getId(), user.getUsername(), user.getRole());
        }
        if (userCache.size() >= USER_CACHE_MAX_SIZE) {
            userCache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
        }
        // 否定结果同样缓存：伪造 sub 的请求不会每次都穿透到数据库
        userCache.put(userId, new CachedUser(loginUser, now + USER_CACHE_TTL_MS));
        return loginUser;
    }
}
