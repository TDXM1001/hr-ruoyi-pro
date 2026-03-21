package com.ruoyi.asset.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Year;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.asset.domain.AssetChangeLog;
import com.ruoyi.asset.domain.AssetInventoryItem;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.AssetRectificationOrder;
import com.ruoyi.asset.domain.bo.AssetRectificationBo;
import com.ruoyi.asset.domain.bo.AssetRectificationCompleteBo;
import com.ruoyi.asset.domain.vo.AssetRectificationVo;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetInventoryMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.asset.mapper.AssetRectificationMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 资产整改服务测试。
 *
 * <p>从资产管理者视角校验：整改登记阶段只能沉淀责任和期限，
 * “待整改 -> 已完成”必须通过独立完成动作推进，不能靠普通编辑绕过。</p>
 *
 * @author Codex
 */
@ExtendWith(MockitoExtension.class)
class AssetRectificationServiceImplTest
{
    @Mock
    private AssetRectificationMapper assetRectificationMapper;

    @Mock
    private AssetInventoryMapper assetInventoryMapper;

    @Mock
    private AssetLedgerMapper assetLedgerMapper;

    @Mock
    private AssetChangeLogMapper assetChangeLogMapper;

    @InjectMocks
    private AssetRectificationServiceImpl service;

    @Test
    @DisplayName("发起整改时应生成整改单并回写巡检结果挂接关系")
    void shouldCreateRectificationAndSyncInventoryFollowUp()
    {
        when(assetInventoryMapper.selectAssetInventoryItemById(66L)).thenReturn(buildInventoryItem());
        when(assetRectificationMapper.selectByInventoryItemId(66L)).thenReturn(null);
        when(assetLedgerMapper.selectAssetById(20001L)).thenReturn(buildAsset());
        when(assetRectificationMapper.selectMaxRectificationNoByPrefix(buildNoPrefix())).thenReturn(buildRectificationNo(1));
        when(assetRectificationMapper.insertAssetRectification(any())).thenAnswer(invocation ->
        {
            AssetRectificationOrder order = invocation.getArgument(0);
            order.setRectificationId(9001L);
            return 1;
        });
        when(assetInventoryMapper.updateInventoryItemFollowUp(66L, "PENDING", null, 9001L)).thenReturn(1);

        Long rectificationId = service.createAssetRectification(buildBo(null, "PENDING"), "asset-admin");

        assertEquals(9001L, rectificationId);
        verify(assetRectificationMapper).insertAssetRectification(any(AssetRectificationOrder.class));
        verify(assetInventoryMapper).updateInventoryItemFollowUp(66L, "PENDING", null, 9001L);
        verify(assetChangeLogMapper).insertAssetChangeLog(any(AssetChangeLog.class));
    }

    @Test
    @DisplayName("整改登记创建阶段不允许直接置为已完成")
    void shouldRejectCreatingCompletedRectification()
    {
        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.createAssetRectification(buildBo(null, "COMPLETED"), "asset-admin"));

