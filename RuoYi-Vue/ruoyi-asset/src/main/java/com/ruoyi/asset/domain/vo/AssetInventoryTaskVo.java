package com.ruoyi.asset.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产盘点任务视图对象。
 *
 * @author Codex
 */
public class AssetInventoryTaskVo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 任务编号 */
    @Excel(name = "盘点任务编号")
    private String taskNo;

    /** 任务名称 */
    @Excel(name = "盘点任务名称")
    private String taskName;

    /** 任务状态 */
    @Excel(name = "任务状态")
    private String taskStatus;

    /** 范围类型 */
    @Excel(name = "盘点范围")
    private String scopeType;

    /** 范围值 */
    private String scopeValue;

    /** 计划盘点日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "计划盘点日期", dateFormat = "yyyy-MM-dd")
    private Date plannedDate;

    /** 完成日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "完成日期", dateFormat = "yyyy-MM-dd")
    private Date completedDate;

    /** 任务范围资产总数 */
    private Long scopeAssetCount;

    /** 已登记资产数 */
    private Long submittedCount;

    /** 异常资产数 */
    private Long abnormalCount;

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

    public Long getScopeAssetCount()
    {
        return scopeAssetCount;
    }

    public void setScopeAssetCount(Long scopeAssetCount)
    {
        this.scopeAssetCount = scopeAssetCount;
    }

    public Long getSubmittedCount()
    {
        return submittedCount;
    }

    public void setSubmittedCount(Long submittedCount)
    {
        this.submittedCount = submittedCount;
    }

    public Long getAbnormalCount()
    {
        return abnormalCount;
    }

    public void setAbnormalCount(Long abnormalCount)
    {
        this.abnormalCount = abnormalCount;
    }
}
