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
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE username = #{username}")
    int countByUsernameIncludeDeleted(@Param("username") String username);
}
