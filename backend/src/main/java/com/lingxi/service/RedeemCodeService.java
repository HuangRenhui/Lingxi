package com.lingxi.service;

import com.lingxi.dto.RedeemCodeBatchRequest;
import com.lingxi.vo.PageResult;
import com.lingxi.dto.RedeemCodeCreateRequest;
import com.lingxi.dto.RedeemCodeUpdateRequest;
import com.lingxi.vo.RedeemCodeVO;

import java.util.List;

/**
 * 管理端兑换码服务。
 */
public interface RedeemCodeService {

    PageResult<RedeemCodeVO> list(int page, int size, String status, String keyword);

    RedeemCodeVO get(Long id);

    RedeemCodeVO create(RedeemCodeCreateRequest request);

    List<RedeemCodeVO> batchCreate(RedeemCodeBatchRequest request);

    RedeemCodeVO update(Long id, RedeemCodeUpdateRequest request);

    void delete(Long id);
}
