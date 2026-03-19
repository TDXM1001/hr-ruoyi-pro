package com.ruoyi.asset.domain.bo;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 资产盘点任务业务入参。
 *
 * @author Codex
 */
public class AssetInventoryTaskBo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 任务编号 */
    private String taskNo;

    /** 任务名称 */
    @NotBlank(message = "盘点任务名称不能为空")
    @Size(max = 120, message = "盘点任务名称长度不能超过120个字符")
    private String taskName;

    /** 任务状态 */
    private String taskStatus;

    /** 范围类型 */
    private String scopeType;

    /** 范围值 */
    private String scopeValue;

    /** 盘点资产ID列表 */
    @NotEmpty(message = "盘点资产不能为空")
    private List<Long> assetIds;

    /** 计划盘点日期 */
    @NotNull(message = "计划盘点日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date plannedDate;

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

    public String getTaskStatus()
    {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus)
    {
        this.taskStatus = taskStatus;
    }

    public String getScopeType()
    {
        return scopeType;
    }

    public void setScopeType(String scopeType)
    {
        this.scopeType = scopeType;
    }

    public String getScopeValue()
    {
        return scopeValue;
    }

    public void setScopeValue(String scopeValue)
    {
        this.scopeValue = scopeValue;
    }

    public List<Long> getAssetIds()
    {
        return assetIds;
    }

    public void setAssetIds(List<Long> assetIds)
    {
        this.assetIds = assetIds;
    }

    public Date getPlannedDate()
    {
        return plannedDate;
    }

    public void setPlannedDate(Date plannedDate)
    {
        this.plannedDate = plannedDate;
    }
}
