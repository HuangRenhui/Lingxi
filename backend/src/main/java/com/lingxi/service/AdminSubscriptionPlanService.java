package com.lingxi.service;

import com.lingxi.dto.SubscriptionPlanCreateRequest;
import com.lingxi.dto.SubscriptionPlanUpdateRequest;
import com.lingxi.vo.SubscriptionPlanVO;

import java.util.List;

/**
 * 管理端订阅套餐服务，包含已下架套餐。
 */
public interface AdminSubscriptionPlanService {

    List<SubscriptionPlanVO> list(int page, int size, String keyword);

    SubscriptionPlanVO get(Long id);

    SubscriptionPlanVO create(SubscriptionPlanCreateRequest request);

    SubscriptionPlanVO update(Long id, SubscriptionPlanUpdateRequest request);

    void delete(Long id);
}
