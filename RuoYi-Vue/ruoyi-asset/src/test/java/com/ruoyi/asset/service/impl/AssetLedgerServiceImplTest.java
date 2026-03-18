package com.ruoyi.asset.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.time.Year;
import java.util.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.bo.AssetLedgerBo;
import com.ruoyi.asset.enums.AssetBizType;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 资产台账服务实现测试。
 *
 * <p>重点覆盖资产编号生成规则，以及台账编辑不得绕过交接单直接改使用关系的约束。</p>
 *
 * @author Codex
 */
@ExtendWith(MockitoExtension.class)
class AssetLedgerServiceImplTest
{
    @Mock
    private AssetLedgerMapper assetLedgerMapper;

    @Mock
    private AssetChangeLogMapper assetChangeLogMapper;

    @InjectMocks
    private AssetLedgerServiceImpl service;

    @Test
    @DisplayName("新增台账时应生成正式资产编号")
    void shouldGenerateOfficialAssetCodeOnCreate()
    {
        String expectedCode = buildAssetCode(2);
        when(assetLedgerMapper.selectMaxAssetCodeByPrefix(buildAssetCodePrefix())).thenReturn(buildAssetCode(1));
        when(assetLedgerMapper.selectByAssetCode(expectedCode)).thenReturn(null);
        when(assetLedgerMapper.insertAsset(any())).thenAnswer(invocation ->
        {
            AssetLedger assetLedger = invocation.getArgument(0);
            assetLedger.setAssetId(2L);
            return 1;
        });

        Long assetId = service.createAsset(buildBo("USER-INPUT"), "admin");

        assertEquals(2L, assetId);
        verify(assetLedgerMapper).insertAsset(argThat(asset ->
            expectedCode.equals(asset.getAssetCode())
                && AssetStatus.IN_LEDGER.getCode().equals(asset.getAssetStatus())
                && "admin".equals(asset.getCreateBy())));
        verify(assetChangeLogMapper).insertAssetChangeLog(argThat(log ->
            AssetBizType.LEDGER_CREATE.getCode().equals(log.getBizType())
                && AssetStatus.IN_LEDGER.getCode().equals(log.getAfterStatus())
                && "admin".equals(log.getOperateBy())));
    }

    @Test
    @DisplayName("新增台账时即使前端未传编号也应自动生成")
    void shouldGenerateAssetCodeWhenCreateWithoutInputCode()
    {
        String expectedCode = buildAssetCode(10);
        when(assetLedgerMapper.selectMaxAssetCodeByPrefix(buildAssetCodePrefix())).thenReturn(buildAssetCode(9));
        when(assetLedgerMapper.selectByAssetCode(expectedCode)).thenReturn(null);
        when(assetLedgerMapper.insertAsset(any())).thenAnswer(invocation ->
        {
            AssetLedger assetLedger = invocation.getArgument(0);
            assetLedger.setAssetId(6L);
            return 1;
        });

        Long assetId = service.createAsset(buildBo(""), "admin");

        assertEquals(6L, assetId);
        verify(assetLedgerMapper).insertAsset(argThat(asset ->
            expectedCode.equals(asset.getAssetCode())
                && asset.getAssetCode().matches("FA-" + Year.now().getValue() + "-\\d{4}")));
    }

    @Test
    @DisplayName("新增台账时若生成编号已存在应直接拒绝")
    void shouldRejectDuplicateGeneratedAssetCode()
    {
        String duplicatedCode = buildAssetCode(2);
        AssetLedger existed = new AssetLedger();
        existed.setAssetId(99L);
        existed.setAssetCode(duplicatedCode);
        when(assetLedgerMapper.selectMaxAssetCodeByPrefix(buildAssetCodePrefix())).thenReturn(buildAssetCode(1));
        when(assetLedgerMapper.selectByAssetCode(duplicatedCode)).thenReturn(existed);

        ServiceException exception = assertThrows(ServiceException.class,
            () -> service.createAsset(buildBo("FA-0001"), "admin"));

        assertEquals("资产编码已存在", exception.getMessage());
    }

    @Test
    @DisplayName("修改台账时应继续沿用当前资产编码和状态")
    void shouldKeepCurrentAssetCodeAndStatusAfterUpdate()
    {
        AssetLedger current = buildLedger(3L, buildAssetCode(4));
        current.setAssetStatus(AssetStatus.IDLE.getCode());
        when(assetLedgerMapper.selectAssetById(3L)).thenReturn(current);
        when(assetLedgerMapper.selectByAssetCode(buildAssetCode(4))).thenReturn(current);
        when(assetLedgerMapper.updateAsset(any())).thenReturn(1);

        int rows = service.updateAsset(buildBoWithId(3L, "FRONT-END-CHANGED"), "admin");

        assertEquals(1, rows);
        verify(assetLedgerMapper).updateAsset(argThat(asset ->
            Long.valueOf(3L).equals(asset.getAssetId())
                && buildAssetCode(4).equals(asset.getAssetCode())
                && AssetStatus.IDLE.getCode().equals(asset.getAssetStatus())
                && "admin".equals(asset.getUpdateBy())));
        verify(assetChangeLogMapper).insertAssetChangeLog(argThat(log ->
            AssetBizType.LEDGER_UPDATE.getCode().equals(log.getBizType())
                && AssetStatus.IDLE.getCode().equals(log.getBeforeStatus())
                && AssetStatus.IDLE.getCode().equals(log.getAfterStatus())));
    }

