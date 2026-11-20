package com.lingxi.service;

import com.lingxi.entity.WalletAccount;
import com.lingxi.vo.WalletAccountVO;
import com.lingxi.vo.WalletTransactionVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包服务。
 *
 * <p>钱包数据为只读视图：余额变动由订单支付、兑换码与退款等业务驱动，
 * 流水只追加不修改，因此不提供直接改余额的接口。</p>
 */
public interface WalletService {

    /**
     * 查询当前用户的钱包账户，未开户时返回零值账户。
     */
    WalletAccountVO getAccount();

    /**
     * 分页查询当前用户的钱包流水，支持按类型筛选。
     */
    List<WalletTransactionVO> listTransactions(int page, int size, String type);

    /**
     * 确保用户钱包存在（不存在则以零余额开户），返回账户实体。
     * 开户与唯一索引配合，重复调用安全。
     */
    WalletAccount ensureAccount(Long userId);

    /**
     * 锁定用户钱包行，作为用户级互斥锁使用，必须在事务内调用。
     */
    WalletAccount lockAccount(Long userId);

    /**
     * 扣减余额并写流水。
     *
     * @param bizNo 业务幂等键；同一 bizNo 重复调用只入账一次
     * @throws BusinessException 余额不足（INSUFFICIENT_BALANCE）或账户不存在
     */
    WalletAccount debit(Long userId, BigDecimal amount, String type, String note, String bizNo);

    /**
     * 增加余额并写流水。{@code type} 为 RECHARGE 时同时累加「累计充值」。
     *
     * @param bizNo 业务幂等键；同一 bizNo 重复调用只入账一次
     */
    WalletAccount credit(Long userId, BigDecimal amount, String type, String note, String bizNo);
}
