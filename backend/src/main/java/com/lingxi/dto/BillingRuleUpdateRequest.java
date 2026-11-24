package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 更新计费规则请求，仅更新传入的字段。
 */
@Data
@Schema(description = "更新计费规则")
public class BillingRuleUpdateRequest {

    @Size(max = 16, message = "计费类型不能超过 16 个字符")
    @Schema(description = "计费类型")
    private String category;

    @Size(max = 64, message = "应用名称不能超过 64 个字符")
    @Schema(description = "应用名称")
    private String target;

    @Size(max = 64, message = "模型名称不能超过 64 个字符")
    @Schema(description = "模型名称")
    private String model;

    @Size(max = 32, message = "计费单位不能超过 32 个字符")
    @Schema(description = "计费单位")
    private String unit;

    @DecimalMin(value = "0", message = "输入单价不能为负数")
    @Schema(description = "输入单价（积分）")
    private BigDecimal priceIn;

    @DecimalMin(value = "0", message = "输出单价不能为负数")
    @Schema(description = "输出单价（积分）")
    private BigDecimal priceOut;

    @DecimalMin(value = "0", message = "缓存输入单价不能为负数")
    @Schema(description = "缓存输入单价（积分）")
    private BigDecimal priceCachedIn;

    @DecimalMin(value = "0", message = "输入成本不能为负数")
    @Schema(description = "输入成本（积分）")
    private BigDecimal costIn;

    @DecimalMin(value = "0", message = "输出成本不能为负数")
    @Schema(description = "输出成本（积分）")
    private BigDecimal costOut;

    @DecimalMin(value = "0.01", message = "倍率必须大于 0")
    @Schema(description = "计价倍率")
    private BigDecimal multiplier;

    @Min(value = 0, message = "启用状态仅支持 0 或 1")
    @Max(value = 1, message = "启用状态仅支持 0 或 1")
    @Schema(description = "启用：1 是 / 0 否")
    private Integer enabled;
}
