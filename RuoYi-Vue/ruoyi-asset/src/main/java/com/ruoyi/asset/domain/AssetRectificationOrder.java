package com.ruoyi.asset.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产整改单对象。
 *
 * <p>用于承接巡检/盘点异常后的整改登记闭环，M1 仅实现登记与状态更新，
 * 后续审批流在 M2 再接入。</p>
 *
 * @author Codex
 */
public class AssetRectificationOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 整改单ID */
    private Long rectificationId;

    /** 整改单号 */
    private String rectificationNo;

    /** 资产ID */
    private Long assetId;

    /** 巡检/盘点任务ID */
    private Long taskId;

    /** 盘点结果明细ID */
    private Long inventoryItemId;

    /** 整改状态 */
    private String rectificationStatus;

    /** 问题类型 */
    private String issueType;

    /** 问题描述 */
    private String issueDesc;

    /** 责任部门ID */
    private Long responsibleDeptId;

    /** 责任人ID */
    private Long responsibleUserId;

    /** 整改期限 */
    private Date deadlineDate;

    /** 完成时间 */
    private Date completedTime;

    private String completionDesc;

    private String acceptanceRemark;

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

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
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

    public Long getResponsibleUserId()
    {
        return responsibleUserId;
    }

    public void setResponsibleUserId(Long responsibleUserId)
    {
        this.responsibleUserId = responsibleUserId;
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

    public String getCompletionDesc()
    {
        return completionDesc;
    }

    public void setCompletionDesc(String completionDesc)
    {
        this.completionDesc = completionDesc;
    }

    public String getAcceptanceRemark()
    {
        return acceptanceRemark;
    }

    public void setAcceptanceRemark(String acceptanceRemark)
    {
        this.acceptanceRemark = acceptanceRemark;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("rectificationId", getRectificationId())
            .append("rectificationNo", getRectificationNo())
            .append("assetId", getAssetId())
            .append("taskId", getTaskId())
            .append("inventoryItemId", getInventoryItemId())
            .append("rectificationStatus", getRectificationStatus())
            .append("issueType", getIssueType())
            .append("issueDesc", getIssueDesc())
            .append("responsibleDeptId", getResponsibleDeptId())
            .append("responsibleUserId", getResponsibleUserId())
            .append("deadlineDate", getDeadlineDate())
            .append("completedTime", getCompletedTime())
            .append("completionDesc", getCompletionDesc())
            .append("acceptanceRemark", getAcceptanceRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
