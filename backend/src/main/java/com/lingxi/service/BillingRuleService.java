package com.lingxi.service;

import com.lingxi.dto.BillingRuleCreateRequest;
import com.lingxi.vo.PageResult;
import com.lingxi.dto.BillingRuleUpdateRequest;
import com.lingxi.vo.BillingRuleVO;

import java.util.List;

/**
 * 管理端计费规则服务。
 */
public interface BillingRuleService {

    PageResult<BillingRuleVO> list(int page, int size, String keyword, String category, Integer enabled);

    BillingRuleVO get(Long id);

    BillingRuleVO create(BillingRuleCreateRequest request);

    BillingRuleVO update(Long id, BillingRuleUpdateRequest request);

    void delete(Long id);
}
