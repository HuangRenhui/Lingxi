package com.lingxi.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感词视图对象。
 */
@Data
public class SensitiveWordVO {

    private Long id;

    private String word;

    private String category;

    private String level;

    private Integer hits;

    /** 使用布尔值，避免前端把 0/1 直接绑定到开关导致状态被误改 */
    private Boolean enabled;

    private LocalDateTime createdAt;
}
