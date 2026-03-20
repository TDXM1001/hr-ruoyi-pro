package com.ruoyi.asset.domain.vo;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.asset.domain.AssetChangeLog;

/**
 * 资产生命周期详情视图对象。
 *
 * @author Codex
 */
public class AssetLedgerLifecycleVo
{
    private AssetLedgerVo ledger;

    private List<AssetHandoverItemVo> handoverRecords = new ArrayList<AssetHandoverItemVo>();

    private List<AssetInventoryRecordVo> inventoryRecords = new ArrayList<AssetInventoryRecordVo>();

    private List<AssetDisposalVo> disposalRecords = new ArrayList<AssetDisposalVo>();

    private List<AssetChangeLog> changeLogs = new ArrayList<AssetChangeLog>();

    public AssetLedgerVo getLedger()
    {
        return ledger;
    }

    public void setLedger(AssetLedgerVo ledger)
    {
        this.ledger = ledger;
    }

    public List<AssetHandoverItemVo> getHandoverRecords()
    {
        return handoverRecords;
    }

    public void setHandoverRecords(List<AssetHandoverItemVo> handoverRecords)
    {
        this.handoverRecords = handoverRecords;
    }

    public List<AssetInventoryRecordVo> getInventoryRecords()
    {
        return inventoryRecords;
    }

    public void setInventoryRecords(List<AssetInventoryRecordVo> inventoryRecords)
    {
        this.inventoryRecords = inventoryRecords;
    }

    public List<AssetDisposalVo> getDisposalRecords()
    {
        return disposalRecords;
    }

    public void setDisposalRecords(List<AssetDisposalVo> disposalRecords)
    {
        this.disposalRecords = disposalRecords;
    }

    public List<AssetChangeLog> getChangeLogs()
    {
        return changeLogs;
    }

    public void setChangeLogs(List<AssetChangeLog> changeLogs)
    {
        this.changeLogs = changeLogs;
    }
}
