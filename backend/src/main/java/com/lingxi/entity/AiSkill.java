package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 技能实体，对应表 ai_skill。
 *
 * <p>技能是一段描述「该用哪些工具、按什么步骤完成任务」的 Markdown 说明，
 * 归属于创建它的用户，后续可在任务编排中授权引用。</p>
 */
@Data
@TableName("ai_skill")
public class AiSkill {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 属主用户 ID，用于数据隔离 */
    private Long userId;

    /** 技能名称，统一以 / 开头 */
    private String name;

    /** 技能简介 */
    private String description;

    /** 技能内容（Markdown） */
    private String content;

    /** 来源分类：OFFICIAL / COMMUNITY */
    private String category;

    /** 状态：1 启用，0 停用 */
    private Integer status;

    /** 创建时间，由数据库默认值填充 */
    private LocalDateTime createdAt;

    /** 更新时间，由数据库自动维护 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0 正常，1 已删除 */
    @TableLogic
    private Integer deleted;
}
