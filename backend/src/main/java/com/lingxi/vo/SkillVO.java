package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 技能视图对象。
 */
@Data
@Schema(description = "技能")
public class SkillVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 技能 ID */
    private Long id;

    /** 技能名称 */
    private String name;

    /** 技能简介 */
    private String description;

    /** 技能内容（Markdown） */
    private String content;

    /** 来源分类 */
    private String category;

    /** 状态：1 启用，0 停用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
