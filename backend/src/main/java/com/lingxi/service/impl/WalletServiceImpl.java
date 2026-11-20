package com.lingxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lingxi.common.BusinessException;
import com.lingxi.common.MoneyUtil;
import com.lingxi.common.ResultCode;
import com.lingxi.entity.WalletAccount;
import com.lingxi.entity.WalletTransaction;
import com.lingxi.mapper.WalletAccountMapper;
import com.lingxi.mapper.WalletTransactionMapper;
import com.lingxi.security.SecurityUtils;
import com.lingxi.service.WalletService;
import com.lingxi.vo.WalletAccountVO;
import com.lingxi.vo.WalletTransactionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 钱包服务实现。
 */
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    /** 单页最大条数，与全局分页上限保持一致 */
    private static final int MAX_PAGE_SIZE = 100;

    /** 流水类型全集，用于筛选参数校验 */
    private static final Set<String> TYPES = new HashSet<>(Arrays.asList(
            "RECHARGE", "CONSUME", "REWARD", "REFUND", "FREEZE", "UNFREEZE"));

    private final WalletAccountMapper walletAccountMapper;
    private final WalletTransactionMapper walletTransactionMapper;

    @Override
    public WalletAccountVO getAccount() {
        WalletAccount account = walletAccountMapper.selectOne(new LambdaQueryWrapper<WalletAccount>()
                .eq(WalletAccount::getUserId, SecurityUtils.getUserId())
                .last("LIMIT 1"));
        WalletAccountVO vo = new WalletAccountVO();
        // 尚未开户的用户返回零值账户，前端无需区分空态
        if (account == null) {
            vo.setBalance(BigDecimal.ZERO);
            vo.setFrozen(BigDecimal.ZERO);
            vo.setTotalRecharge(BigDecimal.ZERO);
            vo.setTotalSpent(BigDecimal.ZERO);
            return vo;
        }
        vo.setBalance(account.getBalance());
        vo.setFrozen(account.getFrozen());
        vo.setTotalRecharge(account.getTotalRecharge());
        vo.setTotalSpent(account.getTotalSpent());
        return vo;
    }

    @Override
    public List<WalletTransactionVO> listTransactions(int page, int size, String type) {
        long current = Math.max(1, page);
        long pageSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);

        String typeFilter = null;
        if (StringUtils.hasText(type)) {
            typeFilter = type.trim().toUpperCase(Locale.ROOT);
            if (!TYPES.contains(typeFilter)) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }
        }

        LambdaQueryWrapper<WalletTransaction> wrapper = new LambdaQueryWrapper<WalletTransaction>()
                .eq(WalletTransaction::getUserId, SecurityUtils.getUserId())
                .eq(typeFilter != null, WalletTransaction::getType, typeFilter)
                .orderByDesc(WalletTransaction::getId);
        Page<WalletTransaction> result = walletTransactionMapper.selectPage(new Page<>(current, pageSize), wrapper);
        return result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public WalletAccount ensureAccount(Long userId) {
        WalletAccount account = walletAccountMapper.selectByUserId(userId);
        if (account != null) {
            return account;
        }
        WalletAccount created = new WalletAccount();
        created.setUserId(userId);
        created.setBalance(BigDecimal.ZERO);
        created.setFrozen(BigDecimal.ZERO);
        created.setTotalRecharge(BigDecimal.ZERO);
        created.setTotalSpent(BigDecimal.ZERO);
        try {
            walletAccountMapper.insert(created);
        } catch (DuplicateKeyException ex) {
            // 并发开户：唯一索引兜底，重新读取已存在的账户即可
            return walletAccountMapper.selectByUserId(userId);
        }
        return walletAccountMapper.selectByUserId(userId);
    }

    @Override
    public WalletAccount lockAccount(Long userId) {
        WalletAccount account = walletAccountMapper.selectForUpdate(userId);
        if (account == null) {
            throw new BusinessException(ResultCode.WALLET_NOT_FOUND);
        }
        return account;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletAccount debit(Long userId, BigDecimal amount, String type, String note, String bizNo) {
        BigDecimal normalized = normalizeAmount(amount);
        if (normalized.compareTo(BigDecimal.ZERO) == 0) {
            return ensureAccount(userId);
        }
        // 幂等：同一业务键已记账则直接返回，避免重试导致重复扣减
        if (StringUtils.hasText(bizNo) && transactionExists(userId, bizNo)) {
            return walletAccountMapper.selectByUserId(userId);
        }
        int affected = walletAccountMapper.deduct(userId, normalized);
        if (affected == 0) {
            // 影响 0 行只可能是余额不足或账户不存在，统一按余额不足返回更贴近用户视角
            if (walletAccountMapper.selectByUserId(userId) == null) {
                throw new BusinessException(ResultCode.WALLET_NOT_FOUND);
            }
            throw new BusinessException(ResultCode.INSUFFICIENT_BALANCE);
        }
        WalletAccount account = walletAccountMapper.selectByUserId(userId);
        appendTransaction(userId, type, normalized.negate(), account.getBalance(), note, bizNo);
        return account;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletAccount credit(Long userId, BigDecimal amount, String type, String note, String bizNo) {
        BigDecimal normalized = normalizeAmount(amount);
        if (normalized.compareTo(BigDecimal.ZERO) == 0) {
            return ensureAccount(userId);
        }
        if (StringUtils.hasText(bizNo) && transactionExists(userId, bizNo)) {
            return walletAccountMapper.selectByUserId(userId);
        }
        BigDecimal recharge = "RECHARGE".equals(type) ? normalized : BigDecimal.ZERO;
        int affected = walletAccountMapper.credit(userId, normalized, recharge);
        if (affected == 0) {
            throw new BusinessException(ResultCode.WALLET_NOT_FOUND);
        }
        WalletAccount account = walletAccountMapper.selectByUserId(userId);
        appendTransaction(userId, type, normalized, account.getBalance(), note, bizNo);
        return account;
    }

    /**
     * 金额规整为 6 位小数（与 DECIMAL(18,6) 对齐），负数与空值在此拦截。
     */
    private BigDecimal normalizeAmount(BigDecimal amount) {
        return MoneyUtil.normalize(amount);
    }

    /**
     * 幂等键是否已存在流水记录。
     */
    private boolean transactionExists(Long userId, String bizNo) {
        Long count = walletTransactionMapper.selectCount(new LambdaQueryWrapper<WalletTransaction>()
                .eq(WalletTransaction::getUserId, userId)
                .eq(WalletTransaction::getBizNo, bizNo));
        return count != null && count > 0;
    }

    /**
     * 追加流水。bizNo 上有唯一索引，重复写入会抛 DuplicateKeyException 并回滚整个事务，
     * 因此调用前必须已通过 {@link #transactionExists} 判断。
     */
    private void appendTransaction(Long userId, String type, BigDecimal amount,
                                   BigDecimal balance, String note, String bizNo) {
        WalletTransaction transaction = new WalletTransaction();
        transaction.setUserId(userId);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalance(balance);
        transaction.setNote(note);
        transaction.setBizNo(bizNo);
        walletTransactionMapper.insert(transaction);
    }

    /**
     * 实体转视图。
     */
    private WalletTransactionVO toVO(WalletTransaction transaction) {
        WalletTransactionVO vo = new WalletTransactionVO();
        vo.setId(transaction.getId());
        vo.setType(transaction.getType());
        vo.setAmount(transaction.getAmount());
        vo.setBalance(transaction.getBalance());
        vo.setNote(transaction.getNote());
        vo.setCreatedAt(transaction.getCreatedAt());
        return vo;
    }
}
