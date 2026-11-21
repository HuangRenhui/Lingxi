package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 优惠券视图对象。
 */
@Data
@Schema(description = "优惠券")
public class CouponVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    /** FULL_REDUCTION 满减 / DISCOUNT 折扣 */
    private String type;

    private BigDecimal value;

    private BigDecimal threshold;

    private Integer total;

    private Integer claimed;

    private LocalDate validFrom;

    private LocalDate validTo;

    private Integer enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
