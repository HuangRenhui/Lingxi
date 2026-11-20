package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 钱包账户视图对象。
 */
@Data
@Schema(description = "钱包账户")
public class WalletAccountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 可用余额（积分） */
    private BigDecimal balance;

    /** 冻结金额（积分） */
    private BigDecimal frozen;

    /** 累计充值（积分） */
    private BigDecimal totalRecharge;

    /** 累计消费（积分） */
    private BigDecimal totalSpent;
}
