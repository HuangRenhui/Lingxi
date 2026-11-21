package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 兑换码视图对象。
 */
@Data
@Schema(description = "兑换码")
public class RedeemCodeVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String code;

    private BigDecimal amount;

    /** UNUSED / USED / VOID */
    private String status;

    private LocalDate expiresAt;

    private Long usedBy;

    private LocalDateTime usedAt;

    private LocalDateTime createdAt;
}
