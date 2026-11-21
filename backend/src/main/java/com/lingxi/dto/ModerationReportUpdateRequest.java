package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 更新内容举报请求，仅更新传入的字段。
 */
@Data
@Schema(description = "更新内容举报")
public class ModerationReportUpdateRequest {

    @Size(max = 16, message = "举报对象类型不能超过 16 个字符")
    @Schema(description = "举报对象类型")
    private String targetType;

    @Size(max = 255, message = "举报原因不能超过 255 个字符")
    @Schema(description = "举报原因")
    private String reason;

    @Pattern(regexp = "PENDING|RESOLVED|REJECTED", message = "状态仅支持 PENDING/RESOLVED/REJECTED")
    @Schema(description = "状态：PENDING / RESOLVED / REJECTED")
    private String status;

    @Size(max = 255, message = "处理结果不能超过 255 个字符")
    @Schema(description = "处理结果")
    private String result;
}
