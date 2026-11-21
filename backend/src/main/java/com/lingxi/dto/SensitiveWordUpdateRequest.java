package com.lingxi.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 更新敏感词状态请求。
 */
@Data
public class SensitiveWordUpdateRequest {

    @NotNull(message = "请提供启用状态")
    private Boolean enabled;
}
