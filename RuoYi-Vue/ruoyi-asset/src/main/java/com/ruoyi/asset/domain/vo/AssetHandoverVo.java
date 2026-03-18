package com.ruoyi.asset.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产交接展示对象。
 *
 * <p>
 * 该对象用于交接记录列表与详情展示，补齐资产、部门、责任人名称，
 * 便于资产管理员按业务单据追溯责任变化轨迹。
 * </p>
 *
 * @author Codex
 */
public class AssetHandoverVo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 交接ID */
    private Long handoverId;

    /** 交接单号 */
    @Excel(name = "交接单号")
    private String handoverNo;

    /** 资产ID */
    private Long assetId;

    /** 资产编码 */
    @Excel(name = "资产编码")
    private String assetCode;

    /** 资产名称 */
    @Excel(name = "资产名称")
    private String assetName;

    /** 交接类型 */
    @Excel(name = "交接类型")
    private String handoverType;

    /** 原部门ID */
    private Long fromDeptId;

    /** 原部门名称 */
    @Excel(name = "原部门")
    private String fromDeptName;

    /** 原责任人ID */
    private Long fromUserId;

    /** 原责任人名称 */
    @Excel(name = "原责任人")
    private String fromUserName;

    /** 交接前位置 */
    @Excel(name = "交接前位置")
    private String fromLocationName;

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

    /** 交接状态 */
    @Excel(name = "交接状态")
    private String handoverStatus;

    /** 交接日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "交接日期", dateFormat = "yyyy-MM-dd")
    private Date handoverDate;

    /** 交接后位置 */
    @Excel(name = "交接后位置")
    private String locationName;

    /** 确认人 */
    @Excel(name = "确认人")
    private String confirmBy;

    /** 确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "确认时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
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

    public String getAssetCode()
    {
        return assetCode;
    }

    public void setAssetCode(String assetCode)
    {
        this.assetCode = assetCode;
    }

    public String getAssetName()
    {
        return assetName;
    }

    public void setAssetName(String assetName)
    {
        this.assetName = assetName;
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

    public String getFromDeptName()
    {
        return fromDeptName;
    }

    public void setFromDeptName(String fromDeptName)
    {
        this.fromDeptName = fromDeptName;
    }

    public Long getFromUserId()
    {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId)
    {
        this.fromUserId = fromUserId;
    }

    public String getFromUserName()
    {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName)
    {
        this.fromUserName = fromUserName;
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
}
