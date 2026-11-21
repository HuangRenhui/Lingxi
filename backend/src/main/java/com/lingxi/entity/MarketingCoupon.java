package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 营销优惠券实体，对应表 marketing_coupon。
 *
 * <p>本轮仅提供运营配置的增删改查，领券与核销由后续营销流程接入。</p>
 */
@Data
@TableName("marketing_coupon")
public class MarketingCoupon {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 优惠券名称 */
    private String name;

    /** 类型：FULL_REDUCTION 满减 / DISCOUNT 折扣 */
    private String type;

    /** 面额：满减为金额，折扣为 0-1 折扣率 */
    private BigDecimal value;

    /** 使用门槛，0 为无门槛 */
    private BigDecimal threshold;

    /** 发放总量 */
    private Integer total;

    /** 已领取数量 */
    private Integer claimed;

    /** 有效期开始 */
    private LocalDate validFrom;

    /** 有效期结束 */
    private LocalDate validTo;

    /** 启用：1 是 / 0 否 */
    private Integer enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
