package com.ruoyi.asset.domain.bo;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 资产台账业务入参对象。
 * <p>
 * 该对象同时承担列表筛选与新增、修改入参的基础字段，
 * 其中建账核心字段通过校验注解进行前置约束。
 * </p>
 *
 * @author Codex
 */
public class AssetLedgerBo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 资产ID */
    private Long assetId;

    /** 资产编码 */
    @Size(max = 64, message = "资产编码长度不能超过64个字符")
    private String assetCode;

    /** 资产名称 */
    @NotBlank(message = "资产名称不能为空")
    @Size(max = 120, message = "资产名称长度不能超过120个字符")
    private String assetName;

    /** 资产类型 */
    @NotBlank(message = "资产类型不能为空")
    private String assetType;

    /** 分类ID */
    @NotNull(message = "资产分类不能为空")
    private Long categoryId;

    /** 规格型号 */
    @Size(max = 255, message = "规格型号长度不能超过255个字符")
    private String specModel;

    /** 序列号 */
    @Size(max = 100, message = "序列号长度不能超过100个字符")
    private String serialNo;

    /** 资产状态 */
    private String assetStatus;

    /** 录入来源 */
    @NotBlank(message = "录入来源不能为空")
    private String sourceType;

    /** 取得方式 */
    private String acquireType;

    /** 权属部门ID */
    @NotNull(message = "权属部门不能为空")
    private Long ownerDeptId;

    /** 使用部门ID */
    private Long useDeptId;

    /** 责任人ID */
    private Long responsibleUserId;

    /** 当前位置 */
    @Size(max = 200, message = "位置描述长度不能超过200个字符")
    private String locationName;

    /** 原值 */
    private BigDecimal originalValue;

    /** 取得日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date acquisitionDate;

    /** 启用日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date enableDate;

    /** 最近盘点日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
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
}
