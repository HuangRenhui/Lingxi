package com.lingxi.service;

import com.lingxi.dto.SubscriptionCreateRequest;
import com.lingxi.dto.SubscriptionUpdateRequest;
import com.lingxi.vo.SubscriptionPlanVO;
import com.lingxi.vo.UserSubscriptionVO;

import java.util.List;

/**
 * 订阅服务。
 */
public interface SubscriptionService {

    /**
     * 查询已上架的套餐目录。
     */
    List<SubscriptionPlanVO> listPlans();

    /**
     * 查询当前生效的订阅，未订阅或已到期返回 null。
     */
    UserSubscriptionVO getCurrent();

    /**
     * 开通、续费或变更套餐；当前订阅到期后失效，新订阅立即生效。
     */
    UserSubscriptionVO subscribe(SubscriptionCreateRequest request);

    /**
     * 更新当前订阅（自动续费开关）。
     */
    UserSubscriptionVO updateCurrent(SubscriptionUpdateRequest request);

    /**
     * 取消当前订阅。
     */
    void cancelCurrent();

    /**
     * 分页查询订阅历史。
     */
    List<UserSubscriptionVO> listHistory(int page, int size);
}
