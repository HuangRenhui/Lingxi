package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 新建连接器请求。
 */
@Data
@Schema(description = "新建连接器")
public class ConnectorCreateRequest {

    /** 连接器名称，不能为空 */
    @NotBlank(message = "连接器名称不能为空")
    @Size(max = 64, message = "连接器名称不能超过 64 个字符")
    @Schema(description = "连接器名称", example = "GitHub")
    private String name;

    /** 连接器简介，可为空 */
    @Size(max = 255, message = "连接器简介不能超过 255 个字符")
    @Schema(description = "连接器简介", example = "管理仓库、Issue、PR 与 Actions 工作流")
    private String description;

    /** 接入类型，缺省为 HTTP */
    @Pattern(regexp = "HTTP|SSE|STDIO", message = "接入类型仅支持 HTTP/SSE/STDIO")
    @Schema(description = "接入类型", example = "HTTP", defaultValue = "HTTP")
    private String serverType;

    /** 服务地址或启动命令，可为空 */
    @Size(max = 512, message = "服务地址不能超过 512 个字符")
    @Schema(description = "服务地址或启动命令", example = "https://mcp.example.com/github")
    private String endpoint;
}
