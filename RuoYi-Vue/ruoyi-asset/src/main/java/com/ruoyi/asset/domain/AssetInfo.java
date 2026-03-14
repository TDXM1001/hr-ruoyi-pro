package com.ruoyi.asset.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 资产主档对象 asset_info
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public class AssetInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 资产ID */
    private Long assetId;

    /** 资产编号 */
    @Excel(name = "资产编号")
    private String assetNo;

    /** 资产名称 */
    @Excel(name = "资产名称")
    private String assetName;

    /** 分类ID */
    private Long categoryId;

    /** 资产类型：1=固定资产 2=不动产 */
    @Excel(name = "资产类型", readConverterExp = "1=固定资产,2=不动产")
    private String assetType;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String specModel;

    /** 计量单位 */
    @Excel(name = "计量单位")
    private String unit;

    /** 权属组织ID */
    private Long ownershipOrgId;

    /** 归口管理部门ID */
    private Long manageDeptId;

    /** 使用部门ID */
    private Long useDeptId;

    /** 责任人ID */
    private Long responsibleUserId;

    /** 当前使用人ID */
    private Long userId;

    /** 位置ID */
    private Long locationId;

    /** 位置描述 */
    @Excel(name = "位置描述")
    private String locationText;

    /** 取得方式 */
    @Excel(name = "取得方式")
    private String acquireMethod;

    /** 购置日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "购置日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date purchaseDate;

    /** 入账日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "入账日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date capitalizationDate;

    /** 启用日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "启用日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date enableDate;

    /** 资产状态 */
    @Excel(name = "资产状态", readConverterExp = "1=在用,2=领用中,3=维修中,4=盘点中,5=已报废,6=已处置,7=闲置")
    private String assetStatus;

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getAssetNo() {
        return assetNo;
    }

    public void setAssetNo(String assetNo) {
        this.assetNo = assetNo;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getSpecModel() {
        return specModel;
    }

    public void setSpecModel(String specModel) {
        this.specModel = specModel;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Long getOwnershipOrgId() {
        return ownershipOrgId;
    }

    public void setOwnershipOrgId(Long ownershipOrgId) {
        this.ownershipOrgId = ownershipOrgId;
    }

    public Long getManageDeptId() {
        return manageDeptId;
    }

    public void setManageDeptId(Long manageDeptId) {
        this.manageDeptId = manageDeptId;
    }

    public Long getUseDeptId() {
        return useDeptId;
    }

    public void setUseDeptId(Long useDeptId) {
        this.useDeptId = useDeptId;
    }

    public Long getResponsibleUserId() {
        return responsibleUserId;
    }

    public void setResponsibleUserId(Long responsibleUserId) {
        this.responsibleUserId = responsibleUserId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    public String getAcquireMethod() {
        return acquireMethod;
    }

    public void setAcquireMethod(String acquireMethod) {
        this.acquireMethod = acquireMethod;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Date getCapitalizationDate() {
        return capitalizationDate;
    }

    public void setCapitalizationDate(Date capitalizationDate) {
        this.capitalizationDate = capitalizationDate;
    }

    public Date getEnableDate() {
        return enableDate;
    }

    public void setEnableDate(Date enableDate) {
        this.enableDate = enableDate;
    }

    public String getAssetStatus() {
        return assetStatus;
    }

    public void setAssetStatus(String assetStatus) {
        this.assetStatus = assetStatus;
    }

    /**
     * 兼容旧调用方写入使用部门。
     */
    public void setDeptId(Long deptId) {
        this.useDeptId = deptId;
    }

    /**
     * 兼容旧调用方写入资产状态。
     */
    public void setStatus(String status) {
        this.assetStatus = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("assetId", getAssetId())
            .append("assetNo", getAssetNo())
            .append("assetName", getAssetName())
            .append("categoryId", getCategoryId())
            .append("assetType", getAssetType())
            .append("specModel", getSpecModel())
            .append("unit", getUnit())
            .append("ownershipOrgId", getOwnershipOrgId())
            .append("manageDeptId", getManageDeptId())
            .append("useDeptId", getUseDeptId())
            .append("responsibleUserId", getResponsibleUserId())
            .append("userId", getUserId())
            .append("locationId", getLocationId())
            .append("locationText", getLocationText())
            .append("acquireMethod", getAcquireMethod())
            .append("purchaseDate", getPurchaseDate())
            .append("capitalizationDate", getCapitalizationDate())
            .append("enableDate", getEnableDate())
            .append("assetStatus", getAssetStatus())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
