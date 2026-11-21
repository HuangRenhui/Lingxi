package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 内容举报视图对象。
 */
@Data
@Schema(description = "内容举报")
public class ModerationReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String targetType;

    private Long targetId;

    private String reason;

    private Long reporterId;

    /** PENDING / RESOLVED / REJECTED */
    private String status;

    private String result;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
