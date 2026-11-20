package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 更新连接器请求。
 *
 * <p>所有字段均为可选，仅更新传入的字段，便于前端做局部编辑。</p>
 */
@Data
@Schema(description = "更新连接器")
public class ConnectorUpdateRequest {

    /** 连接器名称，传入时不能为空白 */
    @Size(max = 64, message = "连接器名称不能超过 64 个字符")
    @Schema(description = "连接器名称", example = "GitHub")
    private String name;

    /** 连接器简介，传入空串表示清空 */
    @Size(max = 255, message = "连接器简介不能超过 255 个字符")
    @Schema(description = "连接器简介")
    private String description;

    /** 接入类型 */
    @Pattern(regexp = "HTTP|SSE|STDIO", message = "接入类型仅支持 HTTP/SSE/STDIO")
    @Schema(description = "接入类型", example = "HTTP")
    private String serverType;

    /** 服务地址或启动命令 */
    @Size(max = 512, message = "服务地址不能超过 512 个字符")
    @Schema(description = "服务地址或启动命令")
    private String endpoint;

    /** 状态：1 已连接，0 未连接 */
    @Min(value = 0, message = "状态仅支持 0 或 1")
    @Max(value = 1, message = "状态仅支持 0 或 1")
    @Schema(description = "状态：1 已连接，0 未连接", example = "1")
    private Integer status;
}
