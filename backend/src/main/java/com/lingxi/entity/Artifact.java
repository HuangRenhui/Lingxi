package com.lingxi.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创作产物实体，对应表 artifact。
 *
 * <p>产物是任务或创作工具的输出，记录标题、类型、地址与展示信息；
 * 文件本身存放在对象存储，这里只保留元数据。</p>
 */
@Data
@TableName("artifact")
public class Artifact {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 属主用户 ID，用于数据隔离 */
    private Long userId;

    /** 作品标题 */
    private String title;

    /** 产物类型：IMAGE / VIDEO / AUDIO */
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

    /** 审核状态：PENDING / APPROVED / REJECTED */
    private String status;

    /** 创建时间，由数据库默认值填充 */
    private LocalDateTime createdAt;

    /** 更新时间，由数据库自动维护 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0 正常，1 已删除 */
    @TableLogic
    private Integer deleted;
}
