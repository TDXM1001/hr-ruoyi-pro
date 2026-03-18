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
import com.ruoyi.asset.domain.AssetChangeLog;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.bo.AssetLedgerBo;
import com.ruoyi.asset.enums.AssetBizType;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.common.exception.ServiceException;

/**
 * 资产台账服务测试。
 *
 * <p>该测试用于固定资产一期台账建账的关键业务规则，
 * 确保后续控制器和前端接入时，资产编号后端生成、编辑冻结、状态留痕三项基础能力稳定可用。</p>
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
    @DisplayName("新增台账时应该以后端规则生成资产编码")
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
    @DisplayName("新增台账时未填写资产编码也应该自动生成年度流水编码")
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
    @DisplayName("新增台账时如果后端生成的资产编码已存在应该被拒绝")
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
    @DisplayName("修改台账时应该保留原资产编码与原状态并记录修改日志")
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
    @DisplayName("修改历史台账时如果原资产编码为空应该补齐后端生成编码")
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

    private AssetLedgerBo buildBo(String assetCode)
    {
        return buildBoWithId(null, assetCode);
    }

    private AssetLedgerBo buildBoWithId(Long assetId, String assetCode)
    {
        AssetLedgerBo bo = new AssetLedgerBo();
        bo.setAssetId(assetId);
        bo.setAssetCode(assetCode);
        bo.setAssetName("办公笔记本");
        bo.setAssetType("FIXED");
        bo.setCategoryId(1L);
        bo.setSourceType("MANUAL");
        bo.setAcquireType("PURCHASE");
        bo.setOwnerDeptId(100L);
        bo.setUseDeptId(101L);
        bo.setResponsibleUserId(1L);
        bo.setLocationName("研发部A区");
        bo.setOriginalValue(new BigDecimal("8999.00"));
        bo.setAcquisitionDate(new Date());
        bo.setEnableDate(new Date());
        bo.setRemark("测试数据");
        return bo;
    }

    private AssetLedger buildLedger(Long assetId, String assetCode)
    {
        AssetLedger assetLedger = new AssetLedger();
        assetLedger.setAssetId(assetId);
        assetLedger.setAssetCode(assetCode);
        assetLedger.setAssetName("办公笔记本");
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
