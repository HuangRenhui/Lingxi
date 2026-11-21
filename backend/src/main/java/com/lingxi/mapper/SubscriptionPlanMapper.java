package com.lingxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingxi.entity.SubscriptionPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 订阅套餐 Mapper，基础数据操作由 MyBatis-Plus 提供。
 */
@Mapper
public interface SubscriptionPlanMapper extends BaseMapper<SubscriptionPlan> {

    /**
     * 按标识查询，包含已逻辑删除的记录。
     *
     * <p>plan_key 上有唯一索引且不区分删除状态，创建前必须能看到历史记录，
     * 否则删除后重建会直接撞唯一键。</p>
     */
    @Select("SELECT * FROM subscription_plan WHERE plan_key = #{planKey} LIMIT 1")
    SubscriptionPlan selectByPlanKeyIncludeDeleted(@Param("planKey") String planKey);

    /**
     * 恢复一条已逻辑删除的套餐，避免唯一索引冲突。
     */
    @Update("UPDATE subscription_plan SET deleted = 0 WHERE id = #{id}")
    int restoreById(@Param("id") Long id);
}
