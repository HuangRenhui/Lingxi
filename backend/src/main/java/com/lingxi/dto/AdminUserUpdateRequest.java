package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 管理端更新用户请求，字段均可选。
 */
@Data
@Schema(description = "管理端更新用户")
public class AdminUserUpdateRequest {

    /** 传入时表示重置密码 */
    @Size(min = 8, max = 64, message = "密码长度需为 8-64 位")
    @Schema(description = "新密码，留空表示不修改")
    private String password;

    @Size(max = 64, message = "昵称不能超过 64 个字符")
    @Schema(description = "昵称")
    private String nickname;

    @Pattern(regexp = "ADMIN|USER", message = "角色仅支持 ADMIN / USER")
    @Schema(description = "角色")
    private String role;

    @Min(value = 0, message = "状态仅支持 0 或 1")
    @Max(value = 1, message = "状态仅支持 0 或 1")
    @Schema(description = "状态：1 启用，0 禁用")
    private Integer status;
}
