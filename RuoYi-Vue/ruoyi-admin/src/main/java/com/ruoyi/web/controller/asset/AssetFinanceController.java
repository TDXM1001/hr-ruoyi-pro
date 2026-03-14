package com.ruoyi.web.controller.asset;

import com.ruoyi.asset.service.IAssetDepreciationService;
import com.ruoyi.asset.service.IAssetFinanceService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 资产财务管理控制器。
 */
@RestController
@RequestMapping("/asset/finance")
public class AssetFinanceController extends BaseController {
    @Autowired
    private IAssetFinanceService assetFinanceService;

    @Autowired
    private IAssetDepreciationService assetDepreciationService;

    /**
     * 手动重算指定资产的财务结果。
     */
    @PreAuthorize("@ss.hasPermi('asset:finance:recalculate')")
    @Log(title = "资产财务", businessType = BusinessType.UPDATE)
    @PostMapping("/{assetId}/recalculate")
    public AjaxResult recalculate(@PathVariable("assetId") Long assetId) {
        return success(assetFinanceService.recalculateFinanceByAssetId(assetId));
    }

    /**
     * 查询指定资产的折旧日志。
     */
    @PreAuthorize("@ss.hasPermi('asset:finance:query')")
    @GetMapping("/{assetId}/depreciation-logs")
    public AjaxResult logs(@PathVariable("assetId") Long assetId) {
        return success(assetDepreciationService.selectAssetDepreciationLogByAssetId(assetId));
    }

    /**
     * 按期间批量执行月度折旧计提。
     */
    @PreAuthorize("@ss.hasPermi('asset:finance:accrue')")
    @Log(title = "资产折旧计提", businessType = BusinessType.OTHER)
    @PostMapping("/accrue/{period}")
    public AjaxResult accrueByPeriod(@PathVariable("period") String period) {
        return success(assetDepreciationService.accrueDepreciationByPeriod(period));
    }
}
