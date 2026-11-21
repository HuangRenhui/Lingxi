package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内容审核敏感词实体，对应表 moderation_sensitive_word。
 */
@Data
@TableName("moderation_sensitive_word")
public class ModerationSensitiveWord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 敏感词 */
    private String word;

    /** 分类：交易 / 暴力 / 政治 / 违法 / 隐私 / 色情 / 其他 */
    private String category;

    /** 级别：高 / 中 / 低 */
    private String level;

    /** 命中次数，由后续内容检测流程累加 */
    private Integer hits;

    /** 是否启用：1 启用，0 停用 */
    private Integer enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
