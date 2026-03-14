package com.ruoyi.web.controller.asset;

import java.util.Collections;
import java.util.List;
import com.ruoyi.asset.domain.AssetDepreciationLog;
import com.ruoyi.asset.domain.AssetFinance;
import com.ruoyi.asset.service.IAssetDepreciationService;
import com.ruoyi.asset.service.IAssetFinanceService;
import com.ruoyi.common.core.domain.AjaxResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * 资产财务控制器测试。
 */
@ExtendWith(MockitoExtension.class)
class AssetFinanceControllerTest {
    @Mock
    private IAssetFinanceService assetFinanceService;

    @Mock
    private IAssetDepreciationService assetDepreciationService;

    @InjectMocks
    private AssetFinanceController assetFinanceController;

    @Test
    void shouldExposeFinanceRecalculateAndDepreciationApis() {
        AssetFinance finance = new AssetFinance();
        finance.setAssetId(1001L);
        finance.setFinanceStatus("1");
        AssetDepreciationLog log = new AssetDepreciationLog();
        log.setAssetId(1001L);
        log.setPeriod("2026-03");
        List<AssetDepreciationLog> logs = Collections.singletonList(log);

        when(assetFinanceService.recalculateFinanceByAssetId(1001L)).thenReturn(finance);
        when(assetDepreciationService.selectAssetDepreciationLogByAssetId(1001L)).thenReturn(logs);
        when(assetDepreciationService.accrueDepreciationByPeriod("2026-03")).thenReturn(logs);

        AjaxResult financeResult = assetFinanceController.recalculate(1001L);
        AjaxResult logsResult = assetFinanceController.logs(1001L);
        AjaxResult accrueResult = assetFinanceController.accrueByPeriod("2026-03");

        assertEquals(finance, financeResult.get(AjaxResult.DATA_TAG));
        assertEquals(logs, logsResult.get(AjaxResult.DATA_TAG));
        assertEquals(logs, accrueResult.get(AjaxResult.DATA_TAG));
    }
}
