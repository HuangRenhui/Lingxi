package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 更新订单请求。
 *
 * <p>仅待支付订单允许修改，且只开放描述与支付方式；金额与状态不可直接修改。</p>
 */
@Data
@Schema(description = "更新订单")
public class OrderUpdateRequest {

    /** 订单描述，传入时不能为空白 */
    @Size(max = 128, message = "订单描述不能超过 128 个字符")
    @Schema(description = "订单描述", example = "积分充值")
    private String description;

    /** 支付方式，传入空串表示清空 */
    @Pattern(regexp = "^(?:ALIPAY|WECHAT|REDEEM|CREDITS)?$",
            message = "支付方式仅支持 ALIPAY/WECHAT/REDEEM/CREDITS")
    @Schema(description = "支付方式", example = "WECHAT")
    private String payMethod;
}
