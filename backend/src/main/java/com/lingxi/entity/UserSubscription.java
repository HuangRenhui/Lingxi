package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户订阅实体，对应表 user_subscription。
 *
 * <p>每次开通、续费或变更套餐都会新增一条记录，旧记录置为失效，
 * 因此该表同时承担订阅历史，套餐名称与额度均为下单时快照。</p>
 */
@Data
@TableName("user_subscription")
public class UserSubscription {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订阅用户 ID */
    private Long userId;

    /** 套餐标识 */
    private String planKey;

    /** 套餐名称快照 */
    private String planName;

    /** 计费周期：MONTHLY / YEARLY */
    private String cycle;

    /** 状态：ACTIVE / EXPIRED / CANCELLED */
    private String status;

    /**
     * 生效标记：ACTIVE 时为 1，失效置 NULL。
     * 与 (user_id, active_flag) 唯一索引配合，从数据库层保证「每用户最多一条生效订阅」，
     * 而 MySQL 唯一索引允许多个 NULL，历史记录不受影响。
     */
    private Integer activeFlag;

    /** 是否自动续费：1 是 / 0 否 */
    private Integer autoRenew;

    /** 本周期开始时间 */
    private LocalDateTime startedAt;

    /** 本周期结束时间 */
    private LocalDateTime expiresAt;

    /** 周期内可用积分快照 */
    private Integer creditsPerMonth;

    /** 周期内已用积分 */
    private Integer creditsUsed;

    /** 创建时间，由数据库默认值填充 */
    private LocalDateTime createdAt;

    /** 更新时间，由数据库自动维护 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0 正常，1 已删除 */
    @TableLogic
    private Integer deleted;
}
