package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 更新定时任务请求：字段为空表示不修改。
 */
@Data
@Schema(description = "更新定时任务请求")
public class TaskUpdateRequest {

    @Size(max = 128, message = "任务名称不能超过 128 个字符")
    private String name;

    // 与 TEXT 列字节上限对齐，避免超长内容打到数据库才报错
    @Size(max = 16000, message = "提示词过长（上限 16000 字符）")
    private String promptTemplate;

    @Size(max = 64, message = "执行模型不能超过 64 个字符")
    private String model;

    // 允许空串：提交 "" 表示「保持原值」，@Pattern 不允许空串会直接 400
    @Pattern(regexp = "^(?:CLOUD|LOCAL)?$", message = "运行位置仅支持 CLOUD / LOCAL")
    private String runLocation;

    @Size(max = 64, message = "时区不能超过 64 个字符")
    @Pattern(regexp = "^(?:[A-Za-z][A-Za-z0-9_+\\-]*(/[A-Za-z0-9_+\\-]+)*)?$", message = "时区格式不正确，应为 IANA 时区")
    private String timezone;

    @Pattern(regexp = "^(?:INTERVAL|HOURLY|DAILY|WEEKLY|CRON)?$", message = "不支持的任务频率")
    private String scheduleType;

    @Size(max = 64, message = "Cron 表达式不能超过 64 个字符")
    private String cronExpr;

    @Min(value = 1, message = "间隔分钟数至少为 1")
    private Integer intervalMinutes;

    @Pattern(regexp = "^(?:([01]\\d|2[0-3]):[0-5]\\d)?$", message = "执行时间格式应为 HH:mm")
    private String executeTime;

    @Size(max = 16, message = "星期集合不能超过 16 个字符")
    @Pattern(regexp = "^(?:[1-7](,[1-7])*)?$", message = "星期集合格式应为 1,3,5")
    private String weekDays;

    @Min(value = 0, message = "抖动不能小于 0 秒")
    @Max(value = 60, message = "抖动不能超过 60 秒")
    private Integer jitterSeconds;

    @Min(value = 1, message = "最大轮次至少为 1")
    @Max(value = 50, message = "最大轮次不能超过 50")
    private Integer maxRounds;
}
