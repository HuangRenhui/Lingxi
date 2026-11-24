package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 批量生成兑换码请求。
 */
@Data
@Schema(description = "批量生成兑换码")
public class RedeemCodeBatchRequest {

    @NotNull(message = "面额不能为空")
    @DecimalMin(value = "0.01", message = "面额必须大于 0")
    @Schema(description = "单个面额（积分）", example = "100")
    private BigDecimal amount;

    @NotNull(message = "生成数量不能为空")
    @Min(value = 1, message = "生成数量至少为 1")
    @Max(value = 200, message = "单次最多生成 200 个")
    @Schema(description = "生成数量", example = "10")
    private Integer count;

    @Schema(description = "有效期至，留空表示永久有效", example = "2026-12-31")
    private LocalDate expiresAt;

    @Size(max = 12, message = "前缀不能超过 12 个字符")
    @Schema(description = "兑换码前缀", example = "LX2026")
    private String prefix;
}
