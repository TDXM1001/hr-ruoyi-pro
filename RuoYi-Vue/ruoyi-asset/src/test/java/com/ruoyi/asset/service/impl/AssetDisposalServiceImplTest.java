package com.ruoyi.asset.service.impl;

import java.net.URL;
import com.ruoyi.asset.domain.AssetDisposal;
import com.ruoyi.asset.mapper.AssetDisposalMapper;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.workflow.service.IApprovalEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    void shouldSelectDisposalDetailByDisposalNo() {
        AssetDisposal disposal = new AssetDisposal();
        disposal.setDisposalNo("DIS-20260315-010");
        disposal.setAssetId(3005L);
        when(assetDisposalMapper.selectAssetDisposalByDisposalNo("DIS-20260315-010")).thenReturn(disposal);

        AssetDisposal result = assetDisposalService.selectAssetDisposalByDisposalNo("DIS-20260315-010");

        assertEquals("DIS-20260315-010", result.getDisposalNo());
        assertEquals(3005L, result.getAssetId());
    }

    @Test
    void shouldLoadDisposalMapperXmlFromClasspath() {
        URL resource = getClass().getClassLoader().getResource("mapper/asset/AssetDisposalMapper.xml");
        assertNotNull(resource);
    }
}
