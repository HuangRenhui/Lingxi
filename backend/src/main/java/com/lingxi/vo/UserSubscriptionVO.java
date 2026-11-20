package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户订阅视图对象。
 */
@Data
@Schema(description = "用户订阅")
public class UserSubscriptionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订阅 ID */
    private Long id;

    /** 套餐标识 */
    private String planKey;

    /** 套餐名称 */
    private String planName;

    /** 计费周期：MONTHLY / YEARLY */
    private String cycle;

    /** 订阅状态 */
    private String status;

    /** 是否自动续费：1 是 / 0 否 */
    private Integer autoRenew;

    /** 本周期开始时间 */
    private LocalDateTime startedAt;

    /** 本周期结束时间 */
    private LocalDateTime expiresAt;

    /** 周期内可用积分 */
    private Integer creditsPerMonth;

    /** 周期内已用积分 */
    private Integer creditsUsed;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
