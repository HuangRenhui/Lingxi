package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 新建兑换码请求。
 */
@Data
@Schema(description = "新建兑换码")
public class RedeemCodeCreateRequest {

    @Size(max = 32, message = "兑换码不能超过 32 个字符")
    @Schema(description = "兑换码，留空时由服务端生成", example = "LX2026-ABCDEF")
    private String code;

    @NotNull(message = "面额不能为空")
    @DecimalMin(value = "0.01", message = "面额必须大于 0")
    @Schema(description = "面额（积分）", example = "100")
    private BigDecimal amount;

    @Schema(description = "有效期至，留空表示永久有效", example = "2026-12-31")
    private LocalDate expiresAt;
}
