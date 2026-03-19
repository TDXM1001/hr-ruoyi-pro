package com.ruoyi.web.controller.asset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.asset.service.IAssetStatsService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 资产统计控制器。
 *
 * @author Codex
 */
@RestController
@RequestMapping("/asset/stats")
public class AssetStatsController extends BaseController
{
    @Autowired
    private IAssetStatsService assetStatsService;

    /**
     * 查询资产统计总览。
     *
     * @return 统计总览
     */
    @PreAuthorize("@ss.hasAnyPermi('asset:stats:overview,asset:ledger:list')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        return success(assetStatsService.getOverview());
    }
}
