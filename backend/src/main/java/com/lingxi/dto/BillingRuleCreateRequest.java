package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 新建计费规则请求。
 */
@Data
@Schema(description = "新建计费规则")
public class BillingRuleCreateRequest {

    @NotBlank(message = "计费类型不能为空")
    @Size(max = 16, message = "计费类型不能超过 16 个字符")
    @Schema(description = "计费类型：对话 / 图片 / 视频 / 音乐", example = "对话")
    private String category;

    @NotBlank(message = "应用名称不能为空")
    @Size(max = 64, message = "应用名称不能超过 64 个字符")
    @Schema(description = "应用名称", example = "ChatGPT")
    private String target;

    @NotBlank(message = "模型名称不能为空")
    @Size(max = 64, message = "模型名称不能超过 64 个字符")
    @Schema(description = "模型名称", example = "GPT-6 Astra")
    private String model;

    @NotBlank(message = "计费单位不能为空")
    @Size(max = 32, message = "计费单位不能超过 32 个字符")
    @Schema(description = "计费单位", example = "1K tokens")
    private String unit;

    @NotNull(message = "输入单价不能为空")
    @DecimalMin(value = "0", message = "输入单价不能为负数")
    @Schema(description = "输入单价（积分）", example = "0.02")
    private BigDecimal priceIn;

    @NotNull(message = "输出单价不能为空")
    @DecimalMin(value = "0", message = "输出单价不能为负数")
    @Schema(description = "输出单价（积分）", example = "0.06")
    private BigDecimal priceOut;

    @NotNull(message = "缓存输入单价不能为空")
    @DecimalMin(value = "0", message = "缓存输入单价不能为负数")
    @Schema(description = "缓存输入单价（积分）", example = "0.01")
    private BigDecimal priceCachedIn;

    @NotNull(message = "输入成本不能为空")
    @DecimalMin(value = "0", message = "输入成本不能为负数")
    @Schema(description = "输入成本（积分）", example = "0.01")
    private BigDecimal costIn;

    @NotNull(message = "输出成本不能为空")
    @DecimalMin(value = "0", message = "输出成本不能为负数")
    @Schema(description = "输出成本（积分）", example = "0.03")
    private BigDecimal costOut;

    @NotNull(message = "倍率不能为空")
    @DecimalMin(value = "0.01", message = "倍率必须大于 0")
    @Schema(description = "计价倍率", example = "1.5")
    private BigDecimal multiplier;

    @Min(value = 0, message = "启用状态仅支持 0 或 1")
    @Max(value = 1, message = "启用状态仅支持 0 或 1")
    @Schema(description = "启用：1 是 / 0 否", example = "1")
    private Integer enabled;
}
