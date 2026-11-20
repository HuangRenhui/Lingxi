package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产物视图对象。
 */
@Data
@Schema(description = "产物")
public class ArtifactVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 产物 ID */
    private Long id;

    /** 作品标题 */
    private String title;

    /** 产物类型 */
    private String type;

    /** 创作工具标识 */
    private String toolKey;

    /** 创作工具名称 */
    private String toolName;

    /** 生成提示词 */
    private String prompt;

    /** 产物地址或对象存储 Key */
    private String url;

    /** 视频封面地址 */
    private String posterUrl;

    /** 文件大小（字节） */
    private Long sizeBytes;

    /** 收藏：1 是，0 否 */
    private Integer favorite;

    /** 回收站：1 是，0 否 */
    private Integer trashed;

    /** 审核状态 */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}
