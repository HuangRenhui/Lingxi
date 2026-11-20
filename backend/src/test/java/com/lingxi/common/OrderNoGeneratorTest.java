package com.lingxi.common;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 订单号生成规则回归：长度受列宽约束，且连续生成不应重复。
 */
class OrderNoGeneratorTest {

    @Test
    void generatesFixedLengthWithPrefix() {
        String orderNo = OrderNoGenerator.next();
        assertEquals("LX", orderNo.substring(0, 2));
        // LX(2) + yyyyMMddHHmmssSSS(17) + 实例盐(3) + 自增序号(5) = 27
        assertEquals(27, orderNo.length());
        assertTrue(orderNo.length() <= 32, "订单号长度必须小于等于列宽 32");
    }

    @Test
    void generatesDistinctValuesInBurst() {
        // 2000 次连续生成会落在同一毫秒内（Windows 时钟精度约 15ms），
        // 纯随机后缀在此密度下会碰撞，必须由自增序号保证唯一。
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 5000; i++) {
            assertTrue(seen.add(OrderNoGenerator.next()), "同一批生成的订单号出现重复");
        }
    }
}
