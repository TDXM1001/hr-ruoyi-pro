package com.ruoyi.asset.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产交接主单对象。
 *
 * <p>主单负责表达一次批量领用、调拨、退还业务的统一信息，
 * 明细资产的来源、去向与状态快照由 {@link AssetHandoverItem} 记录。</p>
 *
 * @author Codex
 */
public class AssetHandoverOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 交接主单ID */
    private Long handoverOrderId;

    /** 交接单号 */
    private String handoverNo;

    /** 资产类型 */
    private String assetType;

    /** 交接类型 */
    private String handoverType;

    /** 交接状态 */
    private String handoverStatus;

    /** 交接日期 */
    private Date handoverDate;

    /** 资产数量 */
    private Integer assetCount;

    /** 目标部门ID */
    private Long toDeptId;

    /** 目标责任人ID */
    private Long toUserId;

    /** 目标位置 */
    private String locationName;

    /** 确认人 */
    private String confirmBy;

    /** 确认时间 */
    private Date confirmTime;

    public Long getHandoverOrderId()
    {
        return handoverOrderId;
    }

    public void setHandoverOrderId(Long handoverOrderId)
    {
        this.handoverOrderId = handoverOrderId;
    }

    public String getHandoverNo()
    {
        return handoverNo;
    }

    public void setHandoverNo(String handoverNo)
    {
        this.handoverNo = handoverNo;
    }

    public String getAssetType()
    {
        return assetType;
    }

    public void setAssetType(String assetType)
    {
        this.assetType = assetType;
    }

    public String getHandoverType()
    {
        return handoverType;
    }

    public void setHandoverType(String handoverType)
    {
        this.handoverType = handoverType;
    }

    public String getHandoverStatus()
    {
        return handoverStatus;
    }

    public void setHandoverStatus(String handoverStatus)
    {
        this.handoverStatus = handoverStatus;
    }

    public Date getHandoverDate()
    {
        return handoverDate;
    }

    public void setHandoverDate(Date handoverDate)
    {
        this.handoverDate = handoverDate;
    }

    public Integer getAssetCount()
    {
        return assetCount;
    }

    public void setAssetCount(Integer assetCount)
    {
        this.assetCount = assetCount;
    }

    public Long getToDeptId()
    {
        return toDeptId;
    }

    public void setToDeptId(Long toDeptId)
    {
        this.toDeptId = toDeptId;
    }

    public Long getToUserId()
    {
        return toUserId;
    }

    public void setToUserId(Long toUserId)
    {
        this.toUserId = toUserId;
    }

    public String getLocationName()
    {
        return locationName;
    }

    public void setLocationName(String locationName)
    {
        this.locationName = locationName;
    }

    public String getConfirmBy()
    {
        return confirmBy;
    }

    public void setConfirmBy(String confirmBy)
    {
        this.confirmBy = confirmBy;
    }

    public Date getConfirmTime()
    {
        return confirmTime;
    }

    public void setConfirmTime(Date confirmTime)
    {
        this.confirmTime = confirmTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("handoverOrderId", getHandoverOrderId())
            .append("handoverNo", getHandoverNo())
            .append("assetType", getAssetType())
            .append("handoverType", getHandoverType())
            .append("handoverStatus", getHandoverStatus())
            .append("handoverDate", getHandoverDate())
            .append("assetCount", getAssetCount())
            .append("toDeptId", getToDeptId())
            .append("toUserId", getToUserId())
            .append("locationName", getLocationName())
            .append("confirmBy", getConfirmBy())
            .append("confirmTime", getConfirmTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
