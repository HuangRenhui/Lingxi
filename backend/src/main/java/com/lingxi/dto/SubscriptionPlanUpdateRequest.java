package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端更新订阅套餐请求，仅更新传入的字段。
 */
@Data
@Schema(description = "更新订阅套餐")
public class SubscriptionPlanUpdateRequest {

    @Size(max = 32, message = "套餐标识不能超过 32 个字符")
    @Pattern(regexp = "[A-Za-z0-9_-]+", message = "套餐标识仅支持字母、数字、下划线和中划线")
    @Schema(description = "套餐标识")
    private String planKey;

    @Size(max = 64, message = "套餐名称不能超过 64 个字符")
    @Schema(description = "套餐名称")
    private String name;

    @DecimalMin(value = "0", message = "月付价格不能为负数")
    @Schema(description = "月付价格（积分）")
    private BigDecimal monthlyPrice;

    @DecimalMin(value = "0", message = "年付价格不能为负数")
    @Schema(description = "年付价格（积分）")
    private BigDecimal yearlyPrice;

    @Min(value = 0, message = "每周期积分不能为负数")
    @Schema(description = "每周期赠送积分")
    private Integer creditsPerMonth;

    @Size(max = 32, message = "角标文案不能超过 32 个字符")
    @Schema(description = "角标文案")
    private String tag;

    @Schema(description = "权益说明，每项一条")
    private List<String> features;

    @Min(value = 0, message = "上架状态仅支持 0 或 1")
    @Max(value = 1, message = "上架状态仅支持 0 或 1")
    @Schema(description = "上架状态：1 上架 / 0 下架")
    private Integer enabled;

    @Min(value = 0, message = "排序值不能为负数")
    @Schema(description = "排序值，越小越靠前")
    private Integer sortOrder;
}
