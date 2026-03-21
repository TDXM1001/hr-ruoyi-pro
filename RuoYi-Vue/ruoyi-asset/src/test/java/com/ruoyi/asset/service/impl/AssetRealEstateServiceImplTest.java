package com.ruoyi.asset.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.time.Year;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.asset.domain.AssetChangeLog;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.AssetRealEstateOccupancyOrder;
import com.ruoyi.asset.domain.AssetRealEstateProfile;
import com.ruoyi.asset.domain.bo.AssetRealEstateOccupancyBo;
import com.ruoyi.asset.domain.bo.AssetRealEstateOccupancyReleaseBo;
import com.ruoyi.asset.domain.vo.AssetLedgerLifecycleVo;
import com.ruoyi.asset.domain.bo.AssetRealEstateBo;
import com.ruoyi.asset.domain.vo.AssetRealEstateOccupancyVo;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetDisposalMapper;
import com.ruoyi.asset.mapper.AssetHandoverItemMapper;
import com.ruoyi.asset.mapper.AssetInventoryMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.asset.mapper.AssetRealEstateOccupancyMapper;
import com.ruoyi.asset.mapper.AssetRealEstateMapper;
import com.ruoyi.asset.mapper.AssetRectificationMapper;
import com.ruoyi.common.exception.ServiceException;

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

    @Mock
    private AssetHandoverItemMapper assetHandoverItemMapper;

    @Mock
    private AssetInventoryMapper assetInventoryMapper;

    @Mock
    private AssetDisposalMapper assetDisposalMapper;

    @Mock
    private AssetRectificationMapper assetRectificationMapper;

    @Mock
    private AssetRealEstateOccupancyMapper assetRealEstateOccupancyMapper;

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

    @Test
    @DisplayName("发起占用时应新增占用单并回写当前使用信息")
    void shouldCreateOccupancyAndSyncUsageSnapshot()
    {
        when(assetLedgerMapper.selectAssetById(20001L)).thenReturn(buildCurrentLedger("IDLE"));
        when(assetRealEstateOccupancyMapper.selectActiveOccupancyByAssetId(20001L)).thenReturn(null);
        when(assetRealEstateOccupancyMapper.selectMaxOccupancyNoByPrefix(buildOccupancyNoPrefix()))
            .thenReturn(buildOccupancyNo(1));
        when(assetRealEstateOccupancyMapper.insertOccupancy(any())).thenAnswer(invocation ->
        {
            AssetRealEstateOccupancyOrder order = invocation.getArgument(0);
            order.setOccupancyId(50002L);
            return 1;
        });
        when(assetLedgerMapper.updateAssetUsageInfo(any())).thenReturn(1);

        Long occupancyId = service.createOccupancy(20001L, buildOccupancyBo(105L, 3L, "深圳南山研发园-B座"), "admin");

        assertEquals(50002L, occupancyId);
        verify(assetRealEstateOccupancyMapper).insertOccupancy(any(AssetRealEstateOccupancyOrder.class));
        verify(assetLedgerMapper).updateAssetUsageInfo(any(AssetLedger.class));
        verify(assetChangeLogMapper).insertAssetChangeLog(any(AssetChangeLog.class));
    }

    @Test
    @DisplayName("存在有效占用时不允许重复发起占用")
    void shouldRejectCreateWhenActiveOccupancyExists()
    {
        when(assetLedgerMapper.selectAssetById(20001L)).thenReturn(buildCurrentLedger("IN_USE"));
        when(assetRealEstateOccupancyMapper.selectActiveOccupancyByAssetId(20001L))
            .thenReturn(buildActiveOccupancy(90001L, 20001L));

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.createOccupancy(20001L, buildOccupancyBo(105L, 3L, "深圳南山研发园-B座"), "admin"));

        assertEquals("当前资产已存在有效占用", exception.getMessage());
    }

    @Test
    @DisplayName("变更占用时应关闭旧占用并新建新占用")
    void shouldChangeOccupancyByClosingOldAndCreatingNew()
    {
        when(assetLedgerMapper.selectAssetById(20001L)).thenReturn(buildCurrentLedger("IN_USE"));
        when(assetRealEstateOccupancyMapper.selectOccupancyById(90001L))
            .thenReturn(buildActiveOccupancy(90001L, 20001L));
        when(assetRealEstateOccupancyMapper.selectMaxOccupancyNoByPrefix(buildOccupancyNoPrefix()))
            .thenReturn(buildOccupancyNo(2));
        when(assetRealEstateOccupancyMapper.updateOccupancy(any())).thenReturn(1);
        when(assetRealEstateOccupancyMapper.insertOccupancy(any())).thenAnswer(invocation ->
        {
            AssetRealEstateOccupancyOrder order = invocation.getArgument(0);
            order.setOccupancyId(90002L);
            return 1;
        });
        when(assetLedgerMapper.updateAssetUsageInfo(any())).thenReturn(1);

        Long newOccupancyId = service.changeOccupancy(20001L, 90001L,
            buildOccupancyBo(106L, 4L, "深圳南山研发园-C座"), "admin");

        assertEquals(90002L, newOccupancyId);
        verify(assetRealEstateOccupancyMapper).updateOccupancy(any(AssetRealEstateOccupancyOrder.class));
        verify(assetRealEstateOccupancyMapper).insertOccupancy(any(AssetRealEstateOccupancyOrder.class));
        verify(assetLedgerMapper).updateAssetUsageInfo(any(AssetLedger.class));
        verify(assetChangeLogMapper).insertAssetChangeLog(any(AssetChangeLog.class));
    }

    @Test
    @DisplayName("释放占用后应将资产回写为闲置中")
    void shouldReleaseOccupancyAndSetAssetIdle()
    {
        when(assetLedgerMapper.selectAssetById(20001L)).thenReturn(buildCurrentLedger("IN_USE"));
        when(assetRealEstateOccupancyMapper.selectOccupancyById(90001L))
            .thenReturn(buildActiveOccupancy(90001L, 20001L));
        when(assetRealEstateOccupancyMapper.updateOccupancy(any())).thenReturn(1);
        when(assetLedgerMapper.updateAssetUsageInfo(any())).thenReturn(1);

        int rows = service.releaseOccupancy(20001L, 90001L, buildOccupancyReleaseBo(), "admin");

        assertEquals(1, rows);
        ArgumentCaptor<AssetLedger> assetCaptor = ArgumentCaptor.forClass(AssetLedger.class);
        verify(assetLedgerMapper).updateAssetUsageInfo(assetCaptor.capture());
        assertEquals("IDLE", assetCaptor.getValue().getAssetStatus());
        assertEquals(null, assetCaptor.getValue().getUseDeptId());
        assertEquals(null, assetCaptor.getValue().getResponsibleUserId());
        assertEquals(null, assetCaptor.getValue().getLocationName());
    }

    @Test
    @DisplayName("查询生命周期时应带出占用记录")
    void shouldIncludeOccupancyRecordsInLifecycleDetail()
    {
        when(assetRealEstateMapper.selectDetailByAssetId(20001L)).thenReturn(buildRealEstateVo());
        when(assetLedgerMapper.selectAssetLedgerById(20001L)).thenReturn(new com.ruoyi.asset.domain.vo.AssetLedgerVo());
        when(assetRealEstateOccupancyMapper.selectOccupancyListByAssetId(20001L))
            .thenReturn(java.util.List.of(buildOccupancyVo(90001L, 20001L)));

        AssetLedgerLifecycleVo detail = service.selectLifecycleByAssetId(20001L);

        assertEquals(1, detail.getOccupancyRecords().size());
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

    private AssetLedger buildCurrentLedger(String assetStatus)
    {
        AssetLedger ledger = new AssetLedger();
        ledger.setAssetId(20001L);
        ledger.setAssetCode(buildAssetCode(1));
        ledger.setAssetType("REAL_ESTATE");
        ledger.setAssetStatus(assetStatus);
        ledger.setUseDeptId(103L);
        ledger.setResponsibleUserId(1L);
        ledger.setLocationName("深圳南山研发园-A座");
        return ledger;
    }

    private AssetRealEstateOccupancyOrder buildActiveOccupancy(Long occupancyId, Long assetId)
    {
        AssetRealEstateOccupancyOrder order = new AssetRealEstateOccupancyOrder();
        order.setOccupancyId(occupancyId);
        order.setAssetId(assetId);
        order.setOccupancyNo(buildOccupancyNo(1));
        order.setUseDeptId(103L);
        order.setResponsibleUserId(1L);
        order.setLocationName("深圳南山研发园-A座");
        order.setOccupancyStatus("ACTIVE");
        order.setChangeReason("初始占用");
        return order;
    }

    private AssetRealEstateOccupancyBo buildOccupancyBo(Long useDeptId, Long responsibleUserId, String locationName)
    {
        AssetRealEstateOccupancyBo bo = new AssetRealEstateOccupancyBo();
        bo.setUseDeptId(useDeptId);
        bo.setResponsibleUserId(responsibleUserId);
        bo.setLocationName(locationName);
        bo.setStartDate("2026-03-22");
        bo.setChangeReason("资产级占用闭环点测");
        return bo;
    }

    private AssetRealEstateOccupancyReleaseBo buildOccupancyReleaseBo()
    {
        AssetRealEstateOccupancyReleaseBo bo = new AssetRealEstateOccupancyReleaseBo();
        bo.setEndDate("2026-03-25");
        bo.setReleaseReason("部门搬离，释放占用");
        return bo;
    }

    private com.ruoyi.asset.domain.vo.AssetRealEstateVo buildRealEstateVo()
    {
        com.ruoyi.asset.domain.vo.AssetRealEstateVo vo = new com.ruoyi.asset.domain.vo.AssetRealEstateVo();
        vo.setAssetId(20001L);
        return vo;
    }

    private AssetRealEstateOccupancyVo buildOccupancyVo(Long occupancyId, Long assetId)
    {
        AssetRealEstateOccupancyVo vo = new AssetRealEstateOccupancyVo();
        vo.setOccupancyId(occupancyId);
        vo.setAssetId(assetId);
        vo.setOccupancyNo(buildOccupancyNo(1));
        vo.setOccupancyStatus("ACTIVE");
        return vo;
    }

    private String buildOccupancyNoPrefix()
    {
        return "OCC-" + Year.now().getValue() + "-";
    }

    private String buildOccupancyNo(int sequence)
    {
        return buildOccupancyNoPrefix() + String.format("%04d", sequence);
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
