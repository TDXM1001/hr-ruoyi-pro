package com.ruoyi.asset.domain.bo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 资产整改登记入参。
 *
 * @author Codex
 */
public class AssetRectificationBo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 整改单ID */
    private Long rectificationId;

    /** 资产ID。由路径变量回填，不要求前端在请求体重复传递。 */
    private Long assetId;

    /** 巡检/盘点任务ID */
    @NotNull(message = "巡检任务ID不能为空")
    private Long taskId;

    /** 盘点结果明细ID */
    @NotNull(message = "巡检结果明细ID不能为空")
    private Long inventoryItemId;

    /** 整改状态 */
    private String rectificationStatus;

    /** 问题类型 */
    @NotBlank(message = "问题类型不能为空")
    @Size(max = 64, message = "问题类型长度不能超过64个字符")
    private String issueType;

    /** 问题描述 */
    @NotBlank(message = "问题描述不能为空")
    @Size(max = 500, message = "问题描述长度不能超过500个字符")
    private String issueDesc;

    /** 责任部门ID */
    @NotNull(message = "责任部门不能为空")
    private Long responsibleDeptId;

    /** 责任人ID */
    @NotNull(message = "责任人不能为空")
    private Long responsibleUserId;

    /** 整改期限 */
    @NotNull(message = "整改期限不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date deadlineDate;

    public Long getRectificationId()
    {
        return rectificationId;
    }

    public void setRectificationId(Long rectificationId)
    {
        this.rectificationId = rectificationId;
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
}
