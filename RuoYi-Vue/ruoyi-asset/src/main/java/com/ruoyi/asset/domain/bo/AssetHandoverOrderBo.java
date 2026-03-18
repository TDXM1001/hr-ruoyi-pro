package com.ruoyi.asset.domain.bo;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产交接主单查询参数。
 *
 * @author Codex
 */
public class AssetHandoverOrderBo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 交接单号 */
    private String handoverNo;

    /** 交接类型 */
    private String handoverType;

    /** 交接状态 */
    private String handoverStatus;

    /** 资产类型 */
    private String assetType;

    /** 目标部门ID */
    private Long toDeptId;

    /** 目标责任人ID */
    private Long toUserId;

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

    public String getHandoverStatus()
    {
        return handoverStatus;
    }

    public void setHandoverStatus(String handoverStatus)
    {
        this.handoverStatus = handoverStatus;
    }

    public String getAssetType()
    {
        return assetType;
    }

    public void setAssetType(String assetType)
    {
        this.assetType = assetType;
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
}
