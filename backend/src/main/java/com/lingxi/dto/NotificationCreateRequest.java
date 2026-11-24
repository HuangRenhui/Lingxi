package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 新建通知请求。
 *
 * <p>接收人固定为当前登录用户，避免前端伪造 userId 给他人发通知。</p>
 */
@Data
@Schema(description = "新建通知")
public class NotificationCreateRequest {

    @NotBlank(message = "通知标题不能为空")
    @Size(max = 128, message = "通知标题不能超过 128 个字符")
    @Schema(description = "通知标题", example = "任务执行完成")
    private String title;

    @Size(max = 500, message = "通知正文不能超过 500 个字符")
    @Schema(description = "通知正文")
    private String content;

    @Pattern(regexp = "SYSTEM|TASK|BILLING", message = "类型仅支持 SYSTEM/TASK/BILLING")
    @Schema(description = "类型", example = "SYSTEM")
    private String type;
}
