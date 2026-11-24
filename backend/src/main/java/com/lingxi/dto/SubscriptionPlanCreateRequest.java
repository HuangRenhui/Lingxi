package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端新建订阅套餐请求。
 */
@Data
@Schema(description = "新建订阅套餐")
public class SubscriptionPlanCreateRequest {

    @NotBlank(message = "套餐标识不能为空")
    @Size(max = 32, message = "套餐标识不能超过 32 个字符")
    @Pattern(regexp = "[A-Za-z0-9_-]+", message = "套餐标识仅支持字母、数字、下划线和中划线")
    @Schema(description = "套餐标识", example = "pro")
    private String planKey;

    @NotBlank(message = "套餐名称不能为空")
    @Size(max = 64, message = "套餐名称不能超过 64 个字符")
    @Schema(description = "套餐名称", example = "专业版")
    private String name;

    @NotNull(message = "月付价格不能为空")
    @DecimalMin(value = "0", message = "月付价格不能为负数")
    @Schema(description = "月付价格（积分）", example = "68")
    private BigDecimal monthlyPrice;

    @NotNull(message = "年付价格不能为空")
    @DecimalMin(value = "0", message = "年付价格不能为负数")
    @Schema(description = "年付价格（积分）", example = "680")
    private BigDecimal yearlyPrice;

    @NotNull(message = "每周期积分不能为空")
    @Min(value = 0, message = "每周期积分不能为负数")
    @Schema(description = "每周期赠送积分", example = "2000")
    private Integer creditsPerMonth;

    @Size(max = 32, message = "角标文案不能超过 32 个字符")
    @Schema(description = "角标文案")
    private String tag;

    @Schema(description = "权益说明，每项一条")
    private List<String> features;

    @Min(value = 0, message = "上架状态仅支持 0 或 1")
    @Max(value = 1, message = "上架状态仅支持 0 或 1")
    @Schema(description = "上架状态：1 上架 / 0 下架", example = "1")
    private Integer enabled;

    @Min(value = 0, message = "排序值不能为负数")
    @Schema(description = "排序值，越小越靠前", example = "10")
    private Integer sortOrder;
}
