package com.lingxi.controller;

import com.lingxi.common.Result;
import com.lingxi.service.WalletService;
import com.lingxi.vo.WalletAccountVO;
import com.lingxi.vo.WalletTransactionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 钱包资产接口。
 *
 * <p>仅提供账户与流水的只读查询，余额变动必须由后端业务在事务内驱动。</p>
 */
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
@Tag(name = "17. 钱包资产")
public class WalletController {

    private final WalletService walletService;

    /**
     * 查询当前用户的钱包账户。
     */
    @GetMapping
    @Operation(summary = "我的钱包账户")
    public Result<WalletAccountVO> getAccount() {
        return Result.success(walletService.getAccount());
    }

    /**
     * 分页查询当前用户的钱包流水。
     */
    @GetMapping("/transactions")
    @Operation(summary = "我的钱包流水")
    public Result<List<WalletTransactionVO>> listTransactions(@RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "20") int size,
                                                              @RequestParam(required = false) String type) {
        return Result.success(walletService.listTransactions(page, size, type));
    }
}
