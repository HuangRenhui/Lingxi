package com.lingxi.service;

import com.lingxi.dto.ApplicationCreateRequest;
import com.lingxi.dto.ApplicationUpdateRequest;
import com.lingxi.vo.ApplicationVO;

import java.util.List;

/**
 * AI 应用基础服务。
 *
 * <p>仅提供当前用户维度的简单数据管理，权限、配额等复杂能力后续再扩展。</p>
 */
public interface ApplicationService {

    /**
     * 分页查询当前用户的应用。
     *
     * @param page 页码，从 1 开始
     * @param size 每页条数，最大 100
     * @return 当前页应用列表
     */
    List<ApplicationVO> listMine(int page, int size);

    /**
     * 查询应用详情。
     *
     * @param id 应用 ID
     * @return 应用详情
     */
    ApplicationVO get(Long id);

    /**
     * 新建应用。
     *
     * @param request 新建请求
     * @return 创建后的应用
     */
    ApplicationVO create(ApplicationCreateRequest request);

    /**
     * 更新应用，仅更新传入的字段。
     *
     * @param id      应用 ID
     * @param request 更新请求
     * @return 更新后的应用
     */
    ApplicationVO update(Long id, ApplicationUpdateRequest request);

    /**
     * 删除应用（逻辑删除）。
     *
     * @param id 应用 ID
     */
    void delete(Long id);
}
