package com.ruoyi.asset.service.impl;

import java.net.URL;
import java.util.Date;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetRealEstateStatusChange;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.mapper.AssetRealEstateStatusChangeMapper;
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
 * 不动产状态变更服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AssetRealEstateStatusChangeServiceImplTest {

    @Mock
    private AssetRealEstateStatusChangeMapper statusChangeMapper;

    @Mock
    private AssetInfoMapper assetInfoMapper;

    @Mock
    private IApprovalEngine approvalEngine;

    @InjectMocks
    private AssetRealEstateStatusChangeServiceImpl service;

    @Test
    void shouldRejectStatusChangeForFixedAsset() {
        AssetRealEstateStatusChange request = buildRequest();
        request.setAssetId(9301L);
        when(assetInfoMapper.selectAssetInfoByAssetId(9301L)).thenReturn(buildAssetInfo(9301L, "FA-9301", "1"));

        ServiceException exception = assertThrows(ServiceException.class, () -> service.insertStatusChange(request));

        assertEquals("仅不动产允许发起状态变更", exception.getMessage());
    }

    @Test
    void shouldCreateStatusChangeAndUpdateAssetImmediately() {
        AssetRealEstateStatusChange request = buildRequest();
        when(assetInfoMapper.selectAssetInfoByAssetId(9302L)).thenReturn(buildRealEstateAssetInfo(9302L, "RE-9302", "1"));
        when(statusChangeMapper.insertStatusChange(any(AssetRealEstateStatusChange.class))).thenReturn(1);

        int rows = service.insertStatusChange(request);

        assertEquals(1, rows);
        assertNotNull(request.getAssetId());
        assertEquals("completed", request.getStatus());
        assertEquals("completed", request.getWfStatus());
        assertEquals("1", request.getOldAssetStatus());
        assertNotNull(request.getCreateTime());

        ArgumentCaptor<AssetInfo> assetCaptor = ArgumentCaptor.forClass(AssetInfo.class);
        verify(assetInfoMapper).updateAssetInfo(assetCaptor.capture());
        assertEquals(9302L, assetCaptor.getValue().getAssetId());
        assertEquals("7", assetCaptor.getValue().getAssetStatus());

        verifyNoInteractions(approvalEngine);
    }

    @Test
    void shouldLoadStatusChangeMapperXmlFromClasspath() {
        URL resource = getClass().getClassLoader().getResource("mapper/asset/AssetRealEstateStatusChangeMapper.xml");
        assertNotNull(resource);
    }

    private AssetRealEstateStatusChange buildRequest() {
        AssetRealEstateStatusChange request = new AssetRealEstateStatusChange();
        request.setStatusChangeNo("RES-20260315-001");
        request.setAssetId(9302L);
        request.setTargetAssetStatus("7");
        request.setReason("转为闲置");
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
