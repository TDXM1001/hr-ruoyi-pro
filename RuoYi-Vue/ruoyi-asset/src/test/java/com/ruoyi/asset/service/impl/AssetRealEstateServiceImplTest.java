package com.ruoyi.asset.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.time.Year;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.asset.domain.AssetChangeLog;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.AssetRealEstateProfile;
import com.ruoyi.asset.domain.bo.AssetRealEstateBo;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.asset.mapper.AssetRealEstateMapper;

/**
 * 不动产档案服务测试。
 *
 * <p>从资产管理者视角验证：不动产建档不是只写扩展表，
 * 而是必须同步落统一台账、扩展档案，并提供独立详情查询。</p>
 */
@ExtendWith(MockitoExtension.class)
class AssetRealEstateServiceImplTest
{
    @Mock
    private AssetRealEstateMapper assetRealEstateMapper;

    @Mock
    private AssetLedgerMapper assetLedgerMapper;

    @Mock
    private AssetChangeLogMapper assetChangeLogMapper;

    @InjectMocks
    private AssetRealEstateServiceImpl service;

    @Test
    @DisplayName("应按资产ID查询不动产档案详情")
    void shouldQueryRealEstateProfileByAssetId()
    {
        when(assetRealEstateMapper.selectByAssetId(1001L)).thenReturn(buildProfile(1001L));

        AssetRealEstateProfile profile = service.selectByAssetId(1001L);

        assertNotNull(profile);
        assertEquals("CERT-001", profile.getOwnershipCertNo());
        assertEquals(new BigDecimal("1520.36"), profile.getBuildingArea());
    }

    @Test
    @DisplayName("新建不动产档案时应同时写入统一台账与扩展档案")
    void shouldCreateLedgerAndProfileTogether()
    {
        String expectedCode = buildAssetCode(2);
        when(assetLedgerMapper.selectMaxAssetCodeByPrefix(buildAssetCodePrefix())).thenReturn(buildAssetCode(1));
        when(assetLedgerMapper.selectByAssetCode(expectedCode)).thenReturn(null);
        when(assetLedgerMapper.insertAsset(any())).thenAnswer(invocation ->
        {
            AssetLedger assetLedger = invocation.getArgument(0);
            assetLedger.setAssetId(20002L);
            return 1;
        });
        when(assetRealEstateMapper.insertProfile(any())).thenReturn(1);

        Long assetId = service.createAsset(buildBo(null), "admin");

        assertEquals(20002L, assetId);
        verify(assetLedgerMapper).insertAsset(any(AssetLedger.class));
        verify(assetRealEstateMapper).insertProfile(any(AssetRealEstateProfile.class));
        verify(assetChangeLogMapper).insertAssetChangeLog(any(AssetChangeLog.class));
    }

    @Test
    @DisplayName("编辑不动产档案时应同步更新统一台账与扩展档案")
    void shouldUpdateLedgerAndProfileTogether()
    {
        AssetLedger current = new AssetLedger();
        current.setAssetId(20001L);
        current.setAssetCode(buildAssetCode(1));
        current.setAssetType("REAL_ESTATE");
        current.setAssetStatus("IN_USE");
        AssetRealEstateProfile currentProfile = new AssetRealEstateProfile();
        currentProfile.setProfileId(30001L);
        currentProfile.setAssetId(20001L);
        when(assetLedgerMapper.selectAssetById(20001L)).thenReturn(current);
        when(assetLedgerMapper.selectByAssetCode(buildAssetCode(1))).thenReturn(current);
        when(assetRealEstateMapper.selectByAssetId(20001L)).thenReturn(currentProfile);
        when(assetLedgerMapper.updateAsset(any())).thenReturn(1);
        when(assetRealEstateMapper.updateProfile(any())).thenReturn(1);

        int rows = service.updateAsset(buildBo(20001L), "admin");

        assertEquals(1, rows);
        verify(assetLedgerMapper).updateAsset(any(AssetLedger.class));
        verify(assetRealEstateMapper).updateProfile(any(AssetRealEstateProfile.class));
    }

    private AssetRealEstateProfile buildProfile(Long assetId)
    {
        AssetRealEstateProfile profile = new AssetRealEstateProfile();
        profile.setAssetId(assetId);
        profile.setOwnershipCertNo("CERT-001");
        profile.setLandUseType("办公");
        profile.setBuildingArea(new BigDecimal("1520.36"));
        return profile;
    }

    private AssetRealEstateBo buildBo(Long assetId)
    {
        AssetRealEstateBo bo = new AssetRealEstateBo();
        bo.setAssetId(assetId);
        bo.setAssetName("深圳研发办公楼A座");
        bo.setCategoryId(1101L);
        bo.setSourceType("MANUAL");
        bo.setAcquireType("PURCHASE");
        bo.setAssetStatus("IN_USE");
        bo.setOwnerDeptId(103L);
        bo.setUseDeptId(103L);
        bo.setResponsibleUserId(1L);
        bo.setLocationName("深圳南山研发园区A座");
        bo.setOriginalValue(new BigDecimal("12500000.00"));
        bo.setOwnershipCertNo("粤(2024)深圳市不动产权第A0001号");
        bo.setLandUseType("科研办公");
        bo.setBuildingArea(new BigDecimal("18650.50"));
        bo.setRemark("不动产档案测试数据");
        return bo;
    }

    private String buildAssetCodePrefix()
    {
        return "RE-" + Year.now().getValue() + "-";
    }

    private String buildAssetCode(int sequence)
    {
        return buildAssetCodePrefix() + String.format("%04d", sequence);
    }
}
