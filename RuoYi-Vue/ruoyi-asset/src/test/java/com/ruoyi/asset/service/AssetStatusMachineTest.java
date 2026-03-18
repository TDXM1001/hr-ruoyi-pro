package com.ruoyi.asset.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.ruoyi.asset.enums.AssetStatus;

/**
 * 资产状态机测试。
 * <p>
 * 该测试优先固定第一期固定资产闭环中的核心状态流转规则，
 * 避免后续台账、交接、盘点、处置实现时出现状态口径漂移。
 * </p>
 *
 * @author Codex
 */
class AssetStatusMachineTest
{
    private final AssetStatusMachine machine = new AssetStatusMachine();

    @Test
    @DisplayName("在册资产应该允许进入使用中与闲置中")
    void shouldAllowLedgerToUseAndIdleFlow()
    {
        assertTrue(machine.canTransit(AssetStatus.IN_LEDGER, AssetStatus.IN_USE));
        assertTrue(machine.canTransit(AssetStatus.IN_LEDGER, AssetStatus.IDLE));
    }

    @Test
    @DisplayName("在用资产应该允许进入盘点中与待处置")
    void shouldAllowUseToInventoryAndPendingDisposal()
    {
        assertTrue(machine.canTransit(AssetStatus.IN_USE, AssetStatus.INVENTORYING));
        assertTrue(machine.canTransit(AssetStatus.IN_USE, AssetStatus.PENDING_DISPOSAL));
    }

    @Test
    @DisplayName("盘点中的资产应该允许回到在用或闲置")
    void shouldAllowInventoryBackToUseOrIdle()
    {
        assertTrue(machine.canTransit(AssetStatus.INVENTORYING, AssetStatus.IN_USE));
        assertTrue(machine.canTransit(AssetStatus.INVENTORYING, AssetStatus.IDLE));
    }

    @Test
    @DisplayName("已处置资产不应该回退到在用")
    void shouldRejectDisposedBackToUse()
    {
        assertFalse(machine.canTransit(AssetStatus.DISPOSED, AssetStatus.IN_USE));
    }
}
