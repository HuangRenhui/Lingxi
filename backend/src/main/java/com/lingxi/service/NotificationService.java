package com.lingxi.service;

import com.lingxi.dto.NotificationCreateRequest;
import com.lingxi.dto.NotificationUpdateRequest;
import com.lingxi.vo.NotificationVO;

import java.util.List;

/**
 * 通知中心服务。
 */
public interface NotificationService {

    /**
     * 分页查询当前用户的通知。
     *
     * @param unreadOnly 仅看未读
     */
    List<NotificationVO> listMine(int page, int size, boolean unreadOnly);

    /**
     * 查询通知详情，仅限本人。
     */
    NotificationVO get(Long id);

    /**
     * 给当前用户新增一条通知。
     */
    NotificationVO create(NotificationCreateRequest request);

    /**
     * 更新通知，仅限本人。
     */
    NotificationVO update(Long id, NotificationUpdateRequest request);

    /**
     * 未读数量，用于角标展示。
     */
    long unreadCount();

    /**
     * 标记单条通知已读。
     */
    void markRead(Long id);

    /**
     * 将当前用户全部未读通知标记为已读。
     */
    void markAllRead();

    /**
     * 删除通知（逻辑删除）。
     */
    void delete(Long id);
}
