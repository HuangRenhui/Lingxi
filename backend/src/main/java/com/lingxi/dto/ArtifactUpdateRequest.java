package com.lingxi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 更新产物请求。
 *
 * <p>所有字段均为可选，仅更新传入的字段；收藏与回收站状态也通过本接口切换。</p>
 */
@Data
@Schema(description = "更新产物")
public class ArtifactUpdateRequest {

    /** 作品标题，传入时不能为空白 */
    @Size(max = 128, message = "作品标题不能超过 128 个字符")
    @Schema(description = "作品标题", example = "蓝调雪后山地旅舍")
    private String title;

    /** 产物类型 */
    @Pattern(regexp = "IMAGE|VIDEO|AUDIO", message = "产物类型仅支持 IMAGE/VIDEO/AUDIO")
    @Schema(description = "产物类型", example = "IMAGE")
    private String type;

    /** 创作工具标识，传入空串表示清空 */
    @Size(max = 64, message = "工具标识不能超过 64 个字符")
    @Schema(description = "创作工具标识")
    private String toolKey;

    /** 创作工具名称，传入空串表示清空 */
    @Size(max = 64, message = "工具名称不能超过 64 个字符")
    @Schema(description = "创作工具名称")
    private String toolName;

    /** 生成提示词 */
    @Size(max = 16000, message = "提示词过长（上限 16000 字符）")
    @Schema(description = "生成提示词")
    private String prompt;

    /** 产物地址或对象存储 Key，传入时不能为空白 */
    @Size(max = 512, message = "产物地址不能超过 512 个字符")
    @Schema(description = "产物地址或对象存储 Key")
    private String url;

    /** 视频封面地址，传入空串表示清空 */
    @Size(max = 512, message = "封面地址不能超过 512 个字符")
    @Schema(description = "视频封面地址")
    private String posterUrl;

    /** 文件大小（字节） */
    @Min(value = 0, message = "文件大小不能为负数")
    @Schema(description = "文件大小（字节）")
    private Long sizeBytes;

    /** 收藏：1 是，0 否 */
    @Min(value = 0, message = "收藏状态仅支持 0 或 1")
    @Max(value = 1, message = "收藏状态仅支持 0 或 1")
    @Schema(description = "收藏：1 是，0 否", example = "1")
    private Integer favorite;

    /** 回收站：1 是，0 否 */
    @Min(value = 0, message = "回收站状态仅支持 0 或 1")
    @Max(value = 1, message = "回收站状态仅支持 0 或 1")
    @Schema(description = "回收站：1 是，0 否", example = "0")
    private Integer trashed;

    /** 审核状态 */
    @Pattern(regexp = "PENDING|APPROVED|REJECTED", message = "审核状态仅支持 PENDING/APPROVED/REJECTED")
    @Schema(description = "审核状态", example = "APPROVED")
    private String status;
}
