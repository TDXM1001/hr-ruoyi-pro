package com.ruoyi.asset.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产台账主档对象。
 * <p>
 * 该对象对应资产当前态主档，承载固定资产一期台账维护所需的稳定属性，
 * 不直接承担交接、盘点、处置等过程留痕数据。
 * </p>
 *
 * @author Codex
 */
public class AssetLedger extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 资产ID */
    private Long assetId;

    /** 资产编码 */
    private String assetCode;

    /** 资产名称 */
    private String assetName;

    /** 资产类型 */
    private String assetType;

    /** 分类ID */
    private Long categoryId;

    /** 规格型号 */
    private String specModel;

    /** 序列号 */
    private String serialNo;

    /** 资产状态 */
    private String assetStatus;

    /** 录入来源 */
    private String sourceType;

    /** 取得方式 */
    private String acquireType;

    /** 权属部门ID */
    private Long ownerDeptId;

    /** 使用部门ID */
    private Long useDeptId;

    /** 责任人ID */
    private Long responsibleUserId;

    /** 当前位置 */
    private String locationName;

    /** 原值 */
    private BigDecimal originalValue;

    /** 取得日期 */
    private Date acquisitionDate;

    /** 启用日期 */
    private Date enableDate;

    /** 最近盘点日期 */
    private Date lastInventoryDate;

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

    public String getAssetType()
    {
        return assetType;
    }

    public void setAssetType(String assetType)
    {
        this.assetType = assetType;
    }

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public String getSpecModel()
    {
        return specModel;
    }

    public void setSpecModel(String specModel)
    {
        this.specModel = specModel;
    }

    public String getSerialNo()
    {
        return serialNo;
    }

    public void setSerialNo(String serialNo)
    {
        this.serialNo = serialNo;
    }

    public String getAssetStatus()
    {
        return assetStatus;
    }

    public void setAssetStatus(String assetStatus)
    {
        this.assetStatus = assetStatus;
    }

    public String getSourceType()
    {
        return sourceType;
    }

    public void setSourceType(String sourceType)
    {
        this.sourceType = sourceType;
    }

    public String getAcquireType()
    {
        return acquireType;
    }

    public void setAcquireType(String acquireType)
    {
        this.acquireType = acquireType;
    }

    public Long getOwnerDeptId()
    {
        return ownerDeptId;
    }

    public void setOwnerDeptId(Long ownerDeptId)
    {
        this.ownerDeptId = ownerDeptId;
    }

    public Long getUseDeptId()
    {
        return useDeptId;
    }

    public void setUseDeptId(Long useDeptId)
    {
        this.useDeptId = useDeptId;
    }

    public Long getResponsibleUserId()
    {
        return responsibleUserId;
    }

    public void setResponsibleUserId(Long responsibleUserId)
    {
        this.responsibleUserId = responsibleUserId;
    }

    public String getLocationName()
    {
        return locationName;
    }

    public void setLocationName(String locationName)
    {
        this.locationName = locationName;
    }

    public BigDecimal getOriginalValue()
    {
        return originalValue;
    }

    public void setOriginalValue(BigDecimal originalValue)
    {
        this.originalValue = originalValue;
    }

    public Date getAcquisitionDate()
    {
        return acquisitionDate;
    }

    public void setAcquisitionDate(Date acquisitionDate)
    {
        this.acquisitionDate = acquisitionDate;
    }

    public Date getEnableDate()
    {
        return enableDate;
    }

    public void setEnableDate(Date enableDate)
    {
        this.enableDate = enableDate;
    }

    public Date getLastInventoryDate()
    {
        return lastInventoryDate;
    }

    public void setLastInventoryDate(Date lastInventoryDate)
    {
        this.lastInventoryDate = lastInventoryDate;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("assetId", getAssetId())
            .append("assetCode", getAssetCode())
            .append("assetName", getAssetName())
            .append("assetType", getAssetType())
            .append("categoryId", getCategoryId())
            .append("specModel", getSpecModel())
            .append("serialNo", getSerialNo())
            .append("assetStatus", getAssetStatus())
            .append("sourceType", getSourceType())
            .append("acquireType", getAcquireType())
            .append("ownerDeptId", getOwnerDeptId())
            .append("useDeptId", getUseDeptId())
            .append("responsibleUserId", getResponsibleUserId())
            .append("locationName", getLocationName())
            .append("originalValue", getOriginalValue())
            .append("acquisitionDate", getAcquisitionDate())
            .append("enableDate", getEnableDate())
            .append("lastInventoryDate", getLastInventoryDate())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
