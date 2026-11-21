package com.lingxi.service;

import com.lingxi.dto.SensitiveWordBatchCreateRequest;
import com.lingxi.dto.SensitiveWordUpdateRequest;
import com.lingxi.vo.SensitiveWordVO;

import java.util.List;

/**
 * 敏感词管理服务。
 */
public interface SensitiveWordService {

    List<SensitiveWordVO> list(int page, int size, String keyword, String category);

    List<SensitiveWordVO> batchCreate(SensitiveWordBatchCreateRequest request);

    SensitiveWordVO update(Long id, SensitiveWordUpdateRequest request);

    void delete(Long id);

    /**
     * 校验文本是否命中启用中的敏感词。
     *
     * <p>命中时抛出 {@code CONTENT_SENSITIVE} 业务异常并累加该词的命中次数；
     * 传入 null/空文本会被忽略，便于调用方直接传入可选字段。</p>
     *
     * @param texts 待校验文本，可传多个
     */
    void checkContent(String... texts);
}
