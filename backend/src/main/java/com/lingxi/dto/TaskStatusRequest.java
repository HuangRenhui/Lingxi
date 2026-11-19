package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 任务启停请求。
 */
@Data
@Schema(description = "任务启停请求")
public class TaskStatusRequest {

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "ENABLED|DISABLED", message = "状态仅支持 ENABLED / DISABLED")
    @Schema(description = "ENABLED 启用 / DISABLED 停用", example = "DISABLED")
    private String status;
}
