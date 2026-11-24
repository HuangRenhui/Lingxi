package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 新建产物请求。
 */
@Data
@Schema(description = "新建产物")
public class ArtifactCreateRequest {

    /** 作品标题，不能为空 */
    @NotBlank(message = "作品标题不能为空")
    @Size(max = 128, message = "作品标题不能超过 128 个字符")
    @Schema(description = "作品标题", example = "蓝调雪后山地旅舍")
    private String title;

    /** 产物类型 */
    @NotBlank(message = "产物类型不能为空")
    @Pattern(regexp = "IMAGE|VIDEO|AUDIO", message = "产物类型仅支持 IMAGE/VIDEO/AUDIO")
    @Schema(description = "产物类型", example = "IMAGE")
    private String type;

    /** 创作工具标识，可为空 */
    @Size(max = 64, message = "工具标识不能超过 64 个字符")
    @Schema(description = "创作工具标识", example = "nanobanana")
    private String toolKey;

    /** 创作工具名称，可为空 */
    @Size(max = 64, message = "工具名称不能超过 64 个字符")
    @Schema(description = "创作工具名称", example = "Nano Banana")
    private String toolName;

    /** 生成提示词，可为空 */
    @Size(max = 16000, message = "提示词过长（上限 16000 字符）")
    @Schema(description = "生成提示词", example = "覆雪木石旅舍坐落冷蓝山林，窗内暖光与入口灯笼")
    private String prompt;

    /** 产物地址或对象存储 Key，不能为空 */
    @NotBlank(message = "产物地址不能为空")
    @Size(max = 512, message = "产物地址不能超过 512 个字符")
    @Schema(description = "产物地址或对象存储 Key", example = "/images/showcase-sc-nano-lodge.jpg")
    private String url;

    /** 视频封面地址，可为空 */
    @Size(max = 512, message = "封面地址不能超过 512 个字符")
    @Schema(description = "视频封面地址", example = "/images/showcase-sc-seedance-ink.jpg")
    private String posterUrl;

    /** 文件大小（字节），缺省为 0 */
    @Min(value = 0, message = "文件大小不能为负数")
    @Schema(description = "文件大小（字节）", example = "2516582")
    private Long sizeBytes;
}
