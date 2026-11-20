package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 新增登录设备请求。
 */
@Data
@Schema(description = "新增登录设备")
public class DeviceCreateRequest {

    @NotBlank(message = "设备名称不能为空")
    @Size(max = 64, message = "设备名称不能超过 64 个字符")
    @Schema(description = "设备名称", example = "MacBook Pro")
    private String name;

    @Size(max = 32, message = "操作系统不能超过 32 个字符")
    @Schema(description = "操作系统", example = "macOS 15")
    private String os;

    @Size(max = 32, message = "浏览器不能超过 32 个字符")
    @Schema(description = "浏览器", example = "Chrome 131")
    private String browser;

    @Size(max = 45, message = "IP 地址不能超过 45 个字符")
    @Schema(description = "登录 IP", example = "127.0.0.1")
    private String ip;

    @Size(max = 64, message = "登录地不能超过 64 个字符")
    @Schema(description = "登录地", example = "本地")
    private String location;
}
