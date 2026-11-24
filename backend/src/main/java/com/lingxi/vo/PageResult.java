package com.lingxi.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 通用分页结果。
 *
 * <p>只返回当前页数据与总数，避免前端一次拉取全量后再本地过滤；
 * 调用方按 {@code total} 渲染分页控件。</p>
 */
@Data
@Schema(description = "分页结果")
public class PageResult<T> {

    @Schema(description = "当前页数据")
    private List<T> list;

    @Schema(description = "符合条件的总条数")
    private long total;

    @Schema(description = "当前页码，从 1 开始")
    private long page;

    @Schema(description = "每页条数")
    private long size;

    public static <T> PageResult<T> of(List<T> list, long total, long page, long size) {
        PageResult<T> result = new PageResult<>();
        result.setList(list == null ? Collections.emptyList() : list);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        return result;
    }
}
