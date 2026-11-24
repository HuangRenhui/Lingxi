package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建 API Key 请求。
 */
@Data
@Schema(description = "创建 API Key")
public class ApiKeyCreateRequest {

    /** 密钥名称 */
    @NotBlank(message = "密钥名称不能为空")
    @Size(max = 64, message = "密钥名称不能超过 64 个字符")
    @Schema(description = "密钥名称", example = "本地调试")
    private String name;
}
