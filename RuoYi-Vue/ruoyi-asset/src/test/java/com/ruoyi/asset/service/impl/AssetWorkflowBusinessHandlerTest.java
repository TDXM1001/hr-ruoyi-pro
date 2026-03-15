package com.ruoyi.asset.service.impl;

import java.util.Date;
import com.ruoyi.asset.domain.AssetDisposal;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetMaintenance;
import com.ruoyi.asset.domain.AssetRealEstate;
import com.ruoyi.asset.domain.AssetRealEstateDisposal;
import com.ruoyi.asset.domain.AssetRealEstateOwnershipChange;
import com.ruoyi.asset.domain.AssetRequisition;
import com.ruoyi.asset.mapper.AssetDisposalMapper;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.mapper.AssetMaintenanceMapper;
import com.ruoyi.asset.mapper.AssetRealEstateDisposalMapper;
import com.ruoyi.asset.mapper.AssetRealEstateMapper;
import com.ruoyi.asset.mapper.AssetRealEstateOwnershipChangeMapper;
import com.ruoyi.asset.mapper.AssetRequisitionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 资产工作流业务回写测试。
 *
 * 这组测试只锁定“审批结果如何回写业务单据和资产状态”，
 * 不关心审批引擎本身如何查实例、如何记节点，那部分职责留给 workflow 模块测试覆盖。
 */
@ExtendWith(MockitoExtension.class)
class AssetWorkflowBusinessHandlerTest {

    @Mock
    private AssetRequisitionMapper assetRequisitionMapper;

    @Mock
    private AssetMaintenanceMapper assetMaintenanceMapper;

    @Mock
    private AssetDisposalMapper assetDisposalMapper;

    @Mock
    private AssetInfoMapper assetInfoMapper;

    @Mock
    private AssetRealEstateOwnershipChangeMapper assetRealEstateOwnershipChangeMapper;

    @Mock
    private AssetRealEstateDisposalMapper assetRealEstateDisposalMapper;

    @Mock
    private AssetRealEstateMapper assetRealEstateMapper;

    @InjectMocks
    private AssetWorkflowBusinessHandler assetWorkflowBusinessHandler;

    @Test
    void shouldSupportRealEstateWorkflowBusinessTypes() {
        assertTrue(assetWorkflowBusinessHandler.getSupportedBusinessTypes().contains("asset_real_estate_ownership_change"));
        assertTrue(assetWorkflowBusinessHandler.getSupportedBusinessTypes().contains("asset_real_estate_disposal"));
    }

    @Test
    void shouldMarkRequisitionApprovedAndKeepAssetAsBorrowedWhenApprovalPasses() {
        AssetRequisition requisition = new AssetRequisition();
        requisition.setRequisitionNo("REQ-20260315-001");
        requisition.setAssetId(1001L);
        requisition.setStatus(0);
        when(assetRequisitionMapper.selectAssetRequisitionByRequisitionNo("REQ-20260315-001")).thenReturn(requisition);

        assetWorkflowBusinessHandler.onApprove("asset_requisition", "REQ-20260315-001");

        ArgumentCaptor<AssetRequisition> requisitionCaptor = ArgumentCaptor.forClass(AssetRequisition.class);
        verify(assetRequisitionMapper).updateAssetRequisition(requisitionCaptor.capture());
        assertEquals(1, requisitionCaptor.getValue().getStatus());

        ArgumentCaptor<AssetInfo> assetCaptor = ArgumentCaptor.forClass(AssetInfo.class);
        verify(assetInfoMapper).updateAssetInfo(assetCaptor.capture());
        assertEquals(1001L, assetCaptor.getValue().getAssetId());
        assertEquals("2", assetCaptor.getValue().getAssetStatus());
    }

    @Test
    void shouldRollbackRequisitionAssetStatusWhenApprovalRejected() {
        AssetRequisition requisition = new AssetRequisition();
        requisition.setRequisitionNo("REQ-20260315-002");
        requisition.setAssetId(1002L);
        requisition.setStatus(0);
        when(assetRequisitionMapper.selectAssetRequisitionByRequisitionNo("REQ-20260315-002")).thenReturn(requisition);

        assetWorkflowBusinessHandler.onReject("asset_requisition", "REQ-20260315-002");

        ArgumentCaptor<AssetRequisition> requisitionCaptor = ArgumentCaptor.forClass(AssetRequisition.class);
        verify(assetRequisitionMapper).updateAssetRequisition(requisitionCaptor.capture());
        assertEquals(2, requisitionCaptor.getValue().getStatus());

        ArgumentCaptor<AssetInfo> assetCaptor = ArgumentCaptor.forClass(AssetInfo.class);
        verify(assetInfoMapper).updateAssetInfo(assetCaptor.capture());
        assertEquals("1", assetCaptor.getValue().getAssetStatus());
    }

