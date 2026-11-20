package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单视图对象。
 */
@Data
@Schema(description = "订单")
public class OrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单 ID */
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 订单描述 */
    private String description;

    /** 订单类型 */
    private String type;

    /** 订单金额（积分） */
    private BigDecimal amount;

    /** 支付方式 */
    private String payMethod;

    /** 订单状态 */
    private String status;

    /** 支付完成时间 */
    private LocalDateTime paidAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
