package com.lingxi.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量操作请求：仅携带主键集合。
 *
 * <p>通知批量删除、产物批量更新/删除共用，减少逐条请求带来的往返。</p>
 */
@Data
public class IdsRequest {

    @NotEmpty(message = "ID 列表不能为空")
    private List<Long> ids;
}