    @Test
    void shouldMarkMaintenanceApprovedAndKeepAssetInMaintenanceWhenApprovalPasses() {
        AssetMaintenance maintenance = new AssetMaintenance();
        maintenance.setMaintenanceNo("MNT-20260315-001");
        maintenance.setAssetId(2001L);
        maintenance.setStatus(0);
        when(assetMaintenanceMapper.selectAssetMaintenanceByMaintenanceNo("MNT-20260315-001")).thenReturn(maintenance);

        assetWorkflowBusinessHandler.onApprove("asset_maintenance", "MNT-20260315-001");

        ArgumentCaptor<AssetMaintenance> maintenanceCaptor = ArgumentCaptor.forClass(AssetMaintenance.class);
        verify(assetMaintenanceMapper).updateAssetMaintenance(maintenanceCaptor.capture());
        assertEquals(1, maintenanceCaptor.getValue().getStatus());

        ArgumentCaptor<AssetInfo> assetCaptor = ArgumentCaptor.forClass(AssetInfo.class);
        verify(assetInfoMapper).updateAssetInfo(assetCaptor.capture());
        assertEquals("3", assetCaptor.getValue().getAssetStatus());
    }

    @Test
    void shouldRollbackMaintenanceAssetStatusWhenApprovalRejected() {
        AssetMaintenance maintenance = new AssetMaintenance();
        maintenance.setMaintenanceNo("MNT-20260315-002");
        maintenance.setAssetId(2002L);
        maintenance.setStatus(0);
        when(assetMaintenanceMapper.selectAssetMaintenanceByMaintenanceNo("MNT-20260315-002")).thenReturn(maintenance);

        assetWorkflowBusinessHandler.onReject("asset_maintenance", "MNT-20260315-002");

        ArgumentCaptor<AssetMaintenance> maintenanceCaptor = ArgumentCaptor.forClass(AssetMaintenance.class);
        verify(assetMaintenanceMapper).updateAssetMaintenance(maintenanceCaptor.capture());
        assertEquals(2, maintenanceCaptor.getValue().getStatus());

        ArgumentCaptor<AssetInfo> assetCaptor = ArgumentCaptor.forClass(AssetInfo.class);
        verify(assetInfoMapper).updateAssetInfo(assetCaptor.capture());
        assertEquals("1", assetCaptor.getValue().getAssetStatus());
    }

    @Test
    void shouldMarkDisposalApprovedAndKeepAssetAsScrappedWhenApprovalPasses() {
        AssetDisposal disposal = new AssetDisposal();
        disposal.setDisposalNo("DIS-20260315-001");
        disposal.setAssetId(3001L);
        disposal.setStatus(0);
        when(assetDisposalMapper.selectAssetDisposalByDisposalNo("DIS-20260315-001")).thenReturn(disposal);

        assetWorkflowBusinessHandler.onApprove("asset_disposal", "DIS-20260315-001");

        ArgumentCaptor<AssetDisposal> disposalCaptor = ArgumentCaptor.forClass(AssetDisposal.class);
        verify(assetDisposalMapper).updateAssetDisposal(disposalCaptor.capture());
        assertEquals(1, disposalCaptor.getValue().getStatus());

        ArgumentCaptor<AssetInfo> assetCaptor = ArgumentCaptor.forClass(AssetInfo.class);
        verify(assetInfoMapper).updateAssetInfo(assetCaptor.capture());
        assertEquals("5", assetCaptor.getValue().getAssetStatus());
    }

    @Test
    void shouldRollbackDisposalAssetStatusWhenApprovalRejected() {
        AssetDisposal disposal = new AssetDisposal();
        disposal.setDisposalNo("DIS-20260315-002");
        disposal.setAssetId(3002L);
        disposal.setStatus(0);
        when(assetDisposalMapper.selectAssetDisposalByDisposalNo("DIS-20260315-002")).thenReturn(disposal);

        assetWorkflowBusinessHandler.onReject("asset_disposal", "DIS-20260315-002");

        ArgumentCaptor<AssetDisposal> disposalCaptor = ArgumentCaptor.forClass(AssetDisposal.class);
        verify(assetDisposalMapper).updateAssetDisposal(disposalCaptor.capture());
        assertEquals(2, disposalCaptor.getValue().getStatus());

        ArgumentCaptor<AssetInfo> assetCaptor = ArgumentCaptor.forClass(AssetInfo.class);
        verify(assetInfoMapper).updateAssetInfo(assetCaptor.capture());
        assertEquals("1", assetCaptor.getValue().getAssetStatus());
    }

