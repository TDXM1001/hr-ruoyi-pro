package com.ruoyi.asset.domain.bo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 资产交接业务入参对象。
 *
 * <p>
 * 该对象统一承载领用、调拨、退还的查询与新增参数，
 * 其中动作强约束由服务层结合资产当前状态进行校验。
 * </p>
 *
 * @author Codex
 */
public class AssetHandoverBo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 交接ID */
    private Long handoverId;

    /** 交接单号 */
    @Size(max = 64, message = "交接单号长度不能超过64个字符")
    private String handoverNo;

    /** 资产ID */
    @NotNull(message = "资产ID不能为空")
    private Long assetId;

    /** 交接类型（ASSIGN/TRANSFER/RETURN） */
    @NotBlank(message = "交接类型不能为空")
    private String handoverType;

    /** 原部门ID */
    private Long fromDeptId;

    /** 原责任人ID */
    private Long fromUserId;

    /** 交接前位置 */
    @Size(max = 200, message = "交接前位置长度不能超过200个字符")
    private String fromLocationName;

    /** 目标部门ID */
    private Long toDeptId;

    /** 目标责任人ID */
    private Long toUserId;

    /** 交接状态 */
    @Size(max = 32, message = "交接状态长度不能超过32个字符")
    private String handoverStatus;

    /** 交接日期 */
    @NotNull(message = "交接日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date handoverDate;

    /** 交接后位置 */
    @Size(max = 200, message = "交接后位置长度不能超过200个字符")
    private String locationName;

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
}
