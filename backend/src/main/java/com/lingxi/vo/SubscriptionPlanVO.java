package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订阅套餐视图对象。
 */
@Data
@Schema(description = "订阅套餐")
public class SubscriptionPlanVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 套餐标识 */
    private String planKey;

    /** 套餐名称 */
    private String name;

    /** 月付价格 */
    private BigDecimal monthlyPrice;

    /** 年付价格 */
    private BigDecimal yearlyPrice;

    /** 每周期赠送积分 */
    private Integer creditsPerMonth;

    /** 角标文案 */
    private String tag;

    /** 权益说明 */
    private List<String> features;
}
