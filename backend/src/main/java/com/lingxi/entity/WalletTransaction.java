package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包流水实体，对应表 wallet_transaction。
 *
 * <p>流水只追加不修改、不删除：每笔记录变动值与变动后余额，
 * 保证任何时点的余额都能被逐笔复算。</p>
 */
@Data
@TableName("wallet_transaction")
public class WalletTransaction {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 属主用户 ID */
    private Long userId;

    /** 流水类型：RECHARGE / CONSUME / REWARD / REFUND / FREEZE / UNFREEZE */
    private String type;

    /** 变动值，正数为增加、负数为扣减 */
    private BigDecimal amount;

    /** 变动后余额 */
    private BigDecimal balance;

    /** 备注，说明来源业务 */
    private String note;

    /**
     * 业务幂等键（如 ORDER-&lt;订单号&gt;）。
     * 唯一索引保证同一笔业务只会写一条流水，支付回调或客户端重试不会重复入账。
     */
    private String bizNo;

    /** 创建时间，由数据库默认值填充 */
    private LocalDateTime createdAt;
}
