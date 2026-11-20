package com.lingxi.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金额规则。
 *
 * <p>平台余额与订单金额统一用 DECIMAL(18,6)：入库前必须归一化到 6 位小数，
 * 否则 {@code equals}/{@code compareTo} 混用会判等失败，DB 也会做隐式四舍五入产生尾差。</p>
 */
public final class MoneyUtil {

    /** 与数据库 DECIMAL(18,6) 对齐的小数位。 */
    public static final int SCALE = 6;

    private MoneyUtil() {
    }

    /**
     * 归一化金额：null 与负数一律视为非法参数。
     *
     * @throws BusinessException 金额为空或为负
     */
    public static BigDecimal normalize(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 归一化但不抛异常：非法值按 0 处理，用于「配置项」这类不该放大成多扣费的数据。
     */
    public static BigDecimal normalizeOrZero(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        return amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static boolean isPositive(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
}
