package com.ruoyi.asset.service.impl;

import java.util.Date;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetRequisition;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.mapper.AssetRequisitionMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.workflow.service.IApprovalEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 资产领用服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AssetRequisitionServiceImplTest {
    @Mock
    private AssetRequisitionMapper assetRequisitionMapper;

    @Mock
    private AssetInfoMapper assetInfoMapper;

    @Mock
    private IApprovalEngine approvalEngine;

    @InjectMocks
    private AssetRequisitionServiceImpl assetRequisitionService;

    @Test
    void shouldResolveAssetByAssetIdAndSyncAssetStatusWhenInsertRequisition() {
        AssetRequisition requisition = new AssetRequisition();
        requisition.setRequisitionNo("REQ-20260314-001");
        requisition.setAssetId(1001L);
        requisition.setApplyUserId(2001L);
        requisition.setApplyDeptId(3001L);
        requisition.setReason("办公领用");

        AssetInfo assetInfo = buildAssetInfo(1001L, "A-001", "1");
        when(assetInfoMapper.selectAssetInfoByAssetId(1001L)).thenReturn(assetInfo);
        when(assetRequisitionMapper.insertAssetRequisition(any(AssetRequisition.class))).thenReturn(1);

        int rows = assetRequisitionService.insertAssetRequisition(requisition);

        assertEquals(1, rows);
        assertNotNull(requisition.getAssetId());
        assertEquals("A-001", requisition.getAssetNo());
        assertEquals(0, requisition.getStatus());
        assertEquals("pending", requisition.getWfStatus());
        assertNotNull(requisition.getCreateTime());

        ArgumentCaptor<AssetRequisition> requisitionCaptor = ArgumentCaptor.forClass(AssetRequisition.class);
        verify(assetRequisitionMapper).insertAssetRequisition(requisitionCaptor.capture());
        assertEquals(1001L, requisitionCaptor.getValue().getAssetId());
        assertEquals("A-001", requisitionCaptor.getValue().getAssetNo());
        assertEquals("pending", requisitionCaptor.getValue().getWfStatus());

        ArgumentCaptor<AssetInfo> assetCaptor = ArgumentCaptor.forClass(AssetInfo.class);
        verify(assetInfoMapper).updateAssetInfo(assetCaptor.capture());
        assertEquals(1001L, assetCaptor.getValue().getAssetId());
        assertEquals("2", assetCaptor.getValue().getAssetStatus());
        verify(approvalEngine).startProcess("REQ-20260314-001", "asset_requisition");
    }

    @Test
    void shouldRejectDisposedAssetWhenInsertRequisition() {
        AssetRequisition requisition = new AssetRequisition();
        requisition.setRequisitionNo("REQ-20260314-002");
        requisition.setAssetId(2002L);

        when(assetInfoMapper.selectAssetInfoByAssetId(2002L)).thenReturn(buildAssetInfo(2002L, "A-002", "6"));

        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> assetRequisitionService.insertAssetRequisition(requisition)
        );

        assertEquals("已处置资产不能领用", exception.getMessage());
        verify(assetRequisitionMapper, never()).insertAssetRequisition(any(AssetRequisition.class));
        verify(approvalEngine, never()).startProcess(anyString(), anyString());
    }

    @Test
    void shouldRejectMaintenanceAssetWhenInsertRequisition() {
        AssetRequisition requisition = new AssetRequisition();
        requisition.setRequisitionNo("REQ-20260314-003");
        requisition.setAssetId(3003L);

        when(assetInfoMapper.selectAssetInfoByAssetId(3003L)).thenReturn(buildAssetInfo(3003L, "A-003", "3"));

        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> assetRequisitionService.insertAssetRequisition(requisition)
        );

        assertEquals("维修中的资产不能领用", exception.getMessage());
        verify(assetRequisitionMapper, never()).insertAssetRequisition(any(AssetRequisition.class));
        verify(approvalEngine, never()).startProcess(anyString(), anyString());
    }

    @Test
    void shouldReturnApprovedRequisitionAndResetAssetStatus() {
        AssetRequisition requisition = new AssetRequisition();
        requisition.setRequisitionNo("REQ-20260315-010");
        requisition.setAssetId(1005L);
        requisition.setStatus(1);
        when(assetRequisitionMapper.selectAssetRequisitionByRequisitionNo("REQ-20260315-010")).thenReturn(requisition);

        assetRequisitionService.returnAsset("REQ-20260315-010");

        ArgumentCaptor<AssetRequisition> requisitionCaptor = ArgumentCaptor.forClass(AssetRequisition.class);
        verify(assetRequisitionMapper).updateAssetRequisition(requisitionCaptor.capture());
        assertEquals("REQ-20260315-010", requisitionCaptor.getValue().getRequisitionNo());
        assertEquals(3, requisitionCaptor.getValue().getStatus());
        assertEquals("completed", requisitionCaptor.getValue().getWfStatus());

        ArgumentCaptor<AssetInfo> assetCaptor = ArgumentCaptor.forClass(AssetInfo.class);
        verify(assetInfoMapper).updateAssetInfo(assetCaptor.capture());
        assertEquals(1005L, assetCaptor.getValue().getAssetId());
        assertEquals("1", assetCaptor.getValue().getAssetStatus());
    }

    private AssetInfo buildAssetInfo(Long assetId, String assetNo, String assetStatus) {
        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetId(assetId);
        assetInfo.setAssetNo(assetNo);
        assetInfo.setAssetStatus(assetStatus);
        assetInfo.setCreateTime(new Date());
        return assetInfo;
    }
}
