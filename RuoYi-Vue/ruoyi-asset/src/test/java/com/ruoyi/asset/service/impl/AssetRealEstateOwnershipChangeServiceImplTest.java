package com.ruoyi.asset.service.impl;

import java.net.URL;
import java.util.Date;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetRealEstate;
import com.ruoyi.asset.domain.AssetRealEstateOwnershipChange;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.mapper.AssetRealEstateMapper;
import com.ruoyi.asset.mapper.AssetRealEstateOwnershipChangeMapper;
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
 * 不动产权属变更服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AssetRealEstateOwnershipChangeServiceImplTest {

    @Mock
    private AssetRealEstateOwnershipChangeMapper ownershipChangeMapper;

    @Mock
    private AssetInfoMapper assetInfoMapper;

    @Mock
    private AssetRealEstateMapper assetRealEstateMapper;

    @Mock
    private IApprovalEngine approvalEngine;

    @InjectMocks
    private AssetRealEstateOwnershipChangeServiceImpl service;

    @Test
    void shouldRejectOwnershipChangeForFixedAsset() {
        AssetRealEstateOwnershipChange request = buildRequest();
        request.setAssetId(9001L);
        when(assetInfoMapper.selectAssetInfoByAssetId(9001L)).thenReturn(buildAssetInfo(9001L, "FA-9001", "1"));

        ServiceException exception = assertThrows(ServiceException.class, () -> service.insertOwnershipChange(request));

        assertEquals("固定资产不能发起不动产权属变更", exception.getMessage());
        verify(ownershipChangeMapper, never()).insertOwnershipChange(any(AssetRealEstateOwnershipChange.class));
        verify(approvalEngine, never()).startProcess(anyString(), anyString());
    }

    @Test
    void shouldRejectOwnershipChangeWithoutAssetId() {
        AssetRealEstateOwnershipChange request = buildRequest();
        request.setAssetId(null);
        request.setAssetNo("RE-9002");

        ServiceException exception = assertThrows(ServiceException.class, () -> service.insertOwnershipChange(request));

        assertEquals("不动产业务统一要求由资产台账带入资产主键", exception.getMessage());
        verify(ownershipChangeMapper, never()).insertOwnershipChange(any(AssetRealEstateOwnershipChange.class));
        verify(approvalEngine, never()).startProcess(anyString(), anyString());
    }

    @Test
    void shouldCreateOwnershipChangeForRealEstateAndStartApproval() {
        AssetRealEstateOwnershipChange request = buildRequest();
        AssetRealEstate realEstate = new AssetRealEstate();
        realEstate.setAssetId(9002L);
        realEstate.setRightsHolder("旧权利人");
        realEstate.setPropertyCertNo("CERT-OLD");
        realEstate.setRegistrationDate(new Date(1741996800000L));

        when(assetInfoMapper.selectAssetInfoByAssetId(9002L)).thenReturn(buildRealEstateAssetInfo(9002L, "RE-9002", "1"));
        when(assetRealEstateMapper.selectAssetRealEstateByAssetId(9002L)).thenReturn(realEstate);
        when(ownershipChangeMapper.insertOwnershipChange(any(AssetRealEstateOwnershipChange.class))).thenReturn(1);

        int rows = service.insertOwnershipChange(request);

        assertEquals(1, rows);
        assertNotNull(request.getAssetId());
        assertEquals("RE-9002", request.getAssetNo());
        assertEquals("pending", request.getStatus());
        assertEquals("pending", request.getWfStatus());
        assertEquals("旧权利人", request.getOldRightsHolder());
        assertEquals("CERT-OLD", request.getOldPropertyCertNo());
        assertNotNull(request.getCreateTime());

        ArgumentCaptor<AssetRealEstateOwnershipChange> captor = ArgumentCaptor.forClass(AssetRealEstateOwnershipChange.class);
        verify(ownershipChangeMapper).insertOwnershipChange(captor.capture());
        assertEquals("张三", captor.getValue().getTargetRightsHolder());
        assertEquals("pending", captor.getValue().getStatus());
        assertEquals("pending", captor.getValue().getWfStatus());

        verify(approvalEngine).startProcess("OWN-20260315-001", "asset_real_estate_ownership_change");
        verify(assetInfoMapper, never()).updateAssetInfo(any(AssetInfo.class));
        verify(assetRealEstateMapper, never()).updateAssetRealEstate(any(AssetRealEstate.class));
    }

    @Test
    void shouldLoadOwnershipChangeMapperXmlFromClasspath() {
        URL resource = getClass().getClassLoader().getResource("mapper/asset/AssetRealEstateOwnershipChangeMapper.xml");
        assertNotNull(resource);
    }

    private AssetRealEstateOwnershipChange buildRequest() {
        AssetRealEstateOwnershipChange request = new AssetRealEstateOwnershipChange();
        request.setOwnershipChangeNo("OWN-20260315-001");
        request.setAssetId(9002L);
        request.setTargetRightsHolder("张三");
        request.setTargetPropertyCertNo("CERT-NEW");
        request.setTargetRegistrationDate(new Date(1742083200000L));
        request.setReason("权属变更");
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
