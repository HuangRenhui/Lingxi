package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 更新 AI 应用请求。
 *
 * <p>所有字段均为可选，仅更新传入的字段，便于前端做局部编辑。</p>
 */
@Data
@Schema(description = "更新 AI 应用")
public class ApplicationUpdateRequest {

    /** 应用名称，传入时不能为空白 */
    @Size(max = 64, message = "应用名称不能超过 64 个字符")
    @Schema(description = "应用名称", example = "商品海报工厂")
    private String name;

    /** 应用描述，传入空串表示清空 */
    @Size(max = 255, message = "应用描述不能超过 255 个字符")
    @Schema(description = "应用描述")
    private String description;

    /** 应用类型 */
    @Pattern(regexp = "GENERAL|IMAGE|VIDEO|AUDIO", message = "应用类型仅支持 GENERAL/IMAGE/VIDEO/AUDIO")
    @Schema(description = "应用类型", example = "IMAGE")
    private String appType;

    /** 状态：1 启用，0 禁用 */
    @Min(value = 0, message = "状态仅支持 0 或 1")
    @Max(value = 1, message = "状态仅支持 0 或 1")
    @Schema(description = "状态：1 启用，0 禁用", example = "1")
    private Integer status;
}
