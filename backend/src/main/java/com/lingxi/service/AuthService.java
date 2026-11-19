package com.lingxi.service;

import com.lingxi.dto.LoginRequest;
import com.lingxi.vo.LoginVO;
import com.lingxi.vo.TokenRefreshVO;
import com.lingxi.vo.UserVO;

/**
 * 认证服务（B1）。
 */
public interface AuthService {

    LoginVO login(LoginRequest request);

    UserVO currentUser();

    /**
     * 刷新令牌：旧令牌立即失效，返回新的令牌与有效期。
     */
    TokenRefreshVO refresh(String token);

    /**
     * 注销：将当前令牌加入黑名单；令牌缺失或已过期时直接忽略。
     */
    void logout(String token);
}
