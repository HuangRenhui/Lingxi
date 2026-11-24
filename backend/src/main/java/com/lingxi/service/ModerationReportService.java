package com.lingxi.service;

import com.lingxi.dto.ModerationReportCreateRequest;
import com.lingxi.vo.PageResult;
import com.lingxi.dto.ModerationReportUpdateRequest;
import com.lingxi.vo.ModerationReportVO;

import java.util.List;

/**
 * 管理端内容举报服务。
 */
public interface ModerationReportService {

    PageResult<ModerationReportVO> list(int page, int size, String status, String keyword);

    ModerationReportVO get(Long id);

    ModerationReportVO create(ModerationReportCreateRequest request);

    ModerationReportVO update(Long id, ModerationReportUpdateRequest request);

    void delete(Long id);
}
