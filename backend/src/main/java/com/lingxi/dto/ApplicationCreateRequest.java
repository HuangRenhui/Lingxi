package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 新建 AI 应用请求。
 */
@Data
@Schema(description = "新建 AI 应用")
public class ApplicationCreateRequest {

    /** 应用名称，不能为空 */
    @NotBlank(message = "应用名称不能为空")
    @Size(max = 64, message = "应用名称不能超过 64 个字符")
    @Schema(description = "应用名称", example = "商品海报工厂")
    private String name;

    /** 应用描述，可为空 */
    @Size(max = 255, message = "应用描述不能超过 255 个字符")
    @Schema(description = "应用描述", example = "上传商品图，批量生成营销海报")
    private String description;

    /** 应用类型，缺省为 GENERAL */
    @Pattern(regexp = "GENERAL|IMAGE|VIDEO|AUDIO", message = "应用类型仅支持 GENERAL/IMAGE/VIDEO/AUDIO")
    @Schema(description = "应用类型", example = "IMAGE", defaultValue = "GENERAL")
    private String appType;
}
