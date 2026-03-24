package com.ruoyi.asset.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Date;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.asset.domain.AssetApprovalRecord;
import com.ruoyi.asset.domain.AssetDisposal;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.bo.AssetApprovalActionBo;
import com.ruoyi.asset.domain.bo.AssetDisposalBo;
import com.ruoyi.asset.domain.vo.AssetDisposalVo;
import com.ruoyi.asset.enums.AssetApprovalStatus;
import com.ruoyi.asset.enums.AssetApprovalType;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.asset.mapper.AssetApprovalMapper;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetDisposalMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.asset.service.AssetApprovalStatusMatrix;
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

    @Mock
    private AssetApprovalMapper assetApprovalMapper;

    @Mock
    private AssetApprovalStatusMatrix assetApprovalStatusMatrix;

    @InjectMocks
    private AssetDisposalServiceImpl disposalService;

    @Test
    @DisplayName("非待处置资产不允许提交处置审批")
    void shouldRejectDisposalWhenAssetNotPendingDisposal()
    {
        AssetLedger ledger = new AssetLedger();
        ledger.setAssetId(1L);
        ledger.setAssetStatus(AssetStatus.IN_USE.getCode());
        when(assetLedgerMapper.selectAssetById(1L)).thenReturn(ledger);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> disposalService.submitDisposalApproval(buildBo(), "finance"));

        assertEquals("只有待处置资产才能提交处置审批", exception.getMessage());
        verify(assetDisposalMapper, never()).insertAssetDisposal(any(AssetDisposal.class));
        verify(assetLedgerMapper, never()).updateStatus(any(Long.class), any(String.class));
        verify(assetApprovalMapper, never()).insertAssetApprovalRecord(any(AssetApprovalRecord.class));
        verify(assetChangeLogMapper, never()).insertAssetChangeLog(any());
    }

    @Test
    @DisplayName("待处置资产提交审批后应写入处置单和提交轨迹但不回写终态")
    void shouldSubmitDisposalApprovalWithoutUpdatingAssetStatus()
    {
        AssetLedger ledger = new AssetLedger();
        ledger.setAssetId(1L);
        ledger.setAssetStatus(AssetStatus.PENDING_DISPOSAL.getCode());
        when(assetLedgerMapper.selectAssetById(1L)).thenReturn(ledger);
        when(assetDisposalMapper.selectMaxDisposalNoByPrefix(any(String.class))).thenReturn("DP-2026-0009");
        when(assetDisposalMapper.insertAssetDisposal(any(AssetDisposal.class))).thenAnswer(invocation ->
        {
            AssetDisposal disposal = invocation.getArgument(0);
            disposal.setDisposalId(11L);
            return 1;
        });

        Long disposalId = disposalService.submitDisposalApproval(buildBo(), "finance");

        assertEquals(11L, disposalId);
        verify(assetLedgerMapper, never()).updateStatus(any(Long.class), any(String.class));
        verify(assetApprovalMapper).insertAssetApprovalRecord(any(AssetApprovalRecord.class));
        verify(assetChangeLogMapper).insertAssetChangeLog(any());
    }

    @Test
    @DisplayName("审批通过后应回写处置状态并将资产更新为已处置")
    void shouldApproveDisposalAndWriteBackDisposedStatus()
    {
        AssetDisposalVo disposal = buildDisposalVo("SUBMITTED");
        AssetLedger ledger = new AssetLedger();
        ledger.setAssetId(1L);
        ledger.setAssetStatus(AssetStatus.PENDING_DISPOSAL.getCode());

        when(assetDisposalMapper.selectAssetDisposalById(11L)).thenReturn(disposal);
        when(assetLedgerMapper.selectAssetById(1L)).thenReturn(ledger);
        when(assetApprovalStatusMatrix.mapToAssetStatus(AssetApprovalType.DISPOSAL, AssetApprovalStatus.APPROVED))
            .thenReturn(AssetStatus.DISPOSED);
        when(assetStatusMachine.canTransit(AssetStatus.PENDING_DISPOSAL, AssetStatus.DISPOSED)).thenReturn(true);
        when(assetDisposalMapper.updateDisposalApprovalResult(eq(11L), eq("APPROVED"), eq("auditor"), any(Date.class))).thenReturn(1);
        when(assetLedgerMapper.updateStatus(1L, AssetStatus.DISPOSED.getCode())).thenReturn(1);

        disposalService.approveDisposal(11L, buildApprovalAction("同意处置"), "auditor");

        verify(assetApprovalMapper).insertAssetApprovalRecord(any(AssetApprovalRecord.class));
        verify(assetLedgerMapper).updateStatus(1L, AssetStatus.DISPOSED.getCode());
        verify(assetChangeLogMapper).insertAssetChangeLog(any());
    }

    @Test
    @DisplayName("审批驳回后应仅更新处置单状态并保留待处置资产状态")
    void shouldRejectDisposalWithoutUpdatingAssetStatus()
    {
        AssetDisposalVo disposal = buildDisposalVo("SUBMITTED");
        when(assetDisposalMapper.selectAssetDisposalById(11L)).thenReturn(disposal);
        when(assetDisposalMapper.updateDisposalApprovalResult(eq(11L), eq("REJECTED"), eq("auditor"), any(Date.class))).thenReturn(1);

        disposalService.rejectDisposal(11L, buildApprovalAction("材料不完整"), "auditor");

        verify(assetApprovalMapper).insertAssetApprovalRecord(any(AssetApprovalRecord.class));
        verify(assetLedgerMapper, never()).updateStatus(any(Long.class), any(String.class));
        verify(assetChangeLogMapper).insertAssetChangeLog(any());
    }

    @Test
    @DisplayName("应按处置单查询审批轨迹")
    void shouldQueryApprovalRecordsByDisposalId()
    {
        when(assetApprovalMapper.selectAssetApprovalRecords("DISPOSAL", 11L))
            .thenReturn(Collections.singletonList(new AssetApprovalRecord()));

        assertEquals(1, disposalService.selectDisposalApprovalRecords(11L).size());
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

    private AssetApprovalActionBo buildApprovalAction(String opinion)
    {
        AssetApprovalActionBo bo = new AssetApprovalActionBo();
        bo.setOpinion(opinion);
        return bo;
    }

    private AssetDisposalVo buildDisposalVo(String disposalStatus)
    {
        AssetDisposalVo vo = new AssetDisposalVo();
        vo.setDisposalId(11L);
        vo.setAssetId(1L);
        vo.setDisposalStatus(disposalStatus);
        return vo;
    }
}
