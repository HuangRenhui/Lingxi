package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 新建定时任务请求。
 */
@Data
@Schema(description = "新建定时任务请求")
public class TaskCreateRequest {

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 128, message = "任务名称不能超过 128 个字符")
    @Schema(description = "任务名称", example = "每日 AI 资讯汇总")
    private String name;

    @NotBlank(message = "提示词不能为空")
    // TEXT 列上限为 65535 字节，utf8mb4 下中文约 3 字节，按 16000 字符留足余量
    @Size(max = 16000, message = "提示词过长（上限 16000 字符）")
    @Schema(description = "提示词模板，支持 {{date}} / {{run_count}} / {{last_output}}")
    private String promptTemplate;

    @NotBlank(message = "执行模型不能为空")
    @Size(max = 64, message = "执行模型不能超过 64 个字符")
    @Schema(description = "执行模型", example = "gpt-4o")
    private String model;

    // 可选字段统一允许空串：前端清空输入框会提交 ""，@Pattern 默认会把它判为非法，
    // 从而绕过服务层的 defaultIfBlank 兜底并直接返回 400。null 与 "" 都表示「使用默认值」。
    @Pattern(regexp = "^(?:CLOUD|LOCAL)?$", message = "运行位置仅支持 CLOUD / LOCAL")
    @Schema(description = "运行位置：CLOUD 云端 / LOCAL 这台设备", example = "CLOUD")
    private String runLocation;

    @Size(max = 64, message = "时区不能超过 64 个字符")
    @Pattern(regexp = "^(?:[A-Za-z][A-Za-z0-9_+\\-]*(/[A-Za-z0-9_+\\-]+)*)?$", message = "时区格式不正确，应为 IANA 时区")
    @Schema(description = "IANA 时区", example = "Asia/Shanghai")
    private String timezone;

    @Pattern(regexp = "^(?:INTERVAL|HOURLY|DAILY|WEEKLY|CRON)?$", message = "不支持的任务频率")
    @Schema(description = "执行频率：INTERVAL / HOURLY / DAILY / WEEKLY / CRON", example = "DAILY")
    private String scheduleType;

    @Size(max = 64, message = "Cron 表达式不能超过 64 个字符")
    @Schema(description = "Cron 表达式（6 段）")
    private String cronExpr;

    @Min(value = 1, message = "间隔分钟数至少为 1")
    @Schema(description = "INTERVAL 模式的间隔分钟数", example = "60")
    private Integer intervalMinutes;

    @Pattern(regexp = "^(?:([01]\\d|2[0-3]):[0-5]\\d)?$", message = "执行时间格式应为 HH:mm")
    @Schema(description = "执行时间 HH:mm", example = "09:00")
    private String executeTime;

    @Size(max = 16, message = "星期集合不能超过 16 个字符")
    @Pattern(regexp = "^(?:[1-7](,[1-7])*)?$", message = "星期集合格式应为 1,3,5")
    @Schema(description = "WEEKLY 模式的星期集合，1-7 逗号分隔", example = "1,3,5")
    private String weekDays;

    @Min(value = 0, message = "抖动不能小于 0 秒")
    @Max(value = 60, message = "抖动不能超过 60 秒")
    @Schema(description = "触发抖动秒数 0-60", example = "0")
    private Integer jitterSeconds;

    @Min(value = 1, message = "最大轮次至少为 1")
    @Max(value = 50, message = "最大轮次不能超过 50")
    @Schema(description = "单次运行最大轮次 1-50", example = "50")
    private Integer maxRounds;
}
