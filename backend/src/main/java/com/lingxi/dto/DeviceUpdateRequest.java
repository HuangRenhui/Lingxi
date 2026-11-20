package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 更新设备请求，目前仅支持重命名。
 */
@Data
@Schema(description = "更新设备")
public class DeviceUpdateRequest {

    @Size(max = 64, message = "设备名称不能超过 64 个字符")
    @Schema(description = "设备名称", example = "办公电脑")
    private String name;
}
