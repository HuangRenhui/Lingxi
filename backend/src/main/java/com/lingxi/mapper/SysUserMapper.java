package com.lingxi.mapper;

import com.lingxi.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统用户 Mapper。
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 统计用户名占用情况（包含已逻辑删除的记录）。
     *
     * <p>唯一索引覆盖已删除行，若只按 deleted=0 查重，删除后重建同名用户会触发
     * DuplicateKeyException 变成 500。</p>
     *
     * <p>注意：这是裸 SQL，不经过逻辑删除拦截器，行为依赖数据库排序规则。
     * 当前库为 utf8mb4_general_ci（大小写不敏感），因此与唯一索引语义一致；
     * 若迁移到大小写敏感的排序规则（_bin / _as_cs），查重与唯一索引会同时变为区分大小写。</p>
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username}")
    int countByUsernameIncludeDeleted(@Param("username") String username);
}
