package com.lingxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingxi.entity.RedeemCode;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 兑换码 Mapper。
 */
public interface RedeemCodeMapper extends BaseMapper<RedeemCode> {

    /**
     * 统计兑换码数量，包含已逻辑删除的记录。
     *
     * <p>code 上有唯一索引且不区分删除状态，生成/创建前必须能感知历史记录，
     * 否则会插入成功前的唯一键冲突。</p>
     */
    @Select("SELECT COUNT(*) FROM redeem_code WHERE code = #{code}")
    long countByCodeIncludeDeleted(@Param("code") String code);
}
