package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 新建技能请求。
 */
@Data
@Schema(description = "新建技能")
public class SkillCreateRequest {

    /** 技能名称，服务端会自动补全开头的 / */
    @NotBlank(message = "技能名称不能为空")
    // 列宽 64，服务端会补一个前导 '/'，因此入参最多 63 个字符
    @Size(max = 63, message = "技能名称不能超过 63 个字符（会自动补全开头的 /）")
    @Schema(description = "技能名称", example = "/weekly-report")
    private String name;

    /** 技能简介，可为空 */
    @Size(max = 255, message = "技能简介不能超过 255 个字符")
    @Schema(description = "技能简介", example = "整理一周数据并生成结构化周报")
    private String description;

    /** 技能内容（Markdown），不能为空 */
    @NotBlank(message = "技能内容不能为空")
    // ai_skill.content 为 TEXT（65535 字节），按 utf8mb4 中文 3 字节折算留余量
    @Size(max = 20000, message = "技能内容不能超过 20000 个字符")
    @Schema(description = "技能内容（Markdown）", example = "# 周报\n\n汇总本周数据并输出结论")
    private String content;

    /** 来源分类，缺省为 COMMUNITY */
    @Pattern(regexp = "OFFICIAL|COMMUNITY", message = "分类仅支持 OFFICIAL/COMMUNITY")
    @Schema(description = "分类", example = "COMMUNITY", defaultValue = "COMMUNITY")
    private String category;
}
