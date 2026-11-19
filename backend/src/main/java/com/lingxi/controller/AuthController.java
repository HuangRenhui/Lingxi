package com.lingxi.controller;

import com.lingxi.common.BusinessException;
import com.lingxi.common.Result;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.LoginRequest;
import com.lingxi.service.AuthService;
import com.lingxi.vo.LoginVO;
import com.lingxi.vo.TokenRefreshVO;
import com.lingxi.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 认证与个人中心（B1）。
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "02. 认证")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "账号密码登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @GetMapping("/profile")
    @Operation(summary = "获取当前登录用户")
    public Result<UserVO> profile() {
        return Result.success(authService.currentUser());
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        // 注销保持幂等：未携带令牌时直接返回成功。
        authService.logout(extractToken(authorization));
        return Result.success();
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌")
    public Result<TokenRefreshVO> refresh(@RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = extractToken(authorization);
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        return Result.success(authService.refresh(token));
    }

    private String extractToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring("Bearer ".length());
    }
}
