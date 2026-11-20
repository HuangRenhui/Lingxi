package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.ResultCode;
import com.lingxi.dto.AdminUserCreateRequest;
import com.lingxi.dto.AdminUserUpdateRequest;
import com.lingxi.entity.SysUser;
import com.lingxi.mapper.SysUserMapper;
import com.lingxi.security.LoginUser;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.AdminUserService;
import com.lingxi.vo.AdminUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理端用户服务实现。
 */
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private static final int MAX_PAGE_SIZE = 100;

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<AdminUserVO> list(int page, int size, String keyword) {
        requireAdmin();
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .and(StringUtils.hasText(keyword), item -> item
                        .like(SysUser::getUsername, keyword)
                        .or()
                        .like(SysUser::getNickname, keyword))
                .orderByDesc(SysUser::getId);
        Page<SysUser> result = sysUserMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public AdminUserVO get(Long id) {
        requireAdmin();
        return toVO(getUser(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUserVO create(AdminUserCreateRequest request) {
        requireAdmin();
        String username = request.getUsername().trim();
        // 唯一索引覆盖已删除行：查重必须包含逻辑删除记录，否则删除后重建同名用户会撞唯一键
        if (sysUserMapper.countByUsernameIncludeDeleted(username) > 0) {
            throw new BusinessException(ResultCode.USERNAME_TAKEN);
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : username);
        user.setRole(StringUtils.hasText(request.getRole()) ? request.getRole() : "USER");
        user.setStatus(1);
        sysUserMapper.insert(user);
        // 插入后回查：记录被并发删除时返回明确的业务错误，而不是空指针
        SysUser saved = sysUserMapper.selectById(user.getId());
        if (saved == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return toVO(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminUserVO update(Long id, AdminUserUpdateRequest request) {
        requireAdmin();
        SysUser user = getUser(id);
        LoginUser current = SecurityUtils.getLoginUser();

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname().trim());
        }
        if (request.getRole() != null) {
            // 防止管理员把自己降级后失去管理入口
            if (id.equals(current.getId()) && !"ADMIN".equals(request.getRole())) {
                throw new BusinessException(ResultCode.SELF_OPERATION_FORBIDDEN);
            }
            user.setRole(request.getRole());
        }
        if (request.getStatus() != null) {
            if (id.equals(current.getId()) && request.getStatus() == 0) {
                throw new BusinessException(ResultCode.SELF_OPERATION_FORBIDDEN);
            }
            user.setStatus(request.getStatus());
        }

        sysUserMapper.updateById(user);
        return toVO(sysUserMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        requireAdmin();
        if (id.equals(SecurityUtils.getUserId())) {
            throw new BusinessException(ResultCode.CANNOT_DELETE_SELF);
        }
        getUser(id);
        sysUserMapper.deleteById(id);
    }

    /**
     * 仅管理员可访问管理端用户接口。
     */
    private void requireAdmin() {
        if (!"ADMIN".equals(SecurityUtils.getLoginUser().getRole())) {
            throw new BusinessException(ResultCode.ADMIN_REQUIRED);
        }
    }

    private SysUser getUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    private AdminUserVO toVO(SysUser user) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }
}
