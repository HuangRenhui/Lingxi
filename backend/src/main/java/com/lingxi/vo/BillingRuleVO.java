package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 计费规则视图对象。
 */
@Data
@Schema(description = "计费规则")
public class BillingRuleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String category;

    private String target;

    private String model;

    private String unit;

    private BigDecimal priceIn;

    private BigDecimal priceOut;

    private BigDecimal priceCachedIn;

    private BigDecimal costIn;

    private BigDecimal costOut;

    private BigDecimal multiplier;

    private Integer enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
