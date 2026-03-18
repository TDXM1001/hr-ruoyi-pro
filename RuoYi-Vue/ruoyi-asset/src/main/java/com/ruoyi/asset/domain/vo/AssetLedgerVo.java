package com.ruoyi.asset.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产台账展示对象。
 * <p>
 * 该对象用于台账列表、详情与导出场景，
 * 在主档基础上补齐分类、部门、责任人等可读名称，降低前端二次组装成本。
 * </p>
 *
 * @author Codex
 */
public class AssetLedgerVo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 资产ID */
    @Excel(name = "资产ID")
    private Long assetId;

    /** 资产编码 */
    @Excel(name = "资产编码")
    private String assetCode;

    /** 资产名称 */
    @Excel(name = "资产名称")
    private String assetName;

    /** 资产类型 */
    @Excel(name = "资产类型")
    private String assetType;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称 */
    @Excel(name = "资产分类")
    private String categoryName;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String specModel;

    /** 序列号 */
    @Excel(name = "序列号")
    private String serialNo;

    /** 资产状态 */
    @Excel(name = "资产状态")
    private String assetStatus;

    /** 录入来源 */
    @Excel(name = "录入来源")
    private String sourceType;

    /** 取得方式 */
    @Excel(name = "取得方式")
    private String acquireType;

    /** 权属部门ID */
    private Long ownerDeptId;

    /** 权属部门名称 */
    @Excel(name = "权属部门")
    private String ownerDeptName;

    /** 使用部门ID */
    private Long useDeptId;

    /** 使用部门名称 */
    @Excel(name = "使用部门")
    private String useDeptName;

    /** 责任人ID */
    private Long responsibleUserId;

    /** 责任人名称 */
    @Excel(name = "责任人")
    private String responsibleUserName;

    /** 当前位置 */
    @Excel(name = "当前位置")
    private String locationName;

    /** 原值 */
    @Excel(name = "资产原值")
    private BigDecimal originalValue;

    /** 取得日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "取得日期", dateFormat = "yyyy-MM-dd")
    private Date acquisitionDate;

    /** 启用日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "启用日期", dateFormat = "yyyy-MM-dd")
    private Date enableDate;

    /** 最近盘点日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最近盘点日期", dateFormat = "yyyy-MM-dd")
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

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
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

    public String getOwnerDeptName()
    {
        return ownerDeptName;
    }

    public void setOwnerDeptName(String ownerDeptName)
    {
        this.ownerDeptName = ownerDeptName;
    }

    public Long getUseDeptId()
    {
        return useDeptId;
    }

    public void setUseDeptId(Long useDeptId)
    {
        this.useDeptId = useDeptId;
    }

    public String getUseDeptName()
    {
        return useDeptName;
    }

    public void setUseDeptName(String useDeptName)
    {
        this.useDeptName = useDeptName;
    }

    public Long getResponsibleUserId()
    {
        return responsibleUserId;
    }

    public void setResponsibleUserId(Long responsibleUserId)
    {
        this.responsibleUserId = responsibleUserId;
    }

    public String getResponsibleUserName()
    {
        return responsibleUserName;
    }

    public void setResponsibleUserName(String responsibleUserName)
    {
        this.responsibleUserName = responsibleUserName;
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
