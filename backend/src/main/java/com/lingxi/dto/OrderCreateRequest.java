package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 新建订单请求。
 */
@Data
@Schema(description = "新建订单")
public class OrderCreateRequest {

    /** 订单描述，不能为空 */
    @NotBlank(message = "订单描述不能为空")
    @Size(max = 128, message = "订单描述不能超过 128 个字符")
    @Schema(description = "订单描述", example = "积分充值")
    private String description;

    /** 订单类型，缺省为 CONSUME */
    @Pattern(regexp = "RECHARGE|SUBSCRIPTION|CONSUME|REDEEM",
            message = "订单类型仅支持 RECHARGE/SUBSCRIPTION/CONSUME/REDEEM")
    @Schema(description = "订单类型", example = "RECHARGE")
    private String type;

    /** 订单金额（积分） */
    @NotNull(message = "订单金额不能为空")
    // 0 元订单会绕过付费流程，必须大于 0；整数位受 DECIMAL(18,6) 限制为 12 位
    @DecimalMin(value = "0.000001", message = "订单金额必须大于 0")
    @Digits(integer = 12, fraction = 6, message = "订单金额最多 12 位整数与 6 位小数")
    @Schema(description = "订单金额（积分）", example = "200")
    private BigDecimal amount;

    /** 支付方式，可为空；空串表示未选择 */
    @Pattern(regexp = "^(?:ALIPAY|WECHAT|REDEEM|CREDITS)?$",
            message = "支付方式仅支持 ALIPAY/WECHAT/REDEEM/CREDITS")
    @Schema(description = "支付方式", example = "ALIPAY")
    private String payMethod;
}
