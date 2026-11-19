package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务定义（B7 无人值守 Agent 编排引擎）。
 */
@Data
@TableName("ai_task")
public class AiTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    /** 含 {{date}} / {{run_count}} / {{last_output}} 等占位符。 */
    private String promptTemplate;

    private String model;

    /** CLOUD / LOCAL */
    private String runLocation;

    /** IANA 时区，如 Asia/Shanghai */
    private String timezone;

    /** INTERVAL / HOURLY / DAILY / WEEKLY / CRON */
    private String scheduleType;

    private String cronExpr;

    private Integer intervalMinutes;

    /** HH:mm */
    private String executeTime;

    /** 1-7 逗号分隔（WEEKLY） */
    private String weekDays;

    private Integer jitterSeconds;

    private Integer maxRounds;

    /** ENABLED / DISABLED */
    private String status;

    private LocalDateTime lastRunAt;

    private LocalDateTime nextRunAt;

    private Long runCount;

    private String lastOutput;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
