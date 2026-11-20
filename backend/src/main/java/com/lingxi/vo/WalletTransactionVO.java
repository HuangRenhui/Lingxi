package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包流水视图对象。
 */
@Data
@Schema(description = "钱包流水")
public class WalletTransactionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 流水 ID */
    private Long id;

    /** 流水类型 */
    private String type;

    /** 变动值 */
    private BigDecimal amount;

    /** 变动后余额 */
    private BigDecimal balance;

    /** 备注 */
    private String note;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
