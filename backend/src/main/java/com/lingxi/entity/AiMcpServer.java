package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 连接器（MCP Server）实体，对应表 ai_mcp_server。
 *
 * <p>连接器用于接入第三方服务，让 Agent 在任务中调用外部工具；
 * 当前按用户维护连接配置，后续可扩展任务级授权。</p>
 */
@Data
@TableName("ai_mcp_server")
public class AiMcpServer {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 属主用户 ID，用于数据隔离 */
    private Long userId;

    /** 连接器名称 */
    private String name;

    /** 连接器简介 */
    private String description;

    /** 接入类型：HTTP / SSE / STDIO */
    private String serverType;

    /** 服务地址或启动命令 */
    private String endpoint;

    /** 状态：1 已连接，0 未连接 */
    private Integer status;

    /** 创建时间，由数据库默认值填充 */
    private LocalDateTime createdAt;

    /** 更新时间，由数据库自动维护 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0 正常，1 已删除 */
    @TableLogic
    private Integer deleted;
}
