package com.ruoyi.asset.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产盘点任务对象。
 *
 * @author Codex
 */
public class AssetInventoryTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 任务编号 */
    private String taskNo;

    /** 任务名称 */
    private String taskName;

    /** 任务状态 */
    private String taskStatus;

    /** 范围类型 */
    private String scopeType;

    /** 范围值 */
    private String scopeValue;

    /** 计划盘点日期 */
    private Date plannedDate;

    /** 完成日期 */
    private Date completedDate;

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

    public Date getPlannedDate()
    {
        return plannedDate;
    }

    public void setPlannedDate(Date plannedDate)
    {
        this.plannedDate = plannedDate;
    }

    public Date getCompletedDate()
    {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate)
    {
        this.completedDate = completedDate;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("taskId", getTaskId())
            .append("taskNo", getTaskNo())
            .append("taskName", getTaskName())
            .append("taskStatus", getTaskStatus())
            .append("scopeType", getScopeType())
            .append("scopeValue", getScopeValue())
            .append("plannedDate", getPlannedDate())
            .append("completedDate", getCompletedDate())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
