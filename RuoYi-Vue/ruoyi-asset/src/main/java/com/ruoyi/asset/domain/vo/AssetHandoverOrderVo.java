package com.ruoyi.asset.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产交接主单视图对象。
 *
 * @author Codex
 */
public class AssetHandoverOrderVo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 交接主单ID */
    private Long handoverOrderId;

    /** 交接单号 */
    @Excel(name = "交接单号")
    private String handoverNo;

    /** 资产类型 */
    @Excel(name = "资产类型")
    private String assetType;

    /** 交接类型 */
    @Excel(name = "交接类型")
    private String handoverType;

    /** 交接状态 */
    @Excel(name = "交接状态")
    private String handoverStatus;

    /** 交接日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "交接日期", dateFormat = "yyyy-MM-dd")
    private Date handoverDate;

    /** 资产数量 */
    @Excel(name = "资产数量")
    private Integer assetCount;

    /** 目标部门ID */
    private Long toDeptId;

    /** 目标部门名称 */
    @Excel(name = "目标部门")
    private String toDeptName;

    /** 目标责任人ID */
    private Long toUserId;

    /** 目标责任人名称 */
    @Excel(name = "目标责任人")
    private String toUserName;

    /** 目标位置 */
    @Excel(name = "目标位置")
    private String locationName;

    /** 确认人 */
    @Excel(name = "确认人")
    private String confirmBy;

    /** 确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "确认时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
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

    public String getToDeptName()
    {
        return toDeptName;
    }

    public void setToDeptName(String toDeptName)
    {
        this.toDeptName = toDeptName;
    }

    public Long getToUserId()
    {
        return toUserId;
    }

    public void setToUserId(Long toUserId)
    {
        this.toUserId = toUserId;
    }

    public String getToUserName()
    {
        return toUserName;
    }

    public void setToUserName(String toUserName)
    {
        this.toUserName = toUserName;
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
}
