package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
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
        notification.setReadFlag(1);
        notificationMapper.updateById(notification);
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
