package com.ruoyi.asset.service;

import com.ruoyi.asset.domain.vo.AssetStatsOverviewVo;

/**
 * 资产统计服务接口。
 *
 * @author Codex
 */
public interface IAssetStatsService
{
    /**
     * 查询资产统计总览。
     *
     * @return 总览数据
     */
    AssetStatsOverviewVo getOverview();
}
