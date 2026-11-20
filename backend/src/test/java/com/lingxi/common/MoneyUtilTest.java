package com.lingxi.common;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 金额规则回归：入库前必须归一化到 6 位小数，非法值不得进入资金链路。
 */
class MoneyUtilTest {

    @Test
    void normalizesToScaleSix() {
        assertEquals(new BigDecimal("2.100000"), MoneyUtil.normalize(new BigDecimal("2.1")));
        assertEquals(new BigDecimal("0.000001"), MoneyUtil.normalize(new BigDecimal("0.0000009")));
        assertEquals(new BigDecimal("100.000000"), MoneyUtil.normalize(new BigDecimal("100")));
    }

    @Test
    void rejectsNullAndNegative() {
        assertThrows(BusinessException.class, () -> MoneyUtil.normalize(null));
        assertThrows(BusinessException.class, () -> MoneyUtil.normalize(new BigDecimal("-0.01")));
    }

    @Test
    void normalizeOrZeroFallsBackForBadConfig() {
        assertEquals(new BigDecimal("0.000000"), MoneyUtil.normalizeOrZero(null));
        assertEquals(new BigDecimal("0.000000"), MoneyUtil.normalizeOrZero(new BigDecimal("-1")));
        assertEquals(new BigDecimal("68.000000"), MoneyUtil.normalizeOrZero(new BigDecimal("68")));
    }

    @Test
    void detectsPositiveAmount() {
        assertTrue(MoneyUtil.isPositive(new BigDecimal("0.000001")));
        assertFalse(MoneyUtil.isPositive(BigDecimal.ZERO));
        assertFalse(MoneyUtil.isPositive(null));
    }
}
