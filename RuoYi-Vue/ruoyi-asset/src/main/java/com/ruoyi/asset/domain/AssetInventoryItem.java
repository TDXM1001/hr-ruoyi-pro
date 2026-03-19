package com.ruoyi.asset.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产盘点明细对象。
 *
 * @author Codex
 */
public class AssetInventoryItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long itemId;

    /** 盘点任务ID */
    private Long taskId;

    /** 资产ID */
    private Long assetId;

    /** 盘点结果 */
    private String inventoryResult;

    /** 盘点后位置 */
    private String actualLocationName;

    /** 盘点后使用部门ID */
    private Long actualUseDeptId;

    /** 盘点后责任人ID */
    private Long actualResponsibleUserId;

    /** 后续动作 */
    private String followUpAction;

    /** 处理状态 */
    private String processStatus;

    /** 处理时间 */
    private Date processTime;

    /** 后续业务ID */
    private Long followUpBizId;

    /** 盘点人 */
    private String checkedBy;

    /** 盘点时间 */
    private Date checkedTime;

    /** 结果说明 */
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

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

    public String getInventoryResult()
    {
        return inventoryResult;
    }

    public void setInventoryResult(String inventoryResult)
    {
        this.inventoryResult = inventoryResult;
    }

    public String getActualLocationName()
    {
        return actualLocationName;
    }

    public void setActualLocationName(String actualLocationName)
    {
        this.actualLocationName = actualLocationName;
    }

    public Long getActualUseDeptId()
    {
        return actualUseDeptId;
    }

    public void setActualUseDeptId(Long actualUseDeptId)
    {
        this.actualUseDeptId = actualUseDeptId;
    }

    public Long getActualResponsibleUserId()
    {
        return actualResponsibleUserId;
    }

    public void setActualResponsibleUserId(Long actualResponsibleUserId)
    {
        this.actualResponsibleUserId = actualResponsibleUserId;
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

    public Date getProcessTime()
    {
        return processTime;
    }

    public void setProcessTime(Date processTime)
    {
        this.processTime = processTime;
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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("itemId", getItemId())
            .append("taskId", getTaskId())
            .append("assetId", getAssetId())
            .append("inventoryResult", getInventoryResult())
            .append("actualLocationName", getActualLocationName())
            .append("actualUseDeptId", getActualUseDeptId())
            .append("actualResponsibleUserId", getActualResponsibleUserId())
            .append("followUpAction", getFollowUpAction())
            .append("processStatus", getProcessStatus())
            .append("processTime", getProcessTime())
            .append("followUpBizId", getFollowUpBizId())
            .append("checkedBy", getCheckedBy())
            .append("checkedTime", getCheckedTime())
            .append("resultDesc", getResultDesc())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}
