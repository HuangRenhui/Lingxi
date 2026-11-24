package com.lingxi.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 产物批量更新请求：至少提供一个待更新字段。
 */
@Data
public class ArtifactBatchUpdateRequest {

    @NotEmpty(message = "ID 列表不能为空")
    private List<Long> ids;

    /** 收藏标记：0 或 1，为空表示不修改 */
    private Integer favorite;

    /** 回收站标记：0 或 1，为空表示不修改 */
    private Integer trashed;
}
