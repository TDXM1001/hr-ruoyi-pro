package com.ruoyi.asset.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 盘点任务资产明细视图对象。
 *
 * @author Codex
 */
public class AssetInventoryTaskAssetVo
{
    /** 明细ID */
    private Long itemId;

    /** 任务ID */
    private Long taskId;

    /** 资产ID */
    private Long assetId;

    /** 资产编码 */
    private String assetCode;

    /** 资产名称 */
    private String assetName;

    /** 资产状态 */
    private String assetStatus;

    /** 权属部门名称 */
    private String ownerDeptName;

    /** 使用部门名称 */
    private String useDeptName;

    /** 责任人名称 */
    private String responsibleUserName;

    /** 资产位置 */
    private String locationName;

    /** 是否已登记盘点结果：1-已登记，0-未登记 */
    private Integer resultRegistered;

    /** 盘点结果 */
    private String inventoryResult;

    /** 后续动作 */
    private String followUpAction;

    /** 处理状态 */
    private String processStatus;

    /** 关联后续业务ID */
    private Long followUpBizId;

    /** 盘点人 */
    private String checkedBy;

    /** 盘点时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkedTime;

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

    public String getAssetName()
    {
        return assetName;
    }

    public void setAssetName(String assetName)
    {
        this.assetName = assetName;
    }

    public String getAssetStatus()
    {
        return assetStatus;
    }

    public void setAssetStatus(String assetStatus)
    {
        this.assetStatus = assetStatus;
    }

    public String getOwnerDeptName()
    {
        return ownerDeptName;
    }

    public void setOwnerDeptName(String ownerDeptName)
    {
        this.ownerDeptName = ownerDeptName;
    }

    public String getUseDeptName()
    {
        return useDeptName;
    }

    public void setUseDeptName(String useDeptName)
    {
        this.useDeptName = useDeptName;
    }

    public String getResponsibleUserName()
    {
        return responsibleUserName;
    }

    public void setResponsibleUserName(String responsibleUserName)
    {
        this.responsibleUserName = responsibleUserName;
    }

    public String getLocationName()
    {
        return locationName;
    }

    public void setLocationName(String locationName)
    {
        this.locationName = locationName;
    }

    public Integer getResultRegistered()
    {
        return resultRegistered;
    }

    public void setResultRegistered(Integer resultRegistered)
    {
        this.resultRegistered = resultRegistered;
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
}
