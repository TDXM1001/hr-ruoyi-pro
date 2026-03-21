package com.ruoyi.asset.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 不动产占用单对象。
 *
 * <p>用于承接资产级占用闭环，记录当前有效占用与历史释放轨迹。</p>
 *
 * @author Codex
 */
public class AssetRealEstateOccupancyOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long occupancyId;

    private String occupancyNo;

    private Long assetId;

    private Long useDeptId;

    private Long responsibleUserId;

    private String locationName;

    private Date startDate;

    private Date endDate;

    private String occupancyStatus;

    private String changeReason;

    private String releaseReason;

    public Long getOccupancyId()
    {
        return occupancyId;
    }

    public void setOccupancyId(Long occupancyId)
    {
        this.occupancyId = occupancyId;
    }

    public String getOccupancyNo()
    {
        return occupancyNo;
    }

    public void setOccupancyNo(String occupancyNo)
    {
        this.occupancyNo = occupancyNo;
    }

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

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

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    public String getOccupancyStatus()
    {
        return occupancyStatus;
    }

    public void setOccupancyStatus(String occupancyStatus)
    {
        this.occupancyStatus = occupancyStatus;
    }

    public String getChangeReason()
    {
        return changeReason;
    }

    public void setChangeReason(String changeReason)
    {
        this.changeReason = changeReason;
    }

    public String getReleaseReason()
    {
        return releaseReason;
    }

    public void setReleaseReason(String releaseReason)
    {
        this.releaseReason = releaseReason;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("occupancyId", getOccupancyId())
            .append("occupancyNo", getOccupancyNo())
            .append("assetId", getAssetId())
            .append("useDeptId", getUseDeptId())
            .append("responsibleUserId", getResponsibleUserId())
            .append("locationName", getLocationName())
            .append("startDate", getStartDate())
            .append("endDate", getEndDate())
            .append("occupancyStatus", getOccupancyStatus())
            .append("changeReason", getChangeReason())
            .append("releaseReason", getReleaseReason())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
