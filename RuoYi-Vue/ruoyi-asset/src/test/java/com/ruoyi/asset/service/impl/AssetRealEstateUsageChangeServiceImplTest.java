package com.ruoyi.asset.service.impl;

import java.net.URL;
import java.util.Date;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetRealEstate;
import com.ruoyi.asset.domain.AssetRealEstateUsageChange;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.mapper.AssetRealEstateMapper;
import com.ruoyi.asset.mapper.AssetRealEstateUsageChangeMapper;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 不动产用途变更服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AssetRealEstateUsageChangeServiceImplTest {

    @Mock
    private AssetRealEstateUsageChangeMapper usageChangeMapper;

    @Mock
    private AssetInfoMapper assetInfoMapper;

    @Mock
    private AssetRealEstateMapper assetRealEstateMapper;

    @Mock
    private IApprovalEngine approvalEngine;

    @InjectMocks
    private AssetRealEstateUsageChangeServiceImpl service;

    @Test
    void shouldRejectUsageChangeForFixedAsset() {
        AssetRealEstateUsageChange request = buildRequest();
        request.setAssetId(9201L);
        when(assetInfoMapper.selectAssetInfoByAssetId(9201L)).thenReturn(buildAssetInfo(9201L, "FA-9201", "1"));

        ServiceException exception = assertThrows(ServiceException.class, () -> service.insertUsageChange(request));

        assertEquals("仅不动产允许发起用途变更", exception.getMessage());
    }

    @Test
    void shouldCreateUsageChangeAndUpdateRealEstateImmediately() {
        AssetRealEstateUsageChange request = buildRequest();
        AssetRealEstate realEstate = new AssetRealEstate();
        realEstate.setAssetId(9202L);
        realEstate.setLandUse("仓储");
        realEstate.setBuildingUse("工业");

        when(assetInfoMapper.selectAssetInfoByAssetId(9202L)).thenReturn(buildRealEstateAssetInfo(9202L, "RE-9202", "1"));
        when(assetRealEstateMapper.selectAssetRealEstateByAssetId(9202L)).thenReturn(realEstate);
        when(usageChangeMapper.insertUsageChange(any(AssetRealEstateUsageChange.class))).thenReturn(1);

        int rows = service.insertUsageChange(request);

        assertEquals(1, rows);
        assertNotNull(request.getAssetId());
        assertEquals("completed", request.getStatus());
        assertEquals("completed", request.getWfStatus());
        assertEquals("仓储", request.getOldLandUse());
        assertEquals("工业", request.getOldBuildingUse());
        assertNotNull(request.getCreateTime());

        ArgumentCaptor<AssetRealEstate> estateCaptor = ArgumentCaptor.forClass(AssetRealEstate.class);
        verify(assetRealEstateMapper).updateAssetRealEstate(estateCaptor.capture());
        assertEquals("住宅", estateCaptor.getValue().getBuildingUse());
        assertEquals("居住", estateCaptor.getValue().getLandUse());

        verifyNoInteractions(approvalEngine);
    }

    @Test
    void shouldLoadUsageChangeMapperXmlFromClasspath() {
        URL resource = getClass().getClassLoader().getResource("mapper/asset/AssetRealEstateUsageChangeMapper.xml");
        assertNotNull(resource);
    }

    private AssetRealEstateUsageChange buildRequest() {
        AssetRealEstateUsageChange request = new AssetRealEstateUsageChange();
        request.setUsageChangeNo("REU-20260315-001");
        request.setAssetId(9202L);
        request.setTargetLandUse("居住");
        request.setTargetBuildingUse("住宅");
        request.setReason("用途调整");
        return request;
    }

    private AssetInfo buildRealEstateAssetInfo(Long assetId, String assetNo, String assetStatus) {
        AssetInfo assetInfo = buildAssetInfo(assetId, assetNo, assetStatus);
        assetInfo.setAssetType("2");
        return assetInfo;
    }

    private AssetInfo buildAssetInfo(Long assetId, String assetNo, String assetStatus) {
        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetId(assetId);
        assetInfo.setAssetNo(assetNo);
        assetInfo.setAssetStatus(assetStatus);
        assetInfo.setAssetType("1");
        assetInfo.setCreateTime(new Date());
        return assetInfo;
    }
}
