package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新优惠券请求，仅更新传入的字段。
 */
@Data
@Schema(description = "更新优惠券")
public class CouponUpdateRequest {

    @Size(max = 64, message = "优惠券名称不能超过 64 个字符")
    @Schema(description = "优惠券名称")
    private String name;

    @Pattern(regexp = "FULL_REDUCTION|DISCOUNT", message = "类型仅支持 FULL_REDUCTION/DISCOUNT")
    @Schema(description = "类型：FULL_REDUCTION 满减 / DISCOUNT 折扣")
    private String type;

    @DecimalMin(value = "0", message = "面额不能为负数")
    @Schema(description = "面额：满减为金额，折扣为 0-1 折扣率")
    private BigDecimal value;

    @DecimalMin(value = "0", message = "使用门槛不能为负数")
    @Schema(description = "使用门槛，0 为无门槛")
    private BigDecimal threshold;

    @Min(value = 1, message = "发放总量至少为 1")
    @Schema(description = "发放总量")
    private Integer total;

    @Schema(description = "有效期开始")
    private LocalDate validFrom;

    @Schema(description = "有效期结束")
    private LocalDate validTo;

    @Min(value = 0, message = "启用状态仅支持 0 或 1")
    @Max(value = 1, message = "启用状态仅支持 0 或 1")
    @Schema(description = "启用：1 是 / 0 否")
    private Integer enabled;
}
