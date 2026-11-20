package com.lingxi.service;

import com.lingxi.dto.AdminUserCreateRequest;
import com.lingxi.dto.AdminUserUpdateRequest;
import com.lingxi.vo.AdminUserVO;

import java.util.List;

/**
 * 管理端用户服务，仅管理员可调用。
 */
public interface AdminUserService {

    /**
     * 分页查询用户，支持用户名/昵称模糊搜索。
     */
    List<AdminUserVO> list(int page, int size, String keyword);

    /**
     * 用户详情。
     */
    AdminUserVO get(Long id);

    /**
     * 新建用户。
     */
    AdminUserVO create(AdminUserCreateRequest request);

    /**
     * 更新用户资料、角色、状态或重置密码。
     */
    AdminUserVO update(Long id, AdminUserUpdateRequest request);

    /**
     * 删除用户（逻辑删除）。
     */
    void delete(Long id);
}
