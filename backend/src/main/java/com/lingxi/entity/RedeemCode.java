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
 * 积分兑换码实体，对应表 redeem_code。
 */
@Data
@TableName("redeem_code")
public class RedeemCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 兑换码 */
    private String code;

    /** 面额（积分） */
    private BigDecimal amount;

    /** 状态：UNUSED / USED / VOID */
    private String status;

    /** 有效期至 */
    private LocalDate expiresAt;

    /** 使用人 */
    private Long usedBy;

    /** 使用时间 */
    private LocalDateTime usedAt;

    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
