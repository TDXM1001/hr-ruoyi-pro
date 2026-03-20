package com.ruoyi.asset.domain.vo;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产交接明细视图对象。
 *
 * @author Codex
 */
public class AssetHandoverItemVo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 交接明细ID */
    private Long handoverItemId;

    /** 交接主单ID */
    private Long handoverOrderId;

    /** 交接单号 */
    private String handoverNo;

    /** 交接类型 */
    private String handoverType;

    /** 交接日期 */
    private String handoverDate;

    /** 资产ID */
    private Long assetId;

    /** 资产编码 */
    @Excel(name = "资产编码")
    private String assetCode;

    /** 资产名称 */
    @Excel(name = "资产名称")
    private String assetName;

    /** 原使用部门ID */
    private Long fromDeptId;

    /** 原使用部门名称 */
    @Excel(name = "原使用部门")
    private String fromDeptName;

    /** 原责任人ID */
    private Long fromUserId;

    /** 原责任人名称 */
    @Excel(name = "原责任人")
    private String fromUserName;

    /** 交接前位置 */
    @Excel(name = "交接前位置")
    private String fromLocationName;

    /** 目标使用部门ID */
    private Long toDeptId;

    /** 目标使用部门名称 */
    @Excel(name = "目标使用部门")
    private String toDeptName;

    /** 目标责任人ID */
    private Long toUserId;

    /** 目标责任人名称 */
    @Excel(name = "目标责任人")
    private String toUserName;

    /** 交接后位置 */
    @Excel(name = "交接后位置")
    private String toLocationName;

    /** 交接前状态 */
    @Excel(name = "交接前状态")
    private String beforeStatus;

    /** 交接后状态 */
    @Excel(name = "交接后状态")
    private String afterStatus;

    public Long getHandoverItemId()
    {
        return handoverItemId;
    }

    public void setHandoverItemId(Long handoverItemId)
    {
        this.handoverItemId = handoverItemId;
    }

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

    public String getHandoverType()
    {
        return handoverType;
    }

    public void setHandoverType(String handoverType)
    {
        this.handoverType = handoverType;
    }

    public String getHandoverDate()
    {
        return handoverDate;
    }

    public void setHandoverDate(String handoverDate)
    {
        this.handoverDate = handoverDate;
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

    public String getToLocationName()
    {
        return toLocationName;
    }

    public void setToLocationName(String toLocationName)
    {
        this.toLocationName = toLocationName;
    }

    public String getBeforeStatus()
    {
        return beforeStatus;
    }

    public void setBeforeStatus(String beforeStatus)
    {
        this.beforeStatus = beforeStatus;
    }

    public String getAfterStatus()
    {
        return afterStatus;
    }

    public void setAfterStatus(String afterStatus)
    {
        this.afterStatus = afterStatus;
    }
}
