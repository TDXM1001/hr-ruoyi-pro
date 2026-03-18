package com.ruoyi.asset.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产交接记录对象。
 *
 * <p>
 * 固定资产一期统一使用该对象承载领用、调拨、退还三类动作，
 * 保证每次使用关系变更都先形成业务记录，再回写台账当前态。
 * </p>
 *
 * @author Codex
 */
public class AssetHandover extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 交接记录ID */
    private Long handoverId;

    /** 交接单号 */
    private String handoverNo;

    /** 资产ID */
    private Long assetId;

    /** 交接类型（ASSIGN/TRANSFER/RETURN） */
    private String handoverType;

    /** 原部门ID */
    private Long fromDeptId;

    /** 原责任人ID */
    private Long fromUserId;

    /** 交接前位置 */
    private String fromLocationName;

    /** 目标部门ID */
    private Long toDeptId;

    /** 目标责任人ID */
    private Long toUserId;

    /** 交接状态 */
    private String handoverStatus;

    /** 交接日期 */
    private Date handoverDate;

    /** 交接后位置 */
    private String locationName;

    /** 确认人 */
    private String confirmBy;

    /** 确认时间 */
    private Date confirmTime;

    public Long getHandoverId()
    {
        return handoverId;
    }

    public void setHandoverId(Long handoverId)
    {
        this.handoverId = handoverId;
    }

    public String getHandoverNo()
    {
        return handoverNo;
    }

    public void setHandoverNo(String handoverNo)
    {
        this.handoverNo = handoverNo;
    }

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

    public String getHandoverType()
    {
        return handoverType;
    }

    public void setHandoverType(String handoverType)
    {
        this.handoverType = handoverType;
    }

    public Long getFromDeptId()
    {
        return fromDeptId;
    }

    public void setFromDeptId(Long fromDeptId)
    {
        this.fromDeptId = fromDeptId;
    }

    public Long getFromUserId()
    {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId)
    {
        this.fromUserId = fromUserId;
    }

    public String getFromLocationName()
    {
        return fromLocationName;
    }

    public void setFromLocationName(String fromLocationName)
    {
        this.fromLocationName = fromLocationName;
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
            .append("handoverId", getHandoverId())
            .append("handoverNo", getHandoverNo())
            .append("assetId", getAssetId())
            .append("handoverType", getHandoverType())
            .append("fromDeptId", getFromDeptId())
            .append("fromUserId", getFromUserId())
            .append("fromLocationName", getFromLocationName())
            .append("toDeptId", getToDeptId())
            .append("toUserId", getToUserId())
            .append("handoverStatus", getHandoverStatus())
            .append("handoverDate", getHandoverDate())
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
