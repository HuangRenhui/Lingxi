package com.lingxi.service;

import com.lingxi.dto.NotificationCreateRequest;
import com.lingxi.dto.NotificationUpdateRequest;
import com.lingxi.vo.NotificationVO;
import com.lingxi.vo.PageResult;

import java.util.List;

/**
 * 通知中心服务。
 */
public interface NotificationService {

    /**
     * 分页查询当前用户的通知。
     *
     * @param unreadOnly 仅看未读
     * @param readOnly   仅看已读（用于批量清除已读）
     * @param type       通知类型，可为空
     * @param keyword    标题或正文关键词，可为空
     */
    PageResult<NotificationVO> listMine(int page, int size, boolean unreadOnly, boolean readOnly,
                                        String type, String keyword);

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

    /**
     * 批量删除通知（逻辑删除），只处理当前用户自己的通知。
     */
    void deleteBatch(List<Long> ids);
}
