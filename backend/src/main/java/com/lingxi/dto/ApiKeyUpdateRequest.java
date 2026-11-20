package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * 更新 API Key 请求，字段均可选。
 */
@Data
@Schema(description = "更新 API Key")
public class ApiKeyUpdateRequest {

    /** 密钥名称 */
    @Size(max = 64, message = "密钥名称不能超过 64 个字符")
    @Schema(description = "密钥名称")
    private String name;

    /** 状态：1 启用，0 禁用 */
    @Min(value = 0, message = "状态仅支持 0 或 1")
    @Max(value = 1, message = "状态仅支持 0 或 1")
    @Schema(description = "状态：1 启用，0 禁用")
    private Integer status;
}
