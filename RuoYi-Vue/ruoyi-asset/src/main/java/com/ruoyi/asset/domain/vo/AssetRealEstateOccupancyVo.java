package com.ruoyi.asset.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 不动产占用视图对象。
 *
 * @author Codex
 */
public class AssetRealEstateOccupancyVo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long occupancyId;

    private String occupancyNo;

    private Long assetId;

    private Long useDeptId;

    private String useDeptName;

    private Long responsibleUserId;

    private String responsibleUserName;

    private String locationName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
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

    public String getUseDeptName()
    {
        return useDeptName;
    }

    public void setUseDeptName(String useDeptName)
    {
        this.useDeptName = useDeptName;
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
}
