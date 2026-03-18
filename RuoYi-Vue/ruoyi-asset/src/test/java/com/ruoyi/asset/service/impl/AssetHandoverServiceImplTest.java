package com.ruoyi.asset.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Year;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.asset.domain.AssetHandoverItem;
import com.ruoyi.asset.domain.AssetHandoverOrder;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.bo.AssetHandoverCreateBo;
import com.ruoyi.asset.enums.AssetBizType;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetHandoverItemMapper;
import com.ruoyi.asset.mapper.AssetHandoverOrderMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 资产交接服务实现测试。
 *
 * <p>重点验证批量交接的一致性约束，以及交接闭环对台账回写和日志留痕的影响。</p>
 *
 * @author Codex
 */
@ExtendWith(MockitoExtension.class)
class AssetHandoverServiceImplTest
{
    @Mock
    private AssetHandoverOrderMapper assetHandoverOrderMapper;

    @Mock
    private AssetHandoverItemMapper assetHandoverItemMapper;

    @Mock
    private AssetLedgerMapper assetLedgerMapper;

    @Mock
    private AssetChangeLogMapper assetChangeLogMapper;

    @InjectMocks
    private AssetHandoverServiceImpl service;

    @Test
    @DisplayName("同一交接单不允许混合不同资产类型")
    void shouldRejectMixedAssetTypesInOneOrder()
    {
        AssetLedger fixedAsset = buildLedger(1L, "FIXED", AssetStatus.IN_USE.getCode(), 100L, 10L, "研发楼-A区");
        AssetLedger estateAsset = buildLedger(2L, "REAL_ESTATE", AssetStatus.IN_USE.getCode(), 100L, 11L, "科研园");
        when(assetLedgerMapper.selectAssetById(1L)).thenReturn(fixedAsset);
        when(assetLedgerMapper.selectAssetById(2L)).thenReturn(estateAsset);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.createHandoverOrder(buildTransferBo(1L, 2L), "admin"));

