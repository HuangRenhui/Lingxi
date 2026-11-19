package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务视图。
 */
@Data
@Schema(description = "定时任务")
public class TaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String promptTemplate;

    private String model;

    private String runLocation;

    private String timezone;

    private String scheduleType;

    private String cronExpr;

    private Integer intervalMinutes;

    private String executeTime;

    private String weekDays;

    private Integer jitterSeconds;

    private Integer maxRounds;

    private String status;

    private LocalDateTime lastRunAt;

    private LocalDateTime nextRunAt;

    private Long runCount;

    private String lastOutput;

    private LocalDateTime createdAt;
}
