package com.lingxi.service;

import com.lingxi.dto.ArtifactCreateRequest;
import com.lingxi.dto.ArtifactUpdateRequest;
import com.lingxi.vo.ArtifactVO;

import java.util.List;

/**
 * 产物基础服务。
 */
public interface ArtifactService {

    /**
     * 分页查询当前用户的产物，支持按类型、关键词与回收站状态筛选。
     */
    List<ArtifactVO> listMine(int page, int size, String type, String keyword, Integer trashed);

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
     * 删除产物（逻辑删除）。
     */
    void delete(Long id);
}
