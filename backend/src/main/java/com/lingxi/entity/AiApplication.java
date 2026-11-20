package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 应用实体，对应表 ai_application。
 *
 * <p>应用归属于创建它的用户，后续可在此基础上扩展工具、配额等能力。</p>
 */
@Data
@TableName("ai_application")
public class AiApplication {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 属主用户 ID，用于数据隔离 */
    private Long userId;

    /** 应用名称 */
    private String name;

    /** 应用描述 */
    private String description;

    /** 应用类型：GENERAL / IMAGE / VIDEO / AUDIO */
    private String appType;

    /** 状态：1 启用，0 禁用 */
    private Integer status;

    /** 创建时间，由数据库默认值填充 */
    private LocalDateTime createdAt;

    /** 更新时间，由数据库自动维护 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0 正常，1 已删除 */
    @TableLogic
    private Integer deleted;
}
