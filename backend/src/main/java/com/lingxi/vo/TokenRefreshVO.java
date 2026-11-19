package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 令牌刷新响应。
 */
@Data
@Schema(description = "令牌刷新响应")
public class TokenRefreshVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "新的 JWT 令牌")
    private String token;

    @Schema(description = "有效期（分钟）")
    private Long expiresIn;
}
