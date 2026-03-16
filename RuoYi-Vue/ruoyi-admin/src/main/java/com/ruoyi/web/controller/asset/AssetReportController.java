package com.ruoyi.web.controller.asset;

import com.ruoyi.asset.service.IAssetReportService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 资产报表与预警 Controller。
 *
 * 当前阶段复用资产台账查看权限开放查询能力，
 * 避免在批次六额外引入菜单与权限 SQL 变更。
 */
@RestController
@RequestMapping("/asset/report")
public class AssetReportController extends BaseController {

    @Autowired
    private IAssetReportService assetReportService;

    /**
     * 查询资产统计汇总。
     */
    @PreAuthorize("@ss.hasPermi('asset:info:list')")
    @GetMapping("/summary")
    public AjaxResult summary() {
        return success(assetReportService.getSummary());
    }

    /**
     * 查询资产预警视图。
     */
    @PreAuthorize("@ss.hasPermi('asset:info:list')")
    @GetMapping("/warnings")
    public AjaxResult warnings(@RequestParam(value = "landTermWithinDays", required = false) Integer landTermWithinDays) {
        return success(assetReportService.getWarnings(landTermWithinDays));
    }
}