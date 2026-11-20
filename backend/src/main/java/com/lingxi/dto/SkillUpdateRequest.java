package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 更新技能请求。
 *
 * <p>所有字段均为可选，仅更新传入的字段，便于前端做局部编辑。</p>
 */
@Data
@Schema(description = "更新技能")
public class SkillUpdateRequest {

    /** 技能名称，传入空串表示非法 */
    @Size(max = 64, message = "技能名称不能超过 64 个字符")
    @Schema(description = "技能名称", example = "/weekly-report")
    private String name;

    /** 技能简介，传入空串表示清空 */
    @Size(max = 255, message = "技能简介不能超过 255 个字符")
    @Schema(description = "技能简介")
    private String description;

    /** 技能内容（Markdown） */
    @Size(max = 20000, message = "技能内容不能超过 20000 个字符")
    @Schema(description = "技能内容（Markdown）")
    private String content;

    /** 来源分类 */
    @Pattern(regexp = "OFFICIAL|COMMUNITY", message = "分类仅支持 OFFICIAL/COMMUNITY")
    @Schema(description = "分类", example = "COMMUNITY")
    private String category;

    /** 状态：1 启用，0 停用 */
    @Min(value = 0, message = "状态仅支持 0 或 1")
    @Max(value = 1, message = "状态仅支持 0 或 1")
    @Schema(description = "状态：1 启用，0 停用", example = "1")
    private Integer status;
}
