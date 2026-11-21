package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 新建内容举报请求。
 */
@Data
@Schema(description = "新建内容举报")
public class ModerationReportCreateRequest {

    @NotBlank(message = "举报对象类型不能为空")
    @Size(max = 16, message = "举报对象类型不能超过 16 个字符")
    @Schema(description = "举报对象类型：IMAGE / VIDEO / ARTIFACT / TEXT", example = "IMAGE")
    private String targetType;

    @NotNull(message = "举报对象 ID 不能为空")
    @Schema(description = "举报对象 ID", example = "1")
    private Long targetId;

    @NotBlank(message = "举报原因不能为空")
    @Size(max = 255, message = "举报原因不能超过 255 个字符")
    @Schema(description = "举报原因", example = "含违规或敏感内容")
    private String reason;
}
