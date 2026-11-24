package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 更新通知请求，仅更新传入的字段。
 */
@Data
@Schema(description = "更新通知")
public class NotificationUpdateRequest {

    @Size(max = 128, message = "通知标题不能超过 128 个字符")
    @Schema(description = "通知标题")
    private String title;

    @Size(max = 500, message = "通知正文不能超过 500 个字符")
    @Schema(description = "通知正文")
    private String content;

    @Pattern(regexp = "SYSTEM|TASK|BILLING", message = "类型仅支持 SYSTEM/TASK/BILLING")
    @Schema(description = "类型")
    private String type;

    @Min(value = 0, message = "已读状态仅支持 0 或 1")
    @Max(value = 1, message = "已读状态仅支持 0 或 1")
    @Schema(description = "是否已读：1 是 / 0 否")
    private Integer readFlag;
}
