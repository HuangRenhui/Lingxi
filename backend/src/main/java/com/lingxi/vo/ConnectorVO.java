package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 连接器视图对象。
 */
@Data
@Schema(description = "连接器")
public class ConnectorVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 连接器 ID */
    private Long id;

    /** 连接器名称 */
    private String name;

    /** 连接器简介 */
    private String description;

    /** 接入类型 */
    private String serverType;

    /** 服务地址或启动命令 */
    private String endpoint;

    /** 状态：1 已连接，0 未连接 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
