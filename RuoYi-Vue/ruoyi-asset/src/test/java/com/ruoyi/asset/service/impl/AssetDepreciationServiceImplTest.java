package com.ruoyi.asset.service.impl;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.ruoyi.asset.domain.AssetDepreciationLog;
import com.ruoyi.asset.domain.AssetFinance;
import com.ruoyi.asset.mapper.AssetDepreciationLogMapper;
import com.ruoyi.asset.mapper.AssetFinanceMapper;
import com.ruoyi.asset.service.IAssetFinanceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * 资产折旧服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AssetDepreciationServiceImplTest {
    @Mock
    private AssetFinanceMapper assetFinanceMapper;

    @Mock
    private AssetDepreciationLogMapper assetDepreciationLogMapper;

    @Mock
    private IAssetFinanceService assetFinanceService;

    @InjectMocks
    private AssetDepreciationServiceImpl assetDepreciationService;

    @Test
    void shouldAccrueDepreciationByPeriodForEligibleAssets() {
        AssetFinance finance1 = new AssetFinance();
        finance1.setFinanceId(1L);
        finance1.setAssetId(101L);
        finance1.setOriginalValue(new BigDecimal("1000.00"));
        finance1.setUsefulLifeMonth(12);
        finance1.setDepreciationMethod("1");
        finance1.setFinanceStatus("0");

        AssetFinance finance2 = new AssetFinance();
        finance2.setFinanceId(2L);
        finance2.setAssetId(102L);
        finance2.setOriginalValue(new BigDecimal("2000.00"));
        finance2.setUsefulLifeMonth(24);
        finance2.setDepreciationMethod("1");
        finance2.setFinanceStatus("1");

        AssetFinance finance3 = new AssetFinance();
        finance3.setFinanceId(3L);
        finance3.setAssetId(103L);
        finance3.setFinanceStatus("3");

        when(assetFinanceMapper.selectAssetFinanceList(org.mockito.ArgumentMatchers.any(AssetFinance.class)))
            .thenReturn(Arrays.asList(finance1, finance2, finance3));
        when(assetFinanceMapper.selectAssetFinanceByAssetId(101L)).thenReturn(finance1);
        when(assetFinanceMapper.selectAssetFinanceByAssetId(102L)).thenReturn(finance2);
        when(assetDepreciationLogMapper.selectAssetDepreciationLogByAssetIdAndPeriod(101L, "2026-03")).thenReturn(null);
        when(assetDepreciationLogMapper.selectAssetDepreciationLogByAssetIdAndPeriod(102L, "2026-03")).thenReturn(null);
        when(assetDepreciationLogMapper.selectAssetDepreciationLogByAssetId(101L)).thenReturn(Collections.emptyList());
        when(assetDepreciationLogMapper.selectAssetDepreciationLogByAssetId(102L)).thenReturn(Collections.emptyList());
        when(assetFinanceService.calculateDepreciatedFinance(finance1, 1)).thenReturn(buildCalculatedFinance(finance1, "80.00"));
        when(assetFinanceService.calculateDepreciatedFinance(finance2, 1)).thenReturn(buildCalculatedFinance(finance2, "60.00"));

        List<AssetDepreciationLog> result = assetDepreciationService.accrueDepreciationByPeriod("2026-03");

        assertEquals(2, result.size());
        assertEquals(Arrays.asList(101L, 102L), result.stream().map(AssetDepreciationLog::getAssetId).toList());
        assertTrue(result.stream().allMatch(log -> "2026-03".equals(log.getPeriod())));
    }

    private AssetFinance buildCalculatedFinance(AssetFinance source, String monthlyAmount) {
        AssetFinance finance = new AssetFinance();
        finance.setFinanceId(source.getFinanceId());
        finance.setAssetId(source.getAssetId());
        finance.setMonthlyDepreciationAmount(new BigDecimal(monthlyAmount));
        finance.setAccumulatedDepreciation(new BigDecimal(monthlyAmount));
        finance.setNetBookValue(new BigDecimal("920.00"));
        finance.setBookValue(new BigDecimal("920.00"));
        return finance;
    }
}
