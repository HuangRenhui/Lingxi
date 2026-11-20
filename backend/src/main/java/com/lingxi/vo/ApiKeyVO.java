package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * API Key 视图对象。
 *
 * <p>{@code key} 仅在创建成功时返回明文，其余场景为 null。</p>
 */
@Data
@Schema(description = "API Key")
public class ApiKeyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    /** 展示前缀，可用于界面掩码展示 */
    private String keyPrefix;

    /** 明文密钥，仅创建时返回 */
    private String key;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
