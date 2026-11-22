package com.lingxi.service.impl;

import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.LoginRequest;
import com.lingxi.entity.SysUser;
import com.lingxi.mapper.SysUserMapper;
import com.lingxi.security.JwtTokenProvider;
import com.lingxi.security.LoginAttemptService;
import com.lingxi.security.LoginUser;
import com.lingxi.security.SecurityUtils;
import com.lingxi.security.TokenBlacklistService;
import com.lingxi.service.AuthService;
import com.lingxi.vo.LoginVO;
import com.lingxi.vo.TokenRefreshVO;
import com.lingxi.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * 认证服务实现：账号密码校验 + JWT 签发。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthServiceImpl(SysUserMapper sysUserMapper,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           LoginAttemptService loginAttemptService,
                           TokenBlacklistService tokenBlacklistService) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.loginAttemptService = loginAttemptService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public LoginVO login(LoginRequest request) {
        // 用户名归一化后再计数：数据库排序规则大小写不敏感，
        // 若用原始输入做 key，Admin/admin 会命中同一账号却绕开失败次数限制。
        String username = request.getUsername().trim();
        String attemptKey = username.toLowerCase(Locale.ROOT);
        // 连续失败达到阈值后直接拒绝，避免在线暴力破解；Redis 故障时使用本地计数降级。
        loginAttemptService.checkLocked(attemptKey);
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        // 用户不存在与密码错误统一返回 INVALID_CREDENTIALS，避免通过错误信息枚举有效账号。
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginAttemptService.onFailure(attemptKey);
            throw new BusinessException(ResultCode.INVALID_CREDENTIALS);
        }
        // 先校验凭证再校验状态，避免向未通过身份验证的请求泄露账号是否被禁用。
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }
        // 登录成功清除失败计数，避免影响后续正常登录。
        loginAttemptService.onSuccess(attemptKey);

        LoginVO vo = new LoginVO();
        // Token 中只携带 id/username/role，敏感字段（密码等）不会进入 JWT。
        vo.setToken(jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole()));
        vo.setTokenType("Bearer");
        vo.setExpiresIn(jwtTokenProvider.getExpireMinutes());
        vo.setUser(toUserVO(user));
        return vo;
    }

    @Override
    public UserVO currentUser() {
        // 只信任 SecurityContext 中的用户 ID，不接收前端传入的用户标识，防止越权查询他人资料。
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = sysUserMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return toUserVO(user);
    }

    @Override
    public TokenRefreshVO refresh(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        Claims claims;
        try {
            // parseToken 会校验签名与过期时间，过期或非法令牌统一按未登录处理。
            claims = jwtTokenProvider.parseToken(token);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        // 已注销的令牌不允许再次换取新令牌。
        if (tokenBlacklistService.isBlacklisted(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        // 凭据变更（改密/禁用/删除）后签发的失效时间点必须生效，
        // 否则旧令牌可以绕过过滤器通过刷新接口换取新令牌。
        if (tokenBlacklistService.isIssuedBeforeInvalidation(
                Long.valueOf(claims.getSubject()), claims.getIssuedAt())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        // 绝对会话上限：持续刷新不能让令牌永久有效，超时必须重新登录。
        if (jwtTokenProvider.isSessionExpired(claims)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        // 刷新也要回查账号状态：用户被禁用/删除后，旧令牌不应继续换取新令牌。
        SysUser current = sysUserMapper.selectById(Long.valueOf(claims.getSubject()));
        if (current == null || current.getStatus() == null || current.getStatus() != 1) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        // 旧令牌按剩余有效期加入黑名单，签发新令牌后立即失效。
        tokenBlacklistService.blacklist(token, remainingSeconds(claims));
        String newToken = jwtTokenProvider.generateToken(
                current.getId(),
                // 角色/用户名以数据库为准，避免使用旧令牌里的历史声明
                current.getUsername(),
                current.getRole(),
                // 透传会话起始时间，保证绝对上限跨刷新依然生效
                jwtTokenProvider.sessionStartSeconds(claims));

        TokenRefreshVO vo = new TokenRefreshVO();
        vo.setToken(newToken);
        // 与登录接口保持一致，expiresIn 单位为分钟。
        vo.setExpiresIn(jwtTokenProvider.getExpireMinutes());
        return vo;
    }

    @Override
    public void logout(String token) {
        // 未携带令牌时视为已退出；已过期的令牌也无需再加入黑名单。
        if (!StringUtils.hasText(token)) {
            return;
        }
        try {
            Claims claims = jwtTokenProvider.parseToken(token);
            tokenBlacklistService.blacklist(token, remainingSeconds(claims));
        } catch (JwtException | IllegalArgumentException ex) {
            // 令牌非法或已过期：注销幂等成功，不再额外报错。
        }
    }

    private long remainingSeconds(Claims claims) {
        if (claims.getExpiration() == null) {
            return 0L;
        }
        return (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000L;
    }

    private UserVO toUserVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        return vo;
    }
}
