package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内通知实体，对应表 notification。
 *
 * <p>通知由系统或业务模块产生，用户侧只做已读与删除。</p>
 */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户 */
    private Long userId;

    /** 通知标题 */
    private String title;

    /** 通知正文 */
    private String content;

    /** 类型：SYSTEM / TASK / BILLING */
    private String type;

    /** 是否已读：1 已读，0 未读 */
    private Integer readFlag;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
