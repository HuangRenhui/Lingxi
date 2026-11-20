package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 开通或续费订阅请求。
 */
@Data
@Schema(description = "开通订阅")
public class SubscriptionCreateRequest {

    /** 套餐标识 */
    @NotBlank(message = "套餐标识不能为空")
    @Size(max = 32, message = "套餐标识不能超过 32 个字符")
    @Schema(description = "套餐标识", example = "pro")
    private String planKey;

    /** 计费周期 */
    @NotBlank(message = "计费周期不能为空")
    @Pattern(regexp = "MONTHLY|YEARLY", message = "计费周期仅支持 MONTHLY/YEARLY")
    @Schema(description = "计费周期", example = "MONTHLY")
    private String cycle;
}
