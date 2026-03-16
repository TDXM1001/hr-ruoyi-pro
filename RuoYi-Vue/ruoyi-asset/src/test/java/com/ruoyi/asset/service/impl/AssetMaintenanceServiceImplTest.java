package com.ruoyi.asset.service.impl;

import java.net.URL;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetMaintenance;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.mapper.AssetMaintenanceMapper;
import com.ruoyi.workflow.service.IApprovalEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 资产维修服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AssetMaintenanceServiceImplTest {

    @Mock
    private AssetMaintenanceMapper assetMaintenanceMapper;

    @Mock
    private AssetInfoMapper assetInfoMapper;

    @Mock
    private IApprovalEngine approvalEngine;

    @InjectMocks
    private AssetMaintenanceServiceImpl assetMaintenanceService;

    @Test
    void shouldInsertMaintenanceAndMarkWorkflowPending() {
        AssetMaintenance maintenance = new AssetMaintenance();
        maintenance.setMaintenanceNo("MNT-20260315-001");
        maintenance.setAssetId(2001L);
        maintenance.setReason("例行维修");

        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetId(2001L);
        assetInfo.setAssetNo("A-2001");
        assetInfo.setAssetStatus("1");
        when(assetInfoMapper.selectAssetInfoByAssetId(2001L)).thenReturn(assetInfo);
        when(assetMaintenanceMapper.insertAssetMaintenance(any(AssetMaintenance.class))).thenReturn(1);

        int rows = assetMaintenanceService.insertAssetMaintenance(maintenance);

        assertEquals(1, rows);
        assertNotNull(maintenance.getAssetId());
        assertEquals("A-2001", maintenance.getAssetNo());
        assertEquals(0, maintenance.getStatus());
        assertEquals("pending", maintenance.getWfStatus());
    }

    @Test
    void shouldCompleteMaintenanceAndResetAssetStatus() {
        AssetMaintenance maintenance = new AssetMaintenance();
        maintenance.setMaintenanceNo("MNT-20260315-010");
        maintenance.setAssetId(2005L);
        maintenance.setStatus(1);
        when(assetMaintenanceMapper.selectAssetMaintenanceByMaintenanceNo("MNT-20260315-010")).thenReturn(maintenance);

        assetMaintenanceService.completeMaintenance("MNT-20260315-010");

        ArgumentCaptor<AssetMaintenance> maintenanceCaptor = ArgumentCaptor.forClass(AssetMaintenance.class);
        verify(assetMaintenanceMapper).updateAssetMaintenance(maintenanceCaptor.capture());
        assertEquals(3, maintenanceCaptor.getValue().getStatus());
        assertEquals("completed", maintenanceCaptor.getValue().getWfStatus());

        ArgumentCaptor<AssetInfo> assetCaptor = ArgumentCaptor.forClass(AssetInfo.class);
        verify(assetInfoMapper).updateAssetInfo(assetCaptor.capture());
        assertEquals(2005L, assetCaptor.getValue().getAssetId());
        assertEquals("1", assetCaptor.getValue().getAssetStatus());
    }

    @Test
    void shouldLoadMaintenanceMapperXmlFromClasspath() {
        URL resource = getClass().getClassLoader().getResource("mapper/asset/AssetMaintenanceMapper.xml");
        assertNotNull(resource);
    }
}
