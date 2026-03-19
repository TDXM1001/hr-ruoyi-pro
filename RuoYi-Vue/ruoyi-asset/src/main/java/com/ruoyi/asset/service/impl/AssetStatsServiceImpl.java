package com.ruoyi.asset.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.asset.domain.vo.AssetStatsOverviewVo;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.asset.mapper.AssetInventoryMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.asset.service.IAssetStatsService;

/**
 * 资产统计服务实现。
 *
 * @author Codex
 */
@Service
public class AssetStatsServiceImpl implements IAssetStatsService
{
    @Autowired
    private AssetLedgerMapper assetLedgerMapper;

    @Autowired
    private AssetInventoryMapper assetInventoryMapper;

    /**
     * 查询资产统计总览。
     *
     * @return 统计总览
     */
    @Override
    public AssetStatsOverviewVo getOverview()
    {
        AssetStatsOverviewVo overview = new AssetStatsOverviewVo();
        overview.setTotalCount(nvl(assetLedgerMapper.countAssetTotal()));
        overview.setInUseCount(countByStatus(AssetStatus.IN_USE));
        overview.setIdleCount(countByStatus(AssetStatus.IDLE));
        overview.setInventoryingCount(countByStatus(AssetStatus.INVENTORYING));
        overview.setPendingDisposalCount(countByStatus(AssetStatus.PENDING_DISPOSAL));
        overview.setDisposedCount(countByStatus(AssetStatus.DISPOSED));
        overview.setOverdueInventoryCount(nvl(assetInventoryMapper.countOverdueInventoryTask()));
        return overview;
    }

    /**
     * 按状态统计资产数量。
     *
     * @param status 资产状态
     * @return 统计值
     */
    private Long countByStatus(AssetStatus status)
    {
        return nvl(assetLedgerMapper.countAssetByStatus(status.getCode()));
    }

    /**
     * 空值转0。
     *
     * @param value 原始值
     * @return 非空值
     */
    private Long nvl(Long value)
    {
        return value == null ? 0L : value;
    }
}
