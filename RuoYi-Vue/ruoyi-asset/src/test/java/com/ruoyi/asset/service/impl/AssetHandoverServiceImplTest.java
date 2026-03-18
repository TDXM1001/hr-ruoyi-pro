package com.ruoyi.asset.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
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
import com.ruoyi.asset.domain.AssetHandover;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.bo.AssetHandoverBo;
import com.ruoyi.asset.enums.AssetBizType;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetHandoverMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 资产交接服务测试。
 *
 * <p>
 * 重点校验固定资产一期交接闭环中的关键控制点：
 * 已处置资产禁止领用、调拨必须写留痕并回写台账当前使用关系。
 * </p>
 *
 * @author Codex
 */
@ExtendWith(MockitoExtension.class)
class AssetHandoverServiceImplTest
{
    @Mock
    private AssetHandoverMapper assetHandoverMapper;

    @Mock
    private AssetLedgerMapper assetLedgerMapper;

    @Mock
    private AssetChangeLogMapper assetChangeLogMapper;

    @InjectMocks
    private AssetHandoverServiceImpl service;

    @Test
    @DisplayName("已处置资产不允许发起领用")
    void shouldRejectAssignWhenAssetDisposed()
    {
        AssetLedger ledger = new AssetLedger();
        ledger.setAssetId(1L);
        ledger.setAssetStatus(AssetStatus.DISPOSED.getCode());
        when(assetLedgerMapper.selectAssetById(1L)).thenReturn(ledger);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.createHandover(buildAssignBo(), "admin"));

        assertEquals("已处置资产不允许发起交接", exception.getMessage());
        verify(assetHandoverMapper, never()).insertAssetHandover(any());
    }

    @Test
    @DisplayName("调拨成功后应该写交接记录、回写台账并写变更日志")
    void shouldWriteChangeLogAfterTransfer()
    {
        AssetLedger ledger = new AssetLedger();
        ledger.setAssetId(1L);
        ledger.setAssetStatus(AssetStatus.IN_USE.getCode());
        ledger.setOwnerDeptId(100L);
        ledger.setUseDeptId(100L);
        ledger.setResponsibleUserId(10L);
        ledger.setLocationName("研发一部-A区");
        when(assetLedgerMapper.selectAssetById(1L)).thenReturn(ledger);
        when(assetHandoverMapper.selectMaxHandoverNoByPrefix(buildHandoverNoPrefix())).thenReturn(buildHandoverNo(1));
        when(assetHandoverMapper.insertAssetHandover(any())).thenAnswer(invocation ->
        {
            AssetHandover handover = invocation.getArgument(0);
            handover.setHandoverId(2L);
            return 1;
        });
        when(assetLedgerMapper.updateAssetUsageInfo(any())).thenReturn(1);

        Long handoverId = service.createHandover(buildTransferBo(), "admin");

        assertEquals(2L, handoverId);
        verify(assetHandoverMapper).insertAssetHandover(argThat(handover ->
            AssetBizType.TRANSFER.getCode().equals(handover.getHandoverType())
                && Long.valueOf(100L).equals(handover.getFromDeptId())
                && Long.valueOf(10L).equals(handover.getFromUserId())
                && Long.valueOf(200L).equals(handover.getToDeptId())
                && Long.valueOf(20L).equals(handover.getToUserId())));
        verify(assetLedgerMapper).updateAssetUsageInfo(argThat(asset ->
            Long.valueOf(1L).equals(asset.getAssetId())
                && Long.valueOf(200L).equals(asset.getUseDeptId())
                && Long.valueOf(20L).equals(asset.getResponsibleUserId())
                && AssetStatus.IN_USE.getCode().equals(asset.getAssetStatus())));
        verify(assetChangeLogMapper).insertAssetChangeLog(argThat(log ->
            AssetBizType.TRANSFER.getCode().equals(log.getBizType())
                && Long.valueOf(2L).equals(log.getBizId())
                && AssetStatus.IN_USE.getCode().equals(log.getBeforeStatus())
                && AssetStatus.IN_USE.getCode().equals(log.getAfterStatus())
                && "admin".equals(log.getOperateBy())));
    }

    private AssetHandoverBo buildAssignBo()
    {
        AssetHandoverBo bo = new AssetHandoverBo();
        bo.setAssetId(1L);
        bo.setHandoverType(AssetBizType.ASSIGN.getCode());
        bo.setToDeptId(200L);
        bo.setToUserId(20L);
        bo.setLocationName("运营二部-B区");
        bo.setHandoverDate(new Date());
        bo.setRemark("领用测试");
        return bo;
    }

    private AssetHandoverBo buildTransferBo()
    {
        AssetHandoverBo bo = new AssetHandoverBo();
        bo.setAssetId(1L);
        bo.setHandoverType(AssetBizType.TRANSFER.getCode());
        bo.setToDeptId(200L);
        bo.setToUserId(20L);
        bo.setLocationName("运营二部-B区");
        bo.setHandoverDate(new Date());
        bo.setRemark("调拨测试");
        return bo;
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
