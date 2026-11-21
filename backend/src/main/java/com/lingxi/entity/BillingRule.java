package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 计费规则实体，对应表 billing_rule。
 *
 * <p>本轮仅提供配置的增删改查，实际扣费口径由调用链后续接入。</p>
 */
@Data
@TableName("billing_rule")
public class BillingRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 计费类型：对话 / 图片 / 视频 / 音乐 */
    private String category;

    /** 应用名称 */
    private String target;

    /** 模型名称 */
    private String model;

    /** 计费单位 */
    private String unit;

    /** 输入单价（积分） */
    private BigDecimal priceIn;

    /** 输出单价（积分） */
    private BigDecimal priceOut;

    /** 缓存输入单价（积分） */
    private BigDecimal priceCachedIn;

    /** 输入成本（积分） */
    private BigDecimal costIn;

    /** 输出成本（积分） */
    private BigDecimal costOut;

    /** 计价倍率 */
    private BigDecimal multiplier;

    /** 启用：1 是 / 0 否 */
    private Integer enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
