package com.ruoyi.asset.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产交接明细对象。
 *
 * <p>明细负责固化每宗资产在交接时的来源、目标和状态快照，
 * 避免后续台账变化后无法追溯当时的真实交接上下文。</p>
 *
 * @author Codex
 */
public class AssetHandoverItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 交接明细ID */
    private Long handoverItemId;

    /** 交接主单ID */
    private Long handoverOrderId;

    /** 资产ID */
    private Long assetId;

    /** 资产编码快照 */
    private String assetCode;

    /** 资产名称快照 */
    private String assetName;

    /** 交接前使用部门ID */
    private Long fromDeptId;

    /** 交接前责任人ID */
    private Long fromUserId;

    /** 交接前位置 */
    private String fromLocationName;

    /** 交接后使用部门ID */
    private Long toDeptId;

    /** 交接后责任人ID */
    private Long toUserId;

    /** 交接后位置 */
    private String toLocationName;

    /** 交接前状态 */
    private String beforeStatus;

    /** 交接后状态 */
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

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("handoverItemId", getHandoverItemId())
            .append("handoverOrderId", getHandoverOrderId())
            .append("assetId", getAssetId())
            .append("assetCode", getAssetCode())
            .append("assetName", getAssetName())
            .append("fromDeptId", getFromDeptId())
            .append("fromUserId", getFromUserId())
            .append("fromLocationName", getFromLocationName())
            .append("toDeptId", getToDeptId())
            .append("toUserId", getToUserId())
            .append("toLocationName", getToLocationName())
            .append("beforeStatus", getBeforeStatus())
            .append("afterStatus", getAfterStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
