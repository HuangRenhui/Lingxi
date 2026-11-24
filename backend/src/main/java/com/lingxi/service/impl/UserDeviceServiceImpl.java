package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.DeviceCreateRequest;
import com.lingxi.dto.DeviceUpdateRequest;
import com.lingxi.entity.UserDevice;
import com.lingxi.mapper.UserDeviceMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.UserDeviceService;
import com.lingxi.vo.DeviceVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录设备服务实现。
 */
@Service
@RequiredArgsConstructor
public class UserDeviceServiceImpl implements UserDeviceService {

    private static final int MAX_PAGE_SIZE = 100;
    /** 单用户设备数量上限，避免无限注册撑爆列表。 */
    private static final int MAX_DEVICE_COUNT = 20;

    private final UserDeviceMapper userDeviceMapper;

    @Override
    public List<DeviceVO> listMine(int page, int size) {
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        LambdaQueryWrapper<UserDevice> wrapper = new LambdaQueryWrapper<UserDevice>()
                .eq(UserDevice::getUserId, SecurityUtils.getUserId())
                .orderByDesc(UserDevice::getCurrentFlag)
                .orderByDesc(UserDevice::getId);
        Page<UserDevice> result = userDeviceMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public DeviceVO get(Long id) {
        return toVO(getOwnedDevice(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceVO create(DeviceCreateRequest request) {
        UserDevice device = new UserDevice();
        device.setUserId(SecurityUtils.getUserId());
        device.setName(request.getName().trim());
        device.setOs(request.getOs());
        device.setBrowser(request.getBrowser());
        device.setIp(request.getIp());
        device.setLocation(request.getLocation());
        // 首个设备默认为当前设备，否则页面的「当前设备」标识永远为空
        Long existing = userDeviceMapper.selectCount(new LambdaQueryWrapper<UserDevice>()
                .eq(UserDevice::getUserId, SecurityUtils.getUserId()));
        if (existing != null && existing >= MAX_DEVICE_COUNT) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                    "设备数量已达上限，请先移除其他设备");
        }
        device.setCurrentFlag(existing == null || existing == 0 ? 1 : 0);
        device.setLastActiveAt(LocalDateTime.now());
        userDeviceMapper.insert(device);
        return toVO(userDeviceMapper.selectById(device.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceVO update(Long id, DeviceUpdateRequest request) {
        UserDevice device = getOwnedDevice(id);
        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
            device.setName(name);
        }
        userDeviceMapper.updateById(device);
        return toVO(userDeviceMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        UserDevice device = getOwnedDevice(id);
        userDeviceMapper.deleteById(id);
        // 删除的是当前设备时，把最近活跃的剩余设备提升为当前设备
        if (Integer.valueOf(1).equals(device.getCurrentFlag())) {
            promoteCurrentDevice(device.getUserId(), List.of(id));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        List<Long> uniqueIds = ids == null ? List.of()
                : ids.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (uniqueIds.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        Long userId = SecurityUtils.getUserId();
        List<UserDevice> devices = userDeviceMapper.selectBatchIds(uniqueIds);
        if (devices.size() != uniqueIds.size()
                || devices.stream().anyMatch(item -> !userId.equals(item.getUserId()))) {
            throw new BusinessException(ResultCode.DEVICE_NOT_OWNED);
        }
        userDeviceMapper.deleteBatchIds(uniqueIds);
        // 批量中包含当前设备时，把最近活跃的剩余设备提升为当前设备
        if (devices.stream().anyMatch(item -> Integer.valueOf(1).equals(item.getCurrentFlag()))) {
            promoteCurrentDevice(userId, uniqueIds);
        }
    }

    /**
     * 从剩余设备中选最近活跃的一台提升为当前设备。
     *
     * @param excludedIds 本次已删除的设备 ID，排除后再挑选
     */
    private void promoteCurrentDevice(Long userId, List<Long> excludedIds) {
        UserDevice next = userDeviceMapper.selectOne(new LambdaQueryWrapper<UserDevice>()
                .eq(UserDevice::getUserId, userId)
                .notIn(UserDevice::getId, excludedIds)
                .orderByDesc(UserDevice::getLastActiveAt)
                .orderByDesc(UserDevice::getId)
                .last("LIMIT 1"));
        if (next != null) {
            userDeviceMapper.update(null, new LambdaUpdateWrapper<UserDevice>()
                    .eq(UserDevice::getId, next.getId())
                    .set(UserDevice::getCurrentFlag, 1));
        }
    }

    /**
     * 查询设备并校验归属。
     */
    private UserDevice getOwnedDevice(Long id) {
        UserDevice device = userDeviceMapper.selectById(id);
        if (device == null) {
            throw new BusinessException(ResultCode.DEVICE_NOT_FOUND);
        }
        if (!SecurityUtils.getUserId().equals(device.getUserId())) {
            throw new BusinessException(ResultCode.DEVICE_NOT_OWNED);
        }
        return device;
    }

    private DeviceVO toVO(UserDevice device) {
        DeviceVO vo = new DeviceVO();
        vo.setId(device.getId());
        vo.setName(device.getName());
        vo.setOs(device.getOs());
        vo.setBrowser(device.getBrowser());
        vo.setIp(device.getIp());
        vo.setLocation(device.getLocation());
        vo.setCurrentFlag(device.getCurrentFlag());
        vo.setLastActiveAt(device.getLastActiveAt());
        vo.setCreatedAt(device.getCreatedAt());
        return vo;
    }
}
