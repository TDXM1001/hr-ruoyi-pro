package com.ruoyi.asset.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.asset.domain.AssetDisposal;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.bo.AssetDisposalBo;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetDisposalMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.asset.service.AssetStatusMachine;
import com.ruoyi.common.exception.ServiceException;

/**
 * 资产处置服务实现测试。
 *
 * @author Codex
 */
@ExtendWith(MockitoExtension.class)
class AssetDisposalServiceImplTest
{
    @Mock
    private AssetDisposalMapper assetDisposalMapper;

    @Mock
    private AssetLedgerMapper assetLedgerMapper;

    @Mock
    private AssetChangeLogMapper assetChangeLogMapper;

    @Mock
    private AssetStatusMachine assetStatusMachine;

    @InjectMocks
    private AssetDisposalServiceImpl disposalService;

    @Test
    @DisplayName("非待处置资产不允许确认处置")
    void shouldRejectDisposalWhenAssetNotPendingDisposal()
    {
        AssetLedger ledger = new AssetLedger();
        ledger.setAssetId(1L);
        ledger.setAssetStatus(AssetStatus.IN_USE.getCode());
        when(assetLedgerMapper.selectAssetById(1L)).thenReturn(ledger);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> disposalService.confirmDisposal(buildBo(), "finance"));

        assertEquals("只有待处置资产才能确认处置", exception.getMessage());
        verify(assetDisposalMapper, never()).insertAssetDisposal(any(AssetDisposal.class));
        verify(assetLedgerMapper, never()).updateStatus(any(Long.class), any(String.class));
        verify(assetChangeLogMapper, never()).insertAssetChangeLog(any());
    }

    @Test
    @DisplayName("待处置资产确认后应写入处置单并回写已处置状态")
    void shouldConfirmDisposalAndMoveAssetToDisposed()
    {
        AssetLedger ledger = new AssetLedger();
        ledger.setAssetId(1L);
        ledger.setAssetStatus(AssetStatus.PENDING_DISPOSAL.getCode());
        when(assetLedgerMapper.selectAssetById(1L)).thenReturn(ledger);
        when(assetStatusMachine.canTransit(AssetStatus.PENDING_DISPOSAL, AssetStatus.DISPOSED)).thenReturn(true);
        when(assetDisposalMapper.selectMaxDisposalNoByPrefix(any(String.class))).thenReturn("DP-2026-0009");
        when(assetDisposalMapper.insertAssetDisposal(any(AssetDisposal.class))).thenAnswer(invocation ->
        {
            AssetDisposal disposal = invocation.getArgument(0);
            disposal.setDisposalId(11L);
            return 1;
        });
        when(assetLedgerMapper.updateStatus(1L, AssetStatus.DISPOSED.getCode())).thenReturn(1);

        Long disposalId = disposalService.confirmDisposal(buildBo(), "finance");

        assertEquals(11L, disposalId);
        verify(assetLedgerMapper).updateStatus(1L, AssetStatus.DISPOSED.getCode());
        verify(assetChangeLogMapper).insertAssetChangeLog(any());
    }

    private AssetDisposalBo buildBo()
    {
        AssetDisposalBo bo = new AssetDisposalBo();
        bo.setAssetId(1L);
        bo.setDisposalType("SCRAP");
        bo.setDisposalReason("盘点毁损");
        bo.setDisposalDate(new Date());
        bo.setFinanceConfirmFlag("1");
        bo.setRemark("测试确认处置");
        return bo;
    }
}
