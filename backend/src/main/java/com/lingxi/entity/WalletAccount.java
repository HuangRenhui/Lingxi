package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包账户实体，对应表 wallet_account。
 *
 * <p>每个用户一个账户，记录可用余额、冻结金额与累计充值/消费；
 * 余额变动必须与流水一并写入，禁止单独改余额。</p>
 */
@Data
@TableName("wallet_account")
public class WalletAccount {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 属主用户 ID，一人一账户 */
    private Long userId;

    /** 可用余额（积分） */
    private BigDecimal balance;

    /** 冻结金额（积分） */
    private BigDecimal frozen;

    /** 累计充值（积分） */
    private BigDecimal totalRecharge;

    /** 累计消费（积分） */
    private BigDecimal totalSpent;

    /** 创建时间，由数据库默认值填充 */
    private LocalDateTime createdAt;

    /** 更新时间，由数据库自动维护 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0 正常，1 已删除 */
    @TableLogic
    private Integer deleted;
}
