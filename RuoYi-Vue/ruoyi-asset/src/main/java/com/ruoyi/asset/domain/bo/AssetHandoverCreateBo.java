package com.ruoyi.asset.domain.bo;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 资产交接创建参数。
 *
 * <p>交接主单只接收统一的目标信息和资产ID集合，
 * 每宗资产的来源快照由服务层在建单时自动读取并固化。</p>
 *
 * @author Codex
 */
public class AssetHandoverCreateBo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 交接类型 */
    @NotBlank(message = "交接类型不能为空")
    private String handoverType;

    /** 交接日期 */
    @NotNull(message = "交接日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date handoverDate;

    /** 交接资产ID列表 */
    @NotEmpty(message = "交接资产不能为空")
    private List<Long> assetIds;

    /** 目标部门ID */
    private Long toDeptId;

    /** 目标责任人ID */
    private Long toUserId;

    /** 目标位置 */
    @Size(max = 200, message = "目标位置长度不能超过200个字符")
    private String locationName;

    public String getHandoverType()
    {
        return handoverType;
    }

    public void setHandoverType(String handoverType)
    {
        this.handoverType = handoverType;
    }

    public Date getHandoverDate()
    {
        return handoverDate;
    }

    public void setHandoverDate(Date handoverDate)
    {
        this.handoverDate = handoverDate;
    }

    public List<Long> getAssetIds()
    {
        return assetIds;
    }

    public void setAssetIds(List<Long> assetIds)
    {
        this.assetIds = assetIds;
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
}
