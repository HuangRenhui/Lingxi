package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 应用视图对象。
 */
@Data
@Schema(description = "AI 应用")
public class ApplicationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 应用 ID */
    private Long id;

    /** 应用名称 */
    private String name;

    /** 应用描述 */
    private String description;

    /** 应用类型 */
    private String appType;

    /** 状态：1 启用，0 禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
