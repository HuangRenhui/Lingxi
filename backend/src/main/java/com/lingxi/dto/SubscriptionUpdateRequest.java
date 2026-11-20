package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 更新当前订阅请求。
 */
@Data
@Schema(description = "更新当前订阅")
public class SubscriptionUpdateRequest {

    /** 是否自动续费：1 是 / 0 否 */
    // 必须 @NotNull：否则空 body {} 也能通过校验，接口返回 200 却什么也没改
    @NotNull(message = "自动续费开关不能为空")
    @Min(value = 0, message = "自动续费仅支持 0 或 1")
    @Max(value = 1, message = "自动续费仅支持 0 或 1")
    @Schema(description = "自动续费：1 是 / 0 否", example = "0")
    private Integer autoRenew;
}
