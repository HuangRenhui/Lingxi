package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求。
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 64, message = "用户名长度为 4-64 个字符")
    @Schema(description = "用户名", example = "admin")
    private String username;

    @NotBlank(message = "密码不能为空")
    // 上限防止超大请求体触发 BCrypt 的 CPU 消耗
    @Size(max = 64, message = "密码长度不能超过 64 个字符")
    @Schema(description = "密码", example = "Admin@123")
    private String password;
}
