package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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

    /**
     * 是否为续费：true 表示同套餐仍在有效期内时按当前周期叠加时长并扣费；
     * 不传/false 时同套餐重复请求按幂等处理，直接返回现有订阅。
     */
    @Schema(description = "是否续费", example = "false")
    private Boolean renew;
}
