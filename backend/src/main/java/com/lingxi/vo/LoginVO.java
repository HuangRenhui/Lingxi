package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应。
 */
@Data
@Schema(description = "登录响应")
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "JWT 令牌")
    private String token;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "有效期（分钟）")
    private Long expiresIn;

    @Schema(description = "当前用户")
    private UserVO user;
}
