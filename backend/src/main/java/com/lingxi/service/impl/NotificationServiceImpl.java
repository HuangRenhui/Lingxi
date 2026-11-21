package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.NotificationCreateRequest;
import com.lingxi.dto.NotificationUpdateRequest;
import com.lingxi.entity.Notification;
import com.lingxi.mapper.NotificationMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.NotificationService;
import com.lingxi.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知中心服务实现。
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationMapper notificationMapper;

    @Override
    public List<NotificationVO> listMine(int page, int size, boolean unreadOnly) {
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, SecurityUtils.getUserId())
                .eq(unreadOnly, Notification::getReadFlag, 0)
                .orderByDesc(Notification::getId);
        Page<Notification> result = notificationMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public NotificationVO get(Long id) {
        return toVO(getOwnedNotification(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationVO create(NotificationCreateRequest request) {
        Notification notification = new Notification();
        // 接收人只取自登录态：通知接口不允许给他人投递
        notification.setUserId(SecurityUtils.getUserId());
        notification.setTitle(request.getTitle().trim());
        notification.setContent(request.getContent());
        notification.setType(request.getType() == null ? "SYSTEM" : request.getType());
        notification.setReadFlag(0);
        notificationMapper.insert(notification);
        return toVO(notificationMapper.selectById(notification.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationVO update(Long id, NotificationUpdateRequest request) {
        Notification notification = getOwnedNotification(id);
        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            notification.setTitle(title);
        }
        if (request.getContent() != null) {
            notification.setContent(request.getContent());
        }
        if (request.getType() != null) {
            notification.setType(request.getType());
        }
        if (request.getReadFlag() != null) {
            notification.setReadFlag(request.getReadFlag());
        }
        notificationMapper.updateById(notification);
        return toVO(notificationMapper.selectById(id));
    }

    @Override
    public long unreadCount() {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, SecurityUtils.getUserId())
                .eq(Notification::getReadFlag, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long id) {
        Notification notification = getOwnedNotification(id);
        if (Integer.valueOf(1).equals(notification.getReadFlag())) {
            return;
        }
        // 只更新已读标记，避免整实体回写覆盖并发的标题/正文修改
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, notification.getUserId())
                .set(Notification::getReadFlag, 1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead() {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, SecurityUtils.getUserId())
                .eq(Notification::getReadFlag, 0)
                .set(Notification::getReadFlag, 1));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getOwnedNotification(id);
        notificationMapper.deleteById(id);
    }

    /**
     * 查询通知并校验归属。
     */
    private Notification getOwnedNotification(Long id) {
        Notification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException(ResultCode.NOTIFICATION_NOT_FOUND);
        }
        if (!SecurityUtils.getUserId().equals(notification.getUserId())) {
            throw new BusinessException(ResultCode.NOTIFICATION_NOT_OWNED);
        }
        return notification;
    }

    private NotificationVO toVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setType(notification.getType());
        vo.setReadFlag(notification.getReadFlag());
        vo.setCreatedAt(notification.getCreatedAt());
        return vo;
    }
}
