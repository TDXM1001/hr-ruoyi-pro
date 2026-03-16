package com.ruoyi.asset.service.impl;

import java.math.BigDecimal;
import java.util.Collections;
import com.ruoyi.asset.domain.AssetFinance;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.dto.AssetUpdateReq;
import com.ruoyi.asset.mapper.AssetAttachmentMapper;
import com.ruoyi.asset.mapper.AssetAttrValueMapper;
import com.ruoyi.asset.mapper.AssetDepreciationLogMapper;
import com.ruoyi.asset.mapper.AssetFinanceMapper;
import com.ruoyi.asset.mapper.AssetRealEstateMapper;
import com.ruoyi.asset.mapper.AssetTimelineMapper;
import com.ruoyi.asset.service.IAssetFinanceService;
import com.ruoyi.asset.service.IAssetInfoService;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 资产聚合服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AssetAggregateServiceImplTest {
    @Mock
    private IAssetInfoService assetInfoService;

    @Mock
    private AssetFinanceMapper assetFinanceMapper;

    @Mock
    private AssetRealEstateMapper assetRealEstateMapper;

    @Mock
    private AssetAttrValueMapper assetAttrValueMapper;

    @Mock
    private AssetAttachmentMapper assetAttachmentMapper;

    @Mock
    private AssetDepreciationLogMapper assetDepreciationLogMapper;

    @Mock
    private AssetTimelineMapper assetTimelineMapper;

    @Mock
    private IAssetFinanceService assetFinanceService;

    @InjectMocks
    private AssetAggregateServiceImpl assetAggregateService;

    @Test
    void shouldRejectFinancialBaseChangeAfterDepreciationStarted() {
        AssetUpdateReq updateReq = new AssetUpdateReq();
        AssetInfo basicInfo = new AssetInfo();
        basicInfo.setAssetId(1001L);
        basicInfo.setAssetNo("A-1001");
        basicInfo.setAssetName("测试资产");
        basicInfo.setCategoryId(10L);
        basicInfo.setAssetType("1");
        updateReq.setBasicInfo(basicInfo);

        AssetFinance updateFinance = new AssetFinance();
        updateFinance.setOriginalValue(new BigDecimal("12000.00"));
        updateFinance.setDepreciationMethod("2");
        updateFinance.setUsefulLifeMonth(72);
        updateReq.setFinanceInfo(updateFinance);
        updateReq.setDynamicAttrs(Collections.emptyList());
        updateReq.setAttachments(Collections.emptyList());

        AssetInfo currentInfo = new AssetInfo();
        currentInfo.setAssetId(1001L);
        currentInfo.setAssetNo("A-1001");
        when(assetInfoService.selectAssetInfoByAssetId(1001L)).thenReturn(currentInfo);
        when(assetInfoService.selectAssetInfoByAssetNo("A-1001")).thenReturn(currentInfo);

        AssetFinance currentFinance = new AssetFinance();
        currentFinance.setAssetId(1001L);
        currentFinance.setOriginalValue(new BigDecimal("10000.00"));
        currentFinance.setDepreciationMethod("1");
        currentFinance.setUsefulLifeMonth(60);
        currentFinance.setLastDepreciationPeriod("2026-02");
        currentFinance.setAccumulatedDepreciation(new BigDecimal("500.00"));
        when(assetFinanceMapper.selectAssetFinanceByAssetId(1001L)).thenReturn(currentFinance);

        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> assetAggregateService.updateAsset(updateReq)
        );

        assertEquals("资产已开始折旧，不能直接修改财务基础数据", exception.getMessage());
        verify(assetInfoService).selectAssetInfoByAssetId(1001L);
        verify(assetFinanceMapper).selectAssetFinanceByAssetId(1001L);
    }

    @Test
    void shouldSkipFinanceRewriteWhenDepreciationAlreadyStartedAndBaseDataUnchanged() {
        AssetUpdateReq updateReq = new AssetUpdateReq();
        AssetInfo basicInfo = new AssetInfo();
        basicInfo.setAssetId(1002L);
        basicInfo.setAssetNo("A-1002");
        basicInfo.setAssetName("测试资产二");
        basicInfo.setCategoryId(10L);
        basicInfo.setAssetType("1");
        updateReq.setBasicInfo(basicInfo);

        AssetFinance updateFinance = new AssetFinance();
        updateFinance.setOriginalValue(new BigDecimal("10000.00"));
        updateFinance.setDepreciationMethod("1");
        updateFinance.setUsefulLifeMonth(60);
        updateReq.setFinanceInfo(updateFinance);
        updateReq.setDynamicAttrs(Collections.emptyList());
        updateReq.setAttachments(Collections.emptyList());

        AssetInfo currentInfo = new AssetInfo();
        currentInfo.setAssetId(1002L);
        currentInfo.setAssetNo("A-1002");
        when(assetInfoService.selectAssetInfoByAssetId(1002L)).thenReturn(currentInfo);
        when(assetInfoService.selectAssetInfoByAssetNo("A-1002")).thenReturn(currentInfo);

        AssetFinance currentFinance = new AssetFinance();
        currentFinance.setAssetId(1002L);
        currentFinance.setOriginalValue(new BigDecimal("10000.00"));
        currentFinance.setDepreciationMethod("1");
        currentFinance.setUsefulLifeMonth(60);
        currentFinance.setLastDepreciationPeriod("2026-02");
        currentFinance.setAccumulatedDepreciation(new BigDecimal("500.00"));
        when(assetFinanceMapper.selectAssetFinanceByAssetId(1002L)).thenReturn(currentFinance);

        assetAggregateService.updateAsset(updateReq);

        verify(assetInfoService).updateAssetInfo(basicInfo);
        verify(assetFinanceMapper, never()).deleteAssetFinanceByAssetId(1002L);
    }
}
