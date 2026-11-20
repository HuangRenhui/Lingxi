package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订阅套餐实体，对应表 subscription_plan。
 *
 * <p>套餐是全局目录数据，由运营配置；用户订阅时以套餐快照写入
 * user_subscription，避免后续调价影响历史记录。</p>
 */
@Data
@TableName("subscription_plan")
public class SubscriptionPlan {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 套餐标识，如 free / pro / ultra，全局唯一 */
    private String planKey;

    /** 套餐名称 */
    private String name;

    /** 月付价格（积分） */
    private BigDecimal monthlyPrice;

    /** 年付价格（积分） */
    private BigDecimal yearlyPrice;

    /** 每周期赠送积分 */
    private Integer creditsPerMonth;

    /** 角标文案，可为空 */
    private String tag;

    /** 权益说明，按换行分隔 */
    private String features;

    /** 是否上架：1 上架 / 0 下架 */
    private Integer enabled;

    /** 排序值，越小越靠前 */
    private Integer sortOrder;

    /** 创建时间，由数据库默认值填充 */
    private LocalDateTime createdAt;

    /** 更新时间，由数据库自动维护 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0 正常，1 已删除 */
    @TableLogic
    private Integer deleted;
}
