package com.lingxi.service;

import com.lingxi.dto.ArtifactBatchUpdateRequest;
import com.lingxi.dto.ArtifactCreateRequest;
import com.lingxi.dto.ArtifactUpdateRequest;
import com.lingxi.vo.ArtifactVO;
import com.lingxi.vo.PageResult;

import java.util.List;

/**
 * 产物基础服务。
 */
public interface ArtifactService {

    /**
     * 分页查询当前用户的产物，支持类型、关键词、收藏、回收站状态与排序。
     */
    PageResult<ArtifactVO> listMine(int page, int size, String type, String keyword,
                                    Integer trashed, Integer favorite, String sort);

    /**
     * 查询产物详情。
     */
    ArtifactVO get(Long id);

    /**
     * 新建产物。
     */
    ArtifactVO create(ArtifactCreateRequest request);

    /**
     * 更新产物，仅更新传入的字段。
     */
    ArtifactVO update(Long id, ArtifactUpdateRequest request);

    /**
     * 批量更新收藏/回收站标记。
     */
    void batchUpdate(ArtifactBatchUpdateRequest request);

    /**
     * 删除产物（逻辑删除）。
     */
    void delete(Long id);

    /**
     * 批量删除产物（逻辑删除）。
     */
    void deleteBatch(List<Long> ids);
}
