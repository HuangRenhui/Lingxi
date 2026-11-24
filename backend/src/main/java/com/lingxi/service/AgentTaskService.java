package com.lingxi.service;

import com.lingxi.dto.TaskCreateRequest;
import com.lingxi.vo.PageResult;
import com.lingxi.dto.TaskUpdateRequest;
import com.lingxi.vo.TaskVO;

import java.util.List;

/**
 * 定时任务服务（B7）。
 */
public interface AgentTaskService {

    /**
     * 分页查询当前用户的任务列表；page 从 1 开始，size 上限 100。
     */
    PageResult<TaskVO> listMine(int page, int size);

    /**
     * 查询任务详情，仅限本人。
     */
    TaskVO get(Long id);

    TaskVO create(TaskCreateRequest request);

    TaskVO update(Long id, TaskUpdateRequest request);

    void delete(Long id);

    TaskVO updateStatus(Long id, String status);
}
