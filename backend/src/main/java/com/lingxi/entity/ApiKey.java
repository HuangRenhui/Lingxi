package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API Key 实体，对应表 api_key。
 *
 * <p>出于安全考虑只保存密钥的 SHA-256 摘要与展示前缀，明文仅在创建时返回一次。</p>
 */
@Data
@TableName("api_key")
public class ApiKey {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 属主用户 */
    private Long userId;

    /** 密钥名称 */
    private String name;

    /** 展示前缀，例如 lx_1a2b3c4d */
    private String keyPrefix;

    /** 密钥 SHA-256 摘要 */
    private String keyHash;

    /** 状态：1 启用，0 禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除 */
    @TableLogic
    private Integer deleted;
}
