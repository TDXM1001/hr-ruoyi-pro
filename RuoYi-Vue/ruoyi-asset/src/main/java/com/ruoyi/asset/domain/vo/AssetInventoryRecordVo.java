package com.ruoyi.asset.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 资产盘点历史记录视图对象。
 *
 * @author Codex
 */
public class AssetInventoryRecordVo
{
    private Long itemId;

    private Long taskId;

    private String taskNo;

    private String taskName;

    private Long assetId;

    private String assetCode;

    private String inventoryResult;

    private String followUpAction;

    private String processStatus;

    private Long followUpBizId;

    private String checkedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkedTime;

    private String resultDesc;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public String getTaskNo()
    {
        return taskNo;
    }

    public void setTaskNo(String taskNo)
    {
        this.taskNo = taskNo;
    }

    public String getTaskName()
    {
        return taskName;
    }

    public void setTaskName(String taskName)
    {
        this.taskName = taskName;
    }

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

    public String getAssetCode()
    {
        return assetCode;
    }

    public void setAssetCode(String assetCode)
    {
        this.assetCode = assetCode;
    }

    public String getInventoryResult()
    {
        return inventoryResult;
    }

    public void setInventoryResult(String inventoryResult)
    {
        this.inventoryResult = inventoryResult;
    }

    public String getFollowUpAction()
    {
        return followUpAction;
    }

    public void setFollowUpAction(String followUpAction)
    {
        this.followUpAction = followUpAction;
    }

    public String getProcessStatus()
    {
        return processStatus;
    }

    public void setProcessStatus(String processStatus)
    {
        this.processStatus = processStatus;
    }

    public Long getFollowUpBizId()
    {
        return followUpBizId;
    }

    public void setFollowUpBizId(Long followUpBizId)
    {
        this.followUpBizId = followUpBizId;
    }

    public String getCheckedBy()
    {
        return checkedBy;
    }

    public void setCheckedBy(String checkedBy)
    {
        this.checkedBy = checkedBy;
    }

    public Date getCheckedTime()
    {
        return checkedTime;
    }

    public void setCheckedTime(Date checkedTime)
    {
        this.checkedTime = checkedTime;
    }

    public String getResultDesc()
    {
        return resultDesc;
    }

    public void setResultDesc(String resultDesc)
    {
        this.resultDesc = resultDesc;
    }
}
