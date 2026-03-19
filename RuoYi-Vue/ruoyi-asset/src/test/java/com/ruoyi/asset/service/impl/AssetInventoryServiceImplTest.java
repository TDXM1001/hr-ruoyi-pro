package com.ruoyi.asset.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.asset.domain.AssetInventoryItem;
import com.ruoyi.asset.domain.AssetInventoryTask;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.bo.AssetInventoryTaskAssetBo;
import com.ruoyi.asset.domain.bo.AssetInventoryResultBo;
import com.ruoyi.asset.domain.vo.AssetInventoryTaskAssetVo;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetInventoryMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.asset.service.AssetStatusMachine;
import com.ruoyi.common.exception.ServiceException;

/**
 * 资产盘点服务实现测试。
 *
 * @author Codex
 */
@ExtendWith(MockitoExtension.class)
class AssetInventoryServiceImplTest
{
    @Mock
    private AssetInventoryMapper assetInventoryMapper;

    @Mock
    private AssetLedgerMapper assetLedgerMapper;

    @Mock
    private AssetChangeLogMapper assetChangeLogMapper;

    @Mock
    private AssetStatusMachine assetStatusMachine;

    @InjectMocks
    private AssetInventoryServiceImpl inventoryService;

    @Test
    @DisplayName("盘点毁损资产应流转到待处置")
    void shouldMoveAssetToPendingDisposalWhenInventoryResultDamaged()
    {
        AssetLedger ledger = new AssetLedger();
        ledger.setAssetId(1L);
        ledger.setAssetStatus(AssetStatus.INVENTORYING.getCode());
        when(assetInventoryMapper.selectAssetInventoryTaskById(10L)).thenReturn(buildTask(10L));
        when(assetInventoryMapper.selectAssetInventoryItem(10L, 1L)).thenReturn(null);
        when(assetLedgerMapper.selectAssetById(1L)).thenReturn(ledger);
        when(assetStatusMachine.canTransit(AssetStatus.INVENTORYING, AssetStatus.PENDING_DISPOSAL)).thenReturn(true);
        when(assetInventoryMapper.insertAssetInventoryItem(any(AssetInventoryItem.class))).thenReturn(1);
        when(assetLedgerMapper.updateStatus(1L, AssetStatus.PENDING_DISPOSAL.getCode())).thenReturn(1);
        when(assetLedgerMapper.updateInventoryResult(any(AssetLedger.class))).thenReturn(1);
        when(assetInventoryMapper.countInventoryItemByTaskId(10L)).thenReturn(1L);

        inventoryService.submitResult(buildDamagedResult(), "admin");

        verify(assetLedgerMapper).updateStatus(1L, AssetStatus.PENDING_DISPOSAL.getCode());
    }

    @Test
    @DisplayName("异常盘点缺少后续动作时应拒绝提交")
    void shouldRejectAbnormalInventoryWithoutFollowUpAction()
    {
        AssetInventoryResultBo bo = buildDamagedResult();
        bo.setFollowUpAction("NONE");
        when(assetInventoryMapper.selectAssetInventoryTaskById(10L)).thenReturn(buildTask(10L));
        when(assetInventoryMapper.selectAssetInventoryItem(10L, 1L)).thenReturn(null);
        AssetLedger ledger = new AssetLedger();
        ledger.setAssetId(1L);
        ledger.setAssetStatus(AssetStatus.INVENTORYING.getCode());
        when(assetLedgerMapper.selectAssetById(1L)).thenReturn(ledger);

        ServiceException exception = assertThrows(ServiceException.class, () -> inventoryService.submitResult(bo, "admin"));

        verify(assetInventoryMapper, never()).insertAssetInventoryItem(any(AssetInventoryItem.class));
        verify(assetLedgerMapper, never()).updateStatus(any(Long.class), any(String.class));
        verify(assetChangeLogMapper, never()).insertAssetChangeLog(any());
        org.junit.jupiter.api.Assertions.assertEquals("盘点异常结果必须指定后续动作", exception.getMessage());
    }

    @Test
    @DisplayName("查询任务资产明细时应规范化筛选类型并调用Mapper")
    void shouldNormalizeResultTypeWhenListTaskAssets()
    {
        AssetInventoryTaskAssetBo bo = new AssetInventoryTaskAssetBo();
        bo.setTaskId(10L);
        bo.setResultType("pending");
        when(assetInventoryMapper.selectAssetInventoryTaskById(10L)).thenReturn(buildTask(10L));

        List<AssetInventoryTaskAssetVo> expected = List.of(new AssetInventoryTaskAssetVo());
        when(assetInventoryMapper.selectTaskAssetList(any(AssetInventoryTaskAssetBo.class))).thenReturn(expected);

        List<AssetInventoryTaskAssetVo> actual = inventoryService.selectTaskAssetList(bo);

        assertSame(expected, actual);
        verify(assetInventoryMapper).selectTaskAssetList(
            argThat(param -> "PENDING".equals(param.getResultType()) && Long.valueOf(10L).equals(param.getTaskId())));
    }

    @Test
    @DisplayName("查询任务资产明细时遇到非法筛选类型应拒绝")
    void shouldRejectIllegalResultTypeWhenListTaskAssets()
    {
        AssetInventoryTaskAssetBo bo = new AssetInventoryTaskAssetBo();
        bo.setTaskId(10L);
        bo.setResultType("INVALID");
        when(assetInventoryMapper.selectAssetInventoryTaskById(10L)).thenReturn(buildTask(10L));

        ServiceException exception = assertThrows(ServiceException.class, () -> inventoryService.selectTaskAssetList(bo));

        org.junit.jupiter.api.Assertions.assertEquals("盘点结果筛选类型不合法", exception.getMessage());
        verify(assetInventoryMapper, never()).selectTaskAssetList(any(AssetInventoryTaskAssetBo.class));
    }

    private AssetInventoryResultBo buildDamagedResult()
    {
        AssetInventoryResultBo bo = new AssetInventoryResultBo();
        bo.setTaskId(10L);
        bo.setAssetId(1L);
        bo.setInventoryResult("DAMAGED");
        bo.setFollowUpAction("CREATE_DISPOSAL");
        bo.setCheckedTime(new Date());
        bo.setResultDesc("盘点确认毁损");
        return bo;
    }

    private AssetInventoryTask buildTask(Long taskId)
    {
        AssetInventoryTask task = new AssetInventoryTask();
        task.setTaskId(taskId);
        task.setTaskNo("IV-2026-0001");
        task.setScopeType("ASSET");
        task.setScopeValue("1");
        return task;
    }
}
