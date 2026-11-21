package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内容举报实体，对应表 moderation_report。
 */
@Data
@TableName("moderation_report")
public class ModerationReport {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 举报对象类型：IMAGE / VIDEO / ARTIFACT / TEXT */
    private String targetType;

    /** 举报对象 ID */
    private Long targetId;

    /** 举报原因 */
    private String reason;

    /** 举报人 */
    private Long reporterId;

    /** 状态：PENDING / RESOLVED / REJECTED */
    private String status;

    /** 处理结果 */
    private String result;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
