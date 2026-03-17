package com.ruoyi.asset.service.impl;

import java.net.URL;
import java.util.Date;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetRealEstateDisposal;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.mapper.AssetRealEstateDisposalMapper;
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
 * 不动产注销/处置服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AssetRealEstateDisposalServiceImplTest {

    @Mock
    private AssetRealEstateDisposalMapper disposalMapper;

    @Mock
    private AssetInfoMapper assetInfoMapper;

    @Mock
    private IApprovalEngine approvalEngine;

    @InjectMocks
    private AssetRealEstateDisposalServiceImpl service;

    @Test
    void shouldRejectDisposalForFixedAsset() {
        AssetRealEstateDisposal request = buildRequest();
        request.setAssetId(9101L);
        when(assetInfoMapper.selectAssetInfoByAssetId(9101L)).thenReturn(buildAssetInfo(9101L, "FA-9101", "1"));

        ServiceException exception = assertThrows(ServiceException.class, () -> service.insertDisposal(request));

        assertEquals("固定资产不能发起不动产注销/处置", exception.getMessage());
        verify(disposalMapper, never()).insertDisposal(any(AssetRealEstateDisposal.class));
        verify(approvalEngine, never()).startProcess(anyString(), anyString());
    }

    @Test
    void shouldRejectDisposalWithoutAssetId() {
        AssetRealEstateDisposal request = buildRequest();
        request.setAssetId(null);
        request.setAssetNo("RE-9102");

        ServiceException exception = assertThrows(ServiceException.class, () -> service.insertDisposal(request));

        assertEquals("不动产业务统一要求由资产台账带入资产主键", exception.getMessage());
        verify(disposalMapper, never()).insertDisposal(any(AssetRealEstateDisposal.class));
        verify(approvalEngine, never()).startProcess(anyString(), anyString());
    }

    @Test
    void shouldCreateDisposalForRealEstateAndStartApproval() {
        AssetRealEstateDisposal request = buildRequest();
        when(assetInfoMapper.selectAssetInfoByAssetId(9102L)).thenReturn(buildRealEstateAssetInfo(9102L, "RE-9102", "1"));
        when(disposalMapper.insertDisposal(any(AssetRealEstateDisposal.class))).thenReturn(1);

        int rows = service.insertDisposal(request);

        assertEquals(1, rows);
        assertNotNull(request.getAssetId());
        assertEquals("RE-9102", request.getAssetNo());
        assertEquals("1", request.getOldAssetStatus());
        assertEquals("pending", request.getStatus());
        assertEquals("pending", request.getWfStatus());
        assertNotNull(request.getCreateTime());

        ArgumentCaptor<AssetRealEstateDisposal> captor = ArgumentCaptor.forClass(AssetRealEstateDisposal.class);
        verify(disposalMapper).insertDisposal(captor.capture());
        assertEquals("6", captor.getValue().getTargetAssetStatus());
        assertEquals("pending", captor.getValue().getStatus());
        assertEquals("pending", captor.getValue().getWfStatus());

        verify(approvalEngine).startProcess("RED-20260315-001", "asset_real_estate_disposal");
        verify(assetInfoMapper, never()).updateAssetInfo(any(AssetInfo.class));
    }

    @Test
    void shouldLoadDisposalMapperXmlFromClasspath() {
        URL resource = getClass().getClassLoader().getResource("mapper/asset/AssetRealEstateDisposalMapper.xml");
        assertNotNull(resource);
    }

    private AssetRealEstateDisposal buildRequest() {
        AssetRealEstateDisposal request = new AssetRealEstateDisposal();
        request.setDisposalNo("RED-20260315-001");
        request.setAssetId(9102L);
        request.setDisposalType("cancel");
        request.setTargetAssetStatus("6");
        request.setReason("注销处置");
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