    @Test
    @DisplayName("修改台账时如果当前资产编号为空应重新生成")
    void shouldGenerateAssetCodeWhenCurrentAssetCodeIsBlankOnUpdate()
    {
        AssetLedger current = buildLedger(8L, "");
        current.setAssetStatus(AssetStatus.IN_LEDGER.getCode());
        when(assetLedgerMapper.selectAssetById(8L)).thenReturn(current);
        when(assetLedgerMapper.selectMaxAssetCodeByPrefix(buildAssetCodePrefix())).thenReturn(buildAssetCode(12));
        when(assetLedgerMapper.selectByAssetCode(buildAssetCode(13))).thenReturn(null);
        when(assetLedgerMapper.updateAsset(any())).thenReturn(1);

        int rows = service.updateAsset(buildBoWithId(8L, "IGNORE-ME"), "admin");

        assertEquals(1, rows);
        verify(assetLedgerMapper).updateAsset(argThat(asset ->
            buildAssetCode(13).equals(asset.getAssetCode())
                && Long.valueOf(8L).equals(asset.getAssetId())));
    }

    @Test
    @DisplayName("修改台账时不允许直接改使用部门责任人和位置")
    void shouldPreserveUsageFieldsWhenUpdatingLedger()
    {
        AssetLedger current = buildLedger(9L, buildAssetCode(15));
        current.setAssetType("FIXED");
        current.setAssetStatus(AssetStatus.IN_USE.getCode());
        current.setUseDeptId(300L);
        current.setResponsibleUserId(30L);
        current.setLocationName("原始位置");
        when(assetLedgerMapper.selectAssetById(9L)).thenReturn(current);
        when(assetLedgerMapper.selectByAssetCode(buildAssetCode(15))).thenReturn(current);
        when(assetLedgerMapper.updateAsset(any())).thenReturn(1);

        AssetLedgerBo bo = buildBoWithId(9L, "CLIENT-INPUT");
        bo.setUseDeptId(999L);
        bo.setResponsibleUserId(88L);
        bo.setLocationName("前端试图直接修改的位置");

        int rows = service.updateAsset(bo, "admin");

        assertEquals(1, rows);
        verify(assetLedgerMapper).updateAsset(argThat(asset ->
            Long.valueOf(9L).equals(asset.getAssetId())
                && Long.valueOf(300L).equals(asset.getUseDeptId())
                && Long.valueOf(30L).equals(asset.getResponsibleUserId())
                && "原始位置".equals(asset.getLocationName())
                && AssetStatus.IN_USE.getCode().equals(asset.getAssetStatus())));
    }

    private AssetLedgerBo buildBo(String assetCode)
    {
        return buildBoWithId(null, assetCode);
    }

    private AssetLedgerBo buildBoWithId(Long assetId, String assetCode)
    {
        AssetLedgerBo bo = new AssetLedgerBo();
        bo.setAssetId(assetId);
        bo.setAssetCode(assetCode);
        bo.setAssetName("测试资产");
        bo.setAssetType("FIXED");
        bo.setCategoryId(1L);
        bo.setSourceType("MANUAL");
        bo.setAcquireType("PURCHASE");
        bo.setOwnerDeptId(100L);
        bo.setUseDeptId(101L);
        bo.setResponsibleUserId(1L);
        bo.setLocationName("行政楼-A座");
        bo.setOriginalValue(new BigDecimal("8999.00"));
        bo.setAcquisitionDate(new Date());
        bo.setEnableDate(new Date());
        bo.setRemark("测试台账");
        return bo;
    }

    private AssetLedger buildLedger(Long assetId, String assetCode)
    {
        AssetLedger assetLedger = new AssetLedger();
        assetLedger.setAssetId(assetId);
        assetLedger.setAssetCode(assetCode);
        assetLedger.setAssetName("测试资产");
        assetLedger.setAssetStatus(AssetStatus.IN_LEDGER.getCode());
        return assetLedger;
    }

    private String buildAssetCodePrefix()
    {
        return "FA-" + Year.now().getValue() + "-";
    }

    private String buildAssetCode(int sequence)
    {
        return buildAssetCodePrefix() + String.format("%04d", sequence);
    }
}