        assertEquals("同一交接单不允许混合不同资产类型", exception.getMessage());
        verify(assetHandoverOrderMapper, never()).insertAssetHandoverOrder(any());
    }

    @Test
    @DisplayName("已处置资产不允许进入批量领用单")
    void shouldRejectDisposedAssetInAssignOrder()
    {
        AssetLedger disposedAsset = buildLedger(1L, "FIXED", AssetStatus.DISPOSED.getCode(), null, null, "仓库");
        when(assetLedgerMapper.selectAssetById(1L)).thenReturn(disposedAsset);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.createHandoverOrder(buildAssignBo(1L), "admin"));

        assertEquals("已处置资产不允许继续交接", exception.getMessage());
        verify(assetHandoverOrderMapper, never()).insertAssetHandoverOrder(any());
    }

    @Test
    @DisplayName("批量调拨成功后应写主单明细并逐项回写台账")
    void shouldCreateBatchTransferOrderAndWriteBackEveryAsset()
    {
        AssetLedger assetOne = buildLedger(1L, "FIXED", AssetStatus.IN_USE.getCode(), 100L, 10L, "研发楼-A区");
        AssetLedger assetTwo = buildLedger(2L, "FIXED", AssetStatus.IN_USE.getCode(), 101L, 11L, "研发楼-B区");
        when(assetLedgerMapper.selectAssetById(1L)).thenReturn(assetOne);
        when(assetLedgerMapper.selectAssetById(2L)).thenReturn(assetTwo);
        when(assetHandoverOrderMapper.selectMaxHandoverNoByPrefix(buildHandoverNoPrefix())).thenReturn(buildHandoverNo(7));
        when(assetHandoverOrderMapper.insertAssetHandoverOrder(any())).thenAnswer(invocation ->
        {
            AssetHandoverOrder order = invocation.getArgument(0);
            order.setHandoverOrderId(8L);
            return 1;
        });
        when(assetHandoverItemMapper.batchInsertAssetHandoverItems(any())).thenReturn(2);
        when(assetLedgerMapper.updateAssetUsageInfo(any())).thenReturn(1);

        Long handoverOrderId = service.createHandoverOrder(buildTransferBo(1L, 2L), "admin");

        assertEquals(8L, handoverOrderId);
        verify(assetHandoverOrderMapper).insertAssetHandoverOrder(argThat(order ->
            "FIXED".equals(order.getAssetType())
                && AssetBizType.TRANSFER.getCode().equals(order.getHandoverType())
                && Integer.valueOf(2).equals(order.getAssetCount())
                && Long.valueOf(200L).equals(order.getToDeptId())
                && Long.valueOf(20L).equals(order.getToUserId())));
        verify(assetHandoverItemMapper).batchInsertAssetHandoverItems(argThat((List<AssetHandoverItem> items) ->
            items.size() == 2
                && items.stream().allMatch(item ->
                    Long.valueOf(8L).equals(item.getHandoverOrderId())
                        && Long.valueOf(200L).equals(item.getToDeptId())
                        && Long.valueOf(20L).equals(item.getToUserId())
                        && AssetStatus.IN_USE.getCode().equals(item.getAfterStatus()))));
        verify(assetLedgerMapper, times(2)).updateAssetUsageInfo(any());
        verify(assetLedgerMapper).updateAssetUsageInfo(argThat(asset ->
            Long.valueOf(1L).equals(asset.getAssetId())
                && Long.valueOf(200L).equals(asset.getUseDeptId())
                && Long.valueOf(20L).equals(asset.getResponsibleUserId())
                && AssetStatus.IN_USE.getCode().equals(asset.getAssetStatus())));
        verify(assetLedgerMapper).updateAssetUsageInfo(argThat(asset ->
            Long.valueOf(2L).equals(asset.getAssetId())
                && Long.valueOf(200L).equals(asset.getUseDeptId())
                && Long.valueOf(20L).equals(asset.getResponsibleUserId())
                && AssetStatus.IN_USE.getCode().equals(asset.getAssetStatus())));
        verify(assetChangeLogMapper, times(2)).insertAssetChangeLog(any());
    }

    private AssetHandoverCreateBo buildAssignBo(Long... assetIds)
    {
        AssetHandoverCreateBo bo = new AssetHandoverCreateBo();
        bo.setAssetIds(Arrays.asList(assetIds));
        bo.setHandoverType(AssetBizType.ASSIGN.getCode());
        bo.setToDeptId(200L);
        bo.setToUserId(20L);
        bo.setLocationName("办公楼-B区");
        bo.setHandoverDate(new Date());
        bo.setRemark("批量领用测试");
        return bo;
    }

    private AssetHandoverCreateBo buildTransferBo(Long... assetIds)
    {
        AssetHandoverCreateBo bo = new AssetHandoverCreateBo();
        bo.setAssetIds(Arrays.asList(assetIds));
        bo.setHandoverType(AssetBizType.TRANSFER.getCode());
        bo.setToDeptId(200L);
        bo.setToUserId(20L);
        bo.setLocationName("办公楼-B区");
        bo.setHandoverDate(new Date());
        bo.setRemark("批量调拨测试");
        return bo;
    }

    private AssetLedger buildLedger(Long assetId, String assetType, String assetStatus, Long useDeptId,
        Long responsibleUserId, String locationName)
    {
        AssetLedger ledger = new AssetLedger();
        ledger.setAssetId(assetId);
        ledger.setAssetCode("FA-" + assetId);
        ledger.setAssetName("测试资产-" + assetId);
        ledger.setAssetType(assetType);
        ledger.setAssetStatus(assetStatus);
        ledger.setOwnerDeptId(100L);
        ledger.setUseDeptId(useDeptId);
        ledger.setResponsibleUserId(responsibleUserId);
        ledger.setLocationName(locationName);
        return ledger;
    }

    private String buildHandoverNoPrefix()
    {
        return "HD-" + Year.now().getValue() + "-";
    }

    private String buildHandoverNo(int sequence)
    {
        return buildHandoverNoPrefix() + String.format("%04d", sequence);
    }
}
