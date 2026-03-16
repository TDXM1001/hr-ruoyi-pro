package com.ruoyi.asset.service.impl;

import java.net.URL;
import com.ruoyi.asset.domain.AssetDisposal;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.mapper.AssetDisposalMapper;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.workflow.service.IApprovalEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 资产处置服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AssetDisposalServiceImplTest {

    @Mock
    private AssetDisposalMapper assetDisposalMapper;

    @Mock
    private AssetInfoMapper assetInfoMapper;

    @Mock
    private IApprovalEngine approvalEngine;

    @InjectMocks
    private AssetDisposalServiceImpl assetDisposalService;

    @Test
    void shouldInsertDisposalAndMarkWorkflowPending() {
        AssetDisposal disposal = new AssetDisposal();
        disposal.setDisposalNo("DIS-20260315-001");
        disposal.setAssetId(3001L);
        disposal.setDisposalType("sell");
        disposal.setReason("出售处置");

        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetId(3001L);
        assetInfo.setAssetNo("FA-3001");
        assetInfo.setAssetStatus("1");
        when(assetInfoMapper.selectAssetInfoByAssetId(3001L)).thenReturn(assetInfo);
        when(assetDisposalMapper.insertAssetDisposal(any(AssetDisposal.class))).thenReturn(1);

        int rows = assetDisposalService.insertAssetDisposal(disposal);

        assertEquals(1, rows);
        assertNotNull(disposal.getAssetId());
        assertEquals("FA-3001", disposal.getAssetNo());
        assertEquals(0, disposal.getStatus());
        assertEquals("pending", disposal.getWfStatus());

        ArgumentCaptor<AssetDisposal> captor = ArgumentCaptor.forClass(AssetDisposal.class);
        verify(assetDisposalMapper).insertAssetDisposal(captor.capture());
        assertEquals("pending", captor.getValue().getWfStatus());
    }

    @Test
    void shouldSelectDisposalDetailByDisposalNo() {
        AssetDisposal disposal = new AssetDisposal();
        disposal.setDisposalNo("DIS-20260315-010");
        disposal.setAssetId(3005L);
        disposal.setStatus(0);
        when(assetDisposalMapper.selectAssetDisposalByDisposalNo("DIS-20260315-010")).thenReturn(disposal);

        AssetDisposal result = assetDisposalService.selectAssetDisposalByDisposalNo("DIS-20260315-010");

        assertEquals("DIS-20260315-010", result.getDisposalNo());
        assertEquals(3005L, result.getAssetId());
        assertEquals("pending", result.getWfStatus());
    }

    @Test
    void shouldLoadDisposalMapperXmlFromClasspath() {
        URL resource = getClass().getClassLoader().getResource("mapper/asset/AssetDisposalMapper.xml");
        assertNotNull(resource);
    }
}