    @Test
    void shouldApplyOwnershipChangeToRealEstateWhenApprovalPasses() {
        AssetRealEstateOwnershipChange ownershipChange = new AssetRealEstateOwnershipChange();
        ownershipChange.setOwnershipChangeNo("OWN-20260315-001");
        ownershipChange.setAssetId(4001L);
        ownershipChange.setTargetRightsHolder("李四");
        ownershipChange.setTargetPropertyCertNo("CERT-NEW");
        ownershipChange.setTargetRegistrationDate(new Date(1742083200000L));
        when(assetRealEstateOwnershipChangeMapper.selectOwnershipChangeByNo("OWN-20260315-001")).thenReturn(ownershipChange);

        assetWorkflowBusinessHandler.onApprove("asset_real_estate_ownership_change", "OWN-20260315-001");

        ArgumentCaptor<AssetRealEstateOwnershipChange> changeCaptor =
            ArgumentCaptor.forClass(AssetRealEstateOwnershipChange.class);
        verify(assetRealEstateOwnershipChangeMapper).updateOwnershipChange(changeCaptor.capture());
        assertEquals("approved", changeCaptor.getValue().getStatus());

        ArgumentCaptor<AssetRealEstate> estateCaptor = ArgumentCaptor.forClass(AssetRealEstate.class);
        verify(assetRealEstateMapper).updateAssetRealEstate(estateCaptor.capture());
        assertEquals(4001L, estateCaptor.getValue().getAssetId());
        assertEquals("李四", estateCaptor.getValue().getRightsHolder());
        assertEquals("CERT-NEW", estateCaptor.getValue().getPropertyCertNo());
    }

    @Test
    void shouldOnlyRejectOwnershipChangeDocumentWithoutTouchingRealEstate() {
        AssetRealEstateOwnershipChange ownershipChange = new AssetRealEstateOwnershipChange();
        ownershipChange.setOwnershipChangeNo("OWN-20260315-002");
        ownershipChange.setAssetId(4002L);
        when(assetRealEstateOwnershipChangeMapper.selectOwnershipChangeByNo("OWN-20260315-002")).thenReturn(ownershipChange);

        assetWorkflowBusinessHandler.onReject("asset_real_estate_ownership_change", "OWN-20260315-002");

        ArgumentCaptor<AssetRealEstateOwnershipChange> changeCaptor =
            ArgumentCaptor.forClass(AssetRealEstateOwnershipChange.class);
        verify(assetRealEstateOwnershipChangeMapper).updateOwnershipChange(changeCaptor.capture());
        assertEquals("rejected", changeCaptor.getValue().getStatus());
        verify(assetRealEstateMapper, never()).updateAssetRealEstate(any(AssetRealEstate.class));
        verify(assetInfoMapper, never()).updateAssetInfo(any(AssetInfo.class));
    }

    @Test
    void shouldMarkRealEstateDisposalApprovedAndUpdateAssetStatusWhenApprovalPasses() {
        AssetRealEstateDisposal disposal = new AssetRealEstateDisposal();
        disposal.setDisposalNo("RED-20260315-001");
        disposal.setAssetId(5001L);
        disposal.setTargetAssetStatus("6");
        when(assetRealEstateDisposalMapper.selectDisposalByNo("RED-20260315-001")).thenReturn(disposal);

        assetWorkflowBusinessHandler.onApprove("asset_real_estate_disposal", "RED-20260315-001");

        ArgumentCaptor<AssetRealEstateDisposal> disposalCaptor = ArgumentCaptor.forClass(AssetRealEstateDisposal.class);
        verify(assetRealEstateDisposalMapper).updateDisposal(disposalCaptor.capture());
        assertEquals("approved", disposalCaptor.getValue().getStatus());

        ArgumentCaptor<AssetInfo> assetCaptor = ArgumentCaptor.forClass(AssetInfo.class);
        verify(assetInfoMapper).updateAssetInfo(assetCaptor.capture());
        assertEquals(5001L, assetCaptor.getValue().getAssetId());
        assertEquals("6", assetCaptor.getValue().getAssetStatus());
    }

    @Test
    void shouldOnlyRejectRealEstateDisposalDocumentWithoutTouchingAsset() {
        AssetRealEstateDisposal disposal = new AssetRealEstateDisposal();
        disposal.setDisposalNo("RED-20260315-002");
        disposal.setAssetId(5002L);
        when(assetRealEstateDisposalMapper.selectDisposalByNo("RED-20260315-002")).thenReturn(disposal);

        assetWorkflowBusinessHandler.onReject("asset_real_estate_disposal", "RED-20260315-002");

        ArgumentCaptor<AssetRealEstateDisposal> disposalCaptor = ArgumentCaptor.forClass(AssetRealEstateDisposal.class);
        verify(assetRealEstateDisposalMapper).updateDisposal(disposalCaptor.capture());
        assertEquals("rejected", disposalCaptor.getValue().getStatus());
        verify(assetInfoMapper, never()).updateAssetInfo(any(AssetInfo.class));
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