        assertEquals("整改登记阶段不允许直接置为已完成，请使用整改完成动作", exception.getMessage());
        verify(assetRectificationMapper, never()).insertAssetRectification(any(AssetRectificationOrder.class));
    }

    @Test
    @DisplayName("更新待整改整改单时应维持待整改状态")
    void shouldKeepRectificationPendingWhenUpdatingPendingOrder()
    {
        AssetRectificationVo current = buildCurrentRectification("PENDING");

        when(assetRectificationMapper.selectAssetRectificationById(9001L)).thenReturn(current);
        when(assetInventoryMapper.selectAssetInventoryItemById(66L)).thenReturn(buildInventoryItem());
        when(assetRectificationMapper.updateAssetRectification(any())).thenReturn(1);
        when(assetInventoryMapper.updateInventoryItemFollowUp(eq(66L), eq("PENDING"), nullable(Date.class), eq(9001L)))
            .thenReturn(1);

        int rows = service.updateAssetRectification(buildBo(9001L, "PENDING"), "asset-admin");

        assertEquals(1, rows);
        verify(assetRectificationMapper).updateAssetRectification(any(AssetRectificationOrder.class));
        verify(assetInventoryMapper).updateInventoryItemFollowUp(eq(66L), eq("PENDING"), nullable(Date.class), eq(9001L));
        verify(assetChangeLogMapper).insertAssetChangeLog(any(AssetChangeLog.class));
    }

    @Test
    @DisplayName("整改登记阶段不允许通过普通编辑直接完成整改单")
    void shouldRejectCompletingRectificationThroughUpdateAction()
    {
        when(assetRectificationMapper.selectAssetRectificationById(9001L)).thenReturn(buildCurrentRectification("PENDING"));

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.updateAssetRectification(buildBo(9001L, "COMPLETED"), "asset-admin"));

        assertEquals("整改登记阶段不允许直接置为已完成，请使用整改完成动作", exception.getMessage());
        verify(assetRectificationMapper, never()).updateAssetRectification(any(AssetRectificationOrder.class));
    }

    @Test
    @DisplayName("已完成整改单不允许再次编辑")
    void shouldRejectEditingCompletedRectification()
    {
        when(assetRectificationMapper.selectAssetRectificationById(9001L))
            .thenReturn(buildCurrentRectification("COMPLETED"));

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.updateAssetRectification(buildBo(9001L, "PENDING"), "asset-admin"));

        assertEquals("整改单已完成，不允许再次编辑", exception.getMessage());
        verify(assetRectificationMapper, never()).updateAssetRectification(any(AssetRectificationOrder.class));
    }

    @Test
    @DisplayName("独立完成整改时应写入完成说明并回写已处理状态")
    void shouldCompleteRectificationWithCompletionDesc()
    {
        AssetRectificationVo current = buildCurrentRectification("PENDING");

        when(assetRectificationMapper.selectAssetRectificationById(9001L)).thenReturn(current);
        when(assetRectificationMapper.updateAssetRectification(any())).thenReturn(1);
        when(assetInventoryMapper.updateInventoryItemFollowUp(eq(66L), eq("PROCESSED"), nullable(Date.class), eq(9001L)))
            .thenReturn(1);

        int rows = service.completeAssetRectification(20001L, 9001L, buildCompleteBo(), "asset-admin");

        assertEquals(1, rows);
        verify(assetRectificationMapper).updateAssetRectification(any(AssetRectificationOrder.class));
        verify(assetInventoryMapper).updateInventoryItemFollowUp(eq(66L), eq("PROCESSED"), nullable(Date.class), eq(9001L));
        verify(assetChangeLogMapper).insertAssetChangeLog(any(AssetChangeLog.class));
    }

    private AssetRectificationVo buildCurrentRectification(String rectificationStatus)
    {
        AssetRectificationVo current = new AssetRectificationVo();
        current.setRectificationId(9001L);
        current.setRectificationNo(buildRectificationNo(1));
        current.setAssetId(20001L);
        current.setTaskId(6L);
        current.setInventoryItemId(66L);
        current.setRectificationStatus(rectificationStatus);
        current.setCompletedTime(null);
        return current;
    }

    private AssetRectificationBo buildBo(Long rectificationId, String rectificationStatus)
    {
        AssetRectificationBo bo = new AssetRectificationBo();
        bo.setRectificationId(rectificationId);
        bo.setAssetId(20001L);
        bo.setTaskId(6L);
        bo.setInventoryItemId(66L);
        bo.setRectificationStatus(rectificationStatus);
        bo.setIssueType("位置不符");
        bo.setIssueDesc("房间实际使用人与台账不一致");
        bo.setResponsibleDeptId(103L);
        bo.setResponsibleUserId(1L);
        bo.setDeadlineDate(new Date());
        bo.setRemark("整改测试");
        return bo;
    }

    private AssetRectificationCompleteBo buildCompleteBo()
    {
        AssetRectificationCompleteBo bo = new AssetRectificationCompleteBo();
        bo.setCompletionDesc("已完成现场整改并复核通过");
        bo.setAcceptanceRemark("资产管理员验收通过");
        return bo;
    }

    private AssetInventoryItem buildInventoryItem()
    {
        AssetInventoryItem item = new AssetInventoryItem();
        item.setItemId(66L);
        item.setTaskId(6L);
        item.setAssetId(20001L);
        item.setInventoryResult("LOCATION_DIFF");
        item.setFollowUpAction("UPDATE_LEDGER");
        item.setProcessStatus("PENDING");
        return item;
    }

    private AssetLedger buildAsset()
    {
        AssetLedger asset = new AssetLedger();
        asset.setAssetId(20001L);
        asset.setAssetCode("RE-2026-0001");
        asset.setAssetName("深圳研发办公楼A座");
        return asset;
    }

    private String buildNoPrefix()
    {
        return "RC-" + Year.now().getValue() + "-";
    }

    private String buildRectificationNo(int sequence)
    {
        return buildNoPrefix() + String.format("%04d", sequence);
    }
}
