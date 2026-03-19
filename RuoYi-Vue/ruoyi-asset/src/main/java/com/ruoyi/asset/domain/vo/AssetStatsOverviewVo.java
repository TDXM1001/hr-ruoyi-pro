package com.ruoyi.asset.domain.vo;

import java.io.Serializable;

/**
 * 资产统计总览对象。
 *
 * @author Codex
 */
public class AssetStatsOverviewVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 资产总量 */
    private Long totalCount;

    /** 使用中数量 */
    private Long inUseCount;

    /** 闲置数量 */
    private Long idleCount;

    /** 盘点中数量 */
    private Long inventoryingCount;

    /** 待处置数量 */
    private Long pendingDisposalCount;

    /** 已处置数量 */
    private Long disposedCount;

    /** 超期未完成盘点任务数量 */
    private Long overdueInventoryCount;

    public Long getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(Long totalCount)
    {
        this.totalCount = totalCount;
    }

    public Long getInUseCount()
    {
        return inUseCount;
    }

    public void setInUseCount(Long inUseCount)
    {
        this.inUseCount = inUseCount;
    }

    public Long getIdleCount()
    {
        return idleCount;
    }

    public void setIdleCount(Long idleCount)
    {
        this.idleCount = idleCount;
    }

    public Long getInventoryingCount()
    {
        return inventoryingCount;
    }

    public void setInventoryingCount(Long inventoryingCount)
    {
        this.inventoryingCount = inventoryingCount;
    }

    public Long getPendingDisposalCount()
    {
        return pendingDisposalCount;
    }

    public void setPendingDisposalCount(Long pendingDisposalCount)
    {
        this.pendingDisposalCount = pendingDisposalCount;
    }

    public Long getDisposedCount()
    {
        return disposedCount;
    }

    public void setDisposedCount(Long disposedCount)
    {
        this.disposedCount = disposedCount;
    }

    public Long getOverdueInventoryCount()
    {
        return overdueInventoryCount;
    }

    public void setOverdueInventoryCount(Long overdueInventoryCount)
    {
        this.overdueInventoryCount = overdueInventoryCount;
    }
}
