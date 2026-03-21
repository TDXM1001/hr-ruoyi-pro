package com.ruoyi.asset.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
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
import com.ruoyi.asset.domain.vo.AssetRectificationVo;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetInventoryMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.asset.mapper.AssetRectificationMapper;

/**
 * 资产整改服务测试。
 *
 * <p>从资产管理者视角校验：巡检异常发起整改后，要能稳定回写跟进状态；
 * 整改完成后，巡检结果应转为已处理。</p>
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
    @DisplayName("整改完成时应将巡检结果处理状态回写为已处理")
    void shouldMarkInventoryFollowUpProcessedWhenRectificationCompleted()
    {
        AssetRectificationVo current = new AssetRectificationVo();
        current.setRectificationId(9001L);
        current.setRectificationNo(buildRectificationNo(1));
        current.setAssetId(20001L);
        current.setCompletedTime(null);

        when(assetRectificationMapper.selectAssetRectificationById(9001L)).thenReturn(current);
        when(assetInventoryMapper.selectAssetInventoryItemById(66L)).thenReturn(buildInventoryItem());
        when(assetRectificationMapper.updateAssetRectification(any())).thenReturn(1);
        when(assetInventoryMapper.updateInventoryItemFollowUp(anyLong(), anyString(), nullable(Date.class), anyLong())).thenReturn(1);

        int rows = service.updateAssetRectification(buildBo(9001L, "COMPLETED"), "asset-admin");

        assertEquals(1, rows);
        verify(assetRectificationMapper).updateAssetRectification(any(AssetRectificationOrder.class));
        verify(assetInventoryMapper).updateInventoryItemFollowUp(anyLong(), anyString(), nullable(Date.class), anyLong());
        verify(assetChangeLogMapper).insertAssetChangeLog(any(AssetChangeLog.class));
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
