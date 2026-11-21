package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 新建优惠券请求。
 */
@Data
@Schema(description = "新建优惠券")
public class CouponCreateRequest {

    @NotBlank(message = "优惠券名称不能为空")
    @Size(max = 64, message = "优惠券名称不能超过 64 个字符")
    @Schema(description = "优惠券名称", example = "双十一充值满减")
    private String name;

    @NotBlank(message = "优惠券类型不能为空")
    @Pattern(regexp = "FULL_REDUCTION|DISCOUNT", message = "类型仅支持 FULL_REDUCTION/DISCOUNT")
    @Schema(description = "类型：FULL_REDUCTION 满减 / DISCOUNT 折扣", example = "FULL_REDUCTION")
    private String type;

    @NotNull(message = "面额不能为空")
    @DecimalMin(value = "0", message = "面额不能为负数")
    @Schema(description = "面额：满减为金额，折扣为 0-1 折扣率", example = "10")
    private BigDecimal value;

    @NotNull(message = "使用门槛不能为空")
    @DecimalMin(value = "0", message = "使用门槛不能为负数")
    @Schema(description = "使用门槛，0 为无门槛", example = "50")
    private BigDecimal threshold;

    @NotNull(message = "发放总量不能为空")
    @Min(value = 1, message = "发放总量至少为 1")
    @Schema(description = "发放总量", example = "200")
    private Integer total;

    @Schema(description = "有效期开始", example = "2026-11-01")
    private LocalDate validFrom;

    @Schema(description = "有效期结束", example = "2026-11-30")
    private LocalDate validTo;

    @Min(value = 0, message = "启用状态仅支持 0 或 1")
    @Max(value = 1, message = "启用状态仅支持 0 或 1")
    @Schema(description = "启用：1 是 / 0 否", example = "1")
    private Integer enabled;
}
