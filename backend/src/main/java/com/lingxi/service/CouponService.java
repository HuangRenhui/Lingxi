package com.lingxi.service;

import com.lingxi.dto.CouponCreateRequest;
import com.lingxi.vo.PageResult;
import com.lingxi.dto.CouponUpdateRequest;
import com.lingxi.vo.CouponVO;

import java.util.List;

/**
 * 管理端优惠券服务。
 */
public interface CouponService {

    PageResult<CouponVO> list(int page, int size, String keyword, Integer enabled);

    CouponVO get(Long id);

    CouponVO create(CouponCreateRequest request);

    CouponVO update(Long id, CouponUpdateRequest request);

    void delete(Long id);
}
