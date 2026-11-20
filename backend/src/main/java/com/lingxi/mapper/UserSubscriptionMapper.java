package com.lingxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingxi.entity.UserSubscription;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户订阅 Mapper，基础数据操作由 MyBatis-Plus 提供。
 */
@Mapper
public interface UserSubscriptionMapper extends BaseMapper<UserSubscription> {
}
