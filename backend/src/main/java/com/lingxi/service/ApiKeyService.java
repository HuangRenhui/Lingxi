package com.lingxi.service;

import com.lingxi.dto.ApiKeyCreateRequest;
import com.lingxi.dto.ApiKeyUpdateRequest;
import com.lingxi.vo.ApiKeyVO;

import java.util.List;

/**
 * API Key 基础服务。
 */
public interface ApiKeyService {

    /**
     * 分页查询当前用户的密钥。
     */
    List<ApiKeyVO> listMine(int page, int size);

    /**
     * 查询密钥详情，仅返回前缀，不含明文。
     */
    ApiKeyVO get(Long id);

    /**
     * 创建密钥，返回的明文只出现这一次。
     */
    ApiKeyVO create(ApiKeyCreateRequest request);

    /**
     * 更新密钥名称或启停状态。
     */
    ApiKeyVO update(Long id, ApiKeyUpdateRequest request);

    /**
     * 删除密钥（逻辑删除）。
     */
    void delete(Long id);
}
