package com.lingxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingxi.entity.BizOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单 Mapper，基础数据操作由 MyBatis-Plus 提供。
 */
@Mapper
public interface BizOrderMapper extends BaseMapper<BizOrder> {
}
