package com.lingxi.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单号生成器。
 *
 * <p>格式：{@code LX} + {@code yyyyMMddHHmmssSSS}（17 位）+ 实例随机盐（3 位）+ 自增序号（5 位），
 * 共 27 个字符（列宽 32）。</p>
 *
 * <p>历史实现是「秒级时间戳 + 4 位随机数」：同一秒内随机空间只有 1 万，并发下单必然撞唯一索引。
 * 仅把精度提到毫秒仍然不够——Windows 上时钟精度约 15ms，同一毫秒内 2000 次生成在 1e6 随机空间里
 * 的碰撞期望已经接近 2 次（单元测试已实测复现）。因此这里改用进程内自增序号：
 * 同一毫秒内最多 10 万个不重复值，再由 3 位实例盐隔离多实例部署。</p>
 */
public final class OrderNoGenerator {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final String PREFIX = "LX";
    private static final int SEQUENCE_BOUND = 100_000;

    /** 进程级随机盐：多实例部署时降低「同毫秒同序号」的碰撞概率。 */
    private static final String INSTANCE_SALT =
            String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    /** 进程内自增序号：从随机起点开始，避免每次重启都从 0 起步。 */
    private static final AtomicInteger SEQUENCE =
            new AtomicInteger(ThreadLocalRandom.current().nextInt(SEQUENCE_BOUND));

    private OrderNoGenerator() {
    }

    public static String next() {
        int sequence = Math.floorMod(SEQUENCE.getAndIncrement(), SEQUENCE_BOUND);
        return PREFIX + LocalDateTime.now().format(TIME) + INSTANCE_SALT
                + String.format("%05d", sequence);
    }
}
