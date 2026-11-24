package com.lingxi.service;

import com.lingxi.dto.ConnectorCreateRequest;
import com.lingxi.vo.PageResult;
import com.lingxi.dto.ConnectorUpdateRequest;
import com.lingxi.vo.ConnectorVO;

import java.util.List;

/**
 * 连接器基础服务。
 */
public interface ConnectorService {

    /**
     * 分页查询当前用户的连接器。
     */
    PageResult<ConnectorVO> listMine(int page, int size);

    /**
     * 查询连接器详情。
     */
    ConnectorVO get(Long id);

    /**
     * 新建连接器。
     */
    ConnectorVO create(ConnectorCreateRequest request);

    /**
     * 更新连接器，仅更新传入的字段。
     */
    ConnectorVO update(Long id, ConnectorUpdateRequest request);

    /**
     * 删除连接器（逻辑删除）。
     */
    void delete(Long id);
}
