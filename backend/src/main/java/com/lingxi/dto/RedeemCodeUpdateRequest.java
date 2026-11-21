package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新兑换码请求，仅更新传入的字段。
 */
@Data
@Schema(description = "更新兑换码")
public class RedeemCodeUpdateRequest {

    @DecimalMin(value = "0.01", message = "面额必须大于 0")
    @Schema(description = "面额（积分）")
    private BigDecimal amount;

    @Schema(description = "有效期至")
    private LocalDate expiresAt;

    @Pattern(regexp = "UNUSED|USED|VOID", message = "状态仅支持 UNUSED/USED/VOID")
    @Schema(description = "状态：UNUSED / USED / VOID")
    private String status;
}
