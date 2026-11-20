package com.lingxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingxi.entity.WalletTransaction;
import org.apache.ibatis.annotations.Mapper;

/**
 * 钱包流水 Mapper，基础数据操作由 MyBatis-Plus 提供。
 */
@Mapper
public interface WalletTransactionMapper extends BaseMapper<WalletTransaction> {
}
