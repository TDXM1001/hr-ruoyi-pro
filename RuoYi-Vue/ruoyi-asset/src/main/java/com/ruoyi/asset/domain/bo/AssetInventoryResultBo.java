package com.ruoyi.asset.domain.bo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 资产盘点结果入参。
 *
 * @author Codex
 */
public class AssetInventoryResultBo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 盘点任务ID */
    @NotNull(message = "盘点任务ID不能为空")
    private Long taskId;

    /** 资产ID */
    @NotNull(message = "资产ID不能为空")
    private Long assetId;

    /** 盘点结果 */
    @NotBlank(message = "盘点结果不能为空")
    private String inventoryResult;

    /** 后续动作 */
    private String followUpAction;

    /** 盘点确认位置 */
    @Size(max = 200, message = "盘点位置长度不能超过200个字符")
    private String actualLocationName;

    /** 盘点确认使用部门ID */
    private Long actualUseDeptId;

    /** 盘点确认责任人ID */
    private Long actualResponsibleUserId;

    /** 是否确认资产处于使用中 */
    private Boolean confirmedUse;

    /** 盘点时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkedTime;

    /** 盘点结果说明 */
    @Size(max = 500, message = "盘点结果说明长度不能超过500个字符")
    private String resultDesc;

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

    public String getFollowUpAction()
    {
        return followUpAction;
    }

    public void setFollowUpAction(String followUpAction)
    {
        this.followUpAction = followUpAction;
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

    public Boolean getConfirmedUse()
    {
        return confirmedUse;
    }

    public void setConfirmedUse(Boolean confirmedUse)
    {
        this.confirmedUse = confirmedUse;
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
