package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 业务订单实体，对应表 biz_order。
 *
 * <p>订单是充值、订阅、消费与兑换码的统一凭证，金额单位为平台积分；
 * 状态只能按状态机流转，用户侧仅可取消待支付订单。</p>
 */
@Data
@TableName("biz_order")
public class BizOrder {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号，对外展示且唯一 */
    private String orderNo;

    /** 下单用户 ID，用于数据隔离 */
    private Long userId;

    /** 订单描述 */
    private String description;

    /** 订单类型：RECHARGE / SUBSCRIPTION / CONSUME / REDEEM */
    private String type;

    /** 订单金额（积分），禁止浮点 */
    private BigDecimal amount;

    /** 支付方式：ALIPAY / WECHAT / REDEEM / CREDITS */
    private String payMethod;

    /** 订单状态：PENDING / PAID / CANCELLED / REFUNDING / REFUNDED */
    private String status;

    /** 支付完成时间 */
    private LocalDateTime paidAt;

    /** 创建时间，由数据库默认值填充 */
    private LocalDateTime createdAt;

    /** 更新时间，由数据库自动维护 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0 正常，1 已删除 */
    @TableLogic
    private Integer deleted;
}
