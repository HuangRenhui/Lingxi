package com.lingxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingxi.entity.WalletAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 钱包账户 Mapper。
 *
 * <p>余额变更必须走这里的原子 SQL：先查后改的写法在并发下会丢失更新，
 * 两个请求同时读到 100、各自扣 60 后写回 40，实际只扣了一次。</p>
 */
@Mapper
public interface WalletAccountMapper extends BaseMapper<WalletAccount> {

    /**
     * 原子扣减余额：余额不足时 {@code balance >= #{amount}} 不成立，返回 0 行。
     * 业务层必须判断返回值，0 行即代表扣费失败。
     */
    @Update("UPDATE wallet_account SET balance = balance - #{amount}, "
            + "total_spent = total_spent + #{amount} "
            + "WHERE user_id = #{userId} AND deleted = 0 AND balance >= #{amount}")
    int deduct(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    /**
     * 原子增加余额；{@code recharge} 为本次计入「累计充值」的金额，
     * 退款与奖励传 0，充值传本次金额。
     */
    @Update("UPDATE wallet_account SET balance = balance + #{amount}, "
            + "total_recharge = total_recharge + #{recharge} "
            + "WHERE user_id = #{userId} AND deleted = 0")
    int credit(@Param("userId") Long userId,
               @Param("amount") BigDecimal amount,
               @Param("recharge") BigDecimal recharge);

    /** 按用户查询账户（绕过逻辑删除拦截器的自动条件，需手写 deleted = 0）。 */
    @Select("SELECT * FROM wallet_account WHERE user_id = #{userId} AND deleted = 0 LIMIT 1")
    WalletAccount selectByUserId(@Param("userId") Long userId);

    /**
     * 加行锁读取账户，用作「用户级互斥锁」：同一用户的开通/扣费串行化，
     * 避免并发请求各自读到旧余额。必须在事务内调用。
     */
    @Select("SELECT * FROM wallet_account WHERE user_id = #{userId} AND deleted = 0 LIMIT 1 FOR UPDATE")
    WalletAccount selectForUpdate(@Param("userId") Long userId);
}
