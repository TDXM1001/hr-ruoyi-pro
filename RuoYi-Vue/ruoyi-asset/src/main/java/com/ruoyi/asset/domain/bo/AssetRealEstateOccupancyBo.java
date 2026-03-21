package com.ruoyi.asset.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 不动产占用登记入参。
 *
 * @author Codex
 */
public class AssetRealEstateOccupancyBo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    @NotNull(message = "使用部门不能为空")
    private Long useDeptId;

    @NotNull(message = "责任人不能为空")
    private Long responsibleUserId;

    @Size(max = 255, message = "使用位置长度不能超过255个字符")
    private String locationName;

    @NotBlank(message = "占用开始日期不能为空")
    private String startDate;

    @NotBlank(message = "发起/变更原因不能为空")
    @Size(max = 500, message = "发起/变更原因长度不能超过500个字符")
    private String changeReason;

    public Long getUseDeptId()
    {
        return useDeptId;
    }

    public void setUseDeptId(Long useDeptId)
    {
        this.useDeptId = useDeptId;
    }

    public Long getResponsibleUserId()
    {
        return responsibleUserId;
    }

    public void setResponsibleUserId(Long responsibleUserId)
    {
        this.responsibleUserId = responsibleUserId;
    }

    public String getLocationName()
    {
        return locationName;
    }

    public void setLocationName(String locationName)
    {
        this.locationName = locationName;
    }

    public String getStartDate()
    {
        return startDate;
    }

    public void setStartDate(String startDate)
    {
        this.startDate = startDate;
    }

    public String getChangeReason()
    {
        return changeReason;
    }

    public void setChangeReason(String changeReason)
    {
        this.changeReason = changeReason;
    }
}
