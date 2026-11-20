package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知视图对象。
 */
@Data
@Schema(description = "站内通知")
public class NotificationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String content;

    private String type;

    /** 1 已读，0 未读 */
    private Integer readFlag;

    private LocalDateTime createdAt;
}
