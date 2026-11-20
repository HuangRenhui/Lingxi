package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 管理端新建用户请求。
 */
@Data
@Schema(description = "管理端新建用户")
public class AdminUserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[A-Za-z0-9_.-]{4,64}$", message = "用户名需为 4-64 位字母、数字或 _ . -")
    @Schema(description = "用户名", example = "operator")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 64, message = "密码长度需为 8-64 位")
    @Schema(description = "初始密码", example = "Operator@123")
    private String password;

    @Size(max = 64, message = "昵称不能超过 64 个字符")
    @Schema(description = "昵称", example = "运营同学")
    private String nickname;

    @Pattern(regexp = "ADMIN|USER", message = "角色仅支持 ADMIN / USER")
    @Schema(description = "角色", example = "USER", defaultValue = "USER")
    private String role;
}
