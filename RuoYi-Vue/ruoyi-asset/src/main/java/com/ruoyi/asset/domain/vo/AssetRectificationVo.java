package com.ruoyi.asset.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产整改单视图对象。
 *
 * @author Codex
 */
public class AssetRectificationVo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long rectificationId;

    private String rectificationNo;

    private Long assetId;

    private String assetCode;

    private String assetName;

    private Long taskId;

    private String taskNo;

    private String taskName;

    private Long inventoryItemId;

    private String rectificationStatus;

    private String issueType;

    private String issueDesc;

    private Long responsibleDeptId;

    private String responsibleDeptName;

    private Long responsibleUserId;

    private String responsibleUserName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date deadlineDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date completedTime;

    public Long getRectificationId()
    {
        return rectificationId;
    }

    public void setRectificationId(Long rectificationId)
    {
        this.rectificationId = rectificationId;
    }

    public String getRectificationNo()
    {
        return rectificationNo;
    }

    public void setRectificationNo(String rectificationNo)
    {
        this.rectificationNo = rectificationNo;
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

    public Long getInventoryItemId()
    {
        return inventoryItemId;
    }

    public void setInventoryItemId(Long inventoryItemId)
    {
        this.inventoryItemId = inventoryItemId;
    }

    public String getRectificationStatus()
    {
        return rectificationStatus;
    }

    public void setRectificationStatus(String rectificationStatus)
    {
        this.rectificationStatus = rectificationStatus;
    }

    public String getIssueType()
    {
        return issueType;
    }

    public void setIssueType(String issueType)
    {
        this.issueType = issueType;
    }

    public String getIssueDesc()
    {
        return issueDesc;
    }

    public void setIssueDesc(String issueDesc)
    {
        this.issueDesc = issueDesc;
    }

    public Long getResponsibleDeptId()
    {
        return responsibleDeptId;
    }

    public void setResponsibleDeptId(Long responsibleDeptId)
    {
        this.responsibleDeptId = responsibleDeptId;
    }

    public String getResponsibleDeptName()
    {
        return responsibleDeptName;
    }

    public void setResponsibleDeptName(String responsibleDeptName)
    {
        this.responsibleDeptName = responsibleDeptName;
    }

    public Long getResponsibleUserId()
    {
        return responsibleUserId;
    }

    public void setResponsibleUserId(Long responsibleUserId)
    {
        this.responsibleUserId = responsibleUserId;
    }

    public String getResponsibleUserName()
    {
        return responsibleUserName;
    }

    public void setResponsibleUserName(String responsibleUserName)
    {
        this.responsibleUserName = responsibleUserName;
    }

    public Date getDeadlineDate()
    {
        return deadlineDate;
    }

    public void setDeadlineDate(Date deadlineDate)
    {
        this.deadlineDate = deadlineDate;
    }

    public Date getCompletedTime()
    {
        return completedTime;
    }

    public void setCompletedTime(Date completedTime)
    {
        this.completedTime = completedTime;
    }
}
