package com.lingxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingxi.entity.BillingRule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 计费规则 Mapper。
 */
public interface BillingRuleMapper extends BaseMapper<BillingRule> {

    /**
     * 按「目标 + 模型」查询，包含已逻辑删除的记录。
     *
     * <p>唯一索引为 (target, model, deleted)，删除后重建同键规则会撞索引；
     * 创建前先看历史记录，存在则复用该行。</p>
     */
    @Select("SELECT * FROM billing_rule WHERE target = #{target} AND model = #{model} LIMIT 1")
    BillingRule selectByTargetAndModelIncludeDeleted(@Param("target") String target,
                                                     @Param("model") String model);

    /**
     * 恢复一条已逻辑删除的计费规则。
     */
    @Update("UPDATE billing_rule SET deleted = 0 WHERE id = #{id}")
    int restoreById(@Param("id") Long id);
}
