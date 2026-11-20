package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 订单状态变更请求。
 *
 * <p>用户侧目前仅支持取消待支付订单；支付与退款由后续支付回调接口驱动。</p>
 */
@Data
@Schema(description = "订单状态变更")
public class OrderStatusRequest {

    /** 目标状态 */
    @NotBlank(message = "目标状态不能为空")
    @Pattern(regexp = "CANCELLED", message = "用户侧仅支持取消订单")
    @Schema(description = "目标状态", example = "CANCELLED")
    private String status;
}
