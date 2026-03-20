package com.ruoyi.asset.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.asset.domain.AssetRealEstateProfile;
import com.ruoyi.asset.mapper.AssetRealEstateMapper;

/**
 * 不动产档案服务测试。
 *
 * <p>从资产管理员视角验证：可基于资产ID拉取权属档案用于详情核查。</p>
 *
 * @author Codex
 */
@ExtendWith(MockitoExtension.class)
class AssetRealEstateServiceImplTest
{
    @Mock
    private AssetRealEstateMapper assetRealEstateMapper;

    @InjectMocks
    private AssetRealEstateServiceImpl service;

    @Test
    @DisplayName("应按资产ID查询不动产权属档案")
    void shouldQueryRealEstateProfileByAssetId()
    {
        when(assetRealEstateMapper.selectByAssetId(1001L)).thenReturn(buildProfile(1001L));

        AssetRealEstateProfile profile = service.selectByAssetId(1001L);

        assertNotNull(profile);
        assertEquals("CERT-001", profile.getOwnershipCertNo());
        assertEquals(new BigDecimal("1520.36"), profile.getBuildingArea());
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
}

