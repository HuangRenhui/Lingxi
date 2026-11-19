package com.lingxi.security;

import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户工具。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 认证信息可能为匿名认证或已被过滤器清空，只有 principal 是 LoginUser 才代表已登录；
        // 统一抛业务异常，交由全局异常处理器转换为 401 响应。
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return (LoginUser) authentication.getPrincipal();
    }

    public static Long getUserId() {
        return getLoginUser().getId();
    }
}
