package com.ruoyi.asset.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 不动产对象 asset_real_estate
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public class AssetRealEstate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long realEstateId;
    private Long assetId;
    private String propertyCertNo;
    private String legacyCertNo;
    private String realEstateUnitNo;
    private String addressFull;
    private String landNature;
    private String landUse;
    private String buildingUse;
    private BigDecimal landArea;
    private BigDecimal sharedLandArea;
    private BigDecimal buildingArea;
    private BigDecimal innerArea;
    private String buildingStructure;
    private String buildingNo;
    private String floorNo;
    private String roomNo;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date completionDate;
    private Integer builtYear;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date landTermStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date landTermEndDate;
    private String rightsType;
    private String rightsHolder;
    private String coOwnershipType;
    private String mortgageStatus;
    private String mortgagee;
    private String seizureStatus;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date registrationDate;

    public Long getRealEstateId() {
        return realEstateId;
    }

    public void setRealEstateId(Long realEstateId) {
        this.realEstateId = realEstateId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getPropertyCertNo() {
        return propertyCertNo;
    }

    public void setPropertyCertNo(String propertyCertNo) {
        this.propertyCertNo = propertyCertNo;
    }

    public String getLegacyCertNo() {
        return legacyCertNo;
    }

    public void setLegacyCertNo(String legacyCertNo) {
        this.legacyCertNo = legacyCertNo;
    }

    public String getRealEstateUnitNo() {
        return realEstateUnitNo;
    }

    public void setRealEstateUnitNo(String realEstateUnitNo) {
        this.realEstateUnitNo = realEstateUnitNo;
    }

    public String getAddressFull() {
        return addressFull;
    }

    public void setAddressFull(String addressFull) {
        this.addressFull = addressFull;
    }

    public String getLandNature() {
        return landNature;
    }

    public void setLandNature(String landNature) {
        this.landNature = landNature;
    }

    public String getLandUse() {
        return landUse;
    }

    public void setLandUse(String landUse) {
        this.landUse = landUse;
    }

    public String getBuildingUse() {
        return buildingUse;
    }

    public void setBuildingUse(String buildingUse) {
        this.buildingUse = buildingUse;
    }

    public BigDecimal getLandArea() {
        return landArea;
    }

    public void setLandArea(BigDecimal landArea) {
        this.landArea = landArea;
    }

    public BigDecimal getSharedLandArea() {
        return sharedLandArea;
    }

    public void setSharedLandArea(BigDecimal sharedLandArea) {
        this.sharedLandArea = sharedLandArea;
    }

    public BigDecimal getBuildingArea() {
        return buildingArea;
    }

    public void setBuildingArea(BigDecimal buildingArea) {
        this.buildingArea = buildingArea;
    }

    public BigDecimal getInnerArea() {
        return innerArea;
    }

    public void setInnerArea(BigDecimal innerArea) {
        this.innerArea = innerArea;
    }

    public String getBuildingStructure() {
        return buildingStructure;
    }

    public void setBuildingStructure(String buildingStructure) {
        this.buildingStructure = buildingStructure;
    }

    public String getBuildingNo() {
        return buildingNo;
    }

    public void setBuildingNo(String buildingNo) {
        this.buildingNo = buildingNo;
    }

    public String getFloorNo() {
        return floorNo;
    }

    public void setFloorNo(String floorNo) {
        this.floorNo = floorNo;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public Integer getBuiltYear() {
        return builtYear;
    }

    public void setBuiltYear(Integer builtYear) {
        this.builtYear = builtYear;
    }

    public Date getLandTermStartDate() {
        return landTermStartDate;
    }

    public void setLandTermStartDate(Date landTermStartDate) {
        this.landTermStartDate = landTermStartDate;
    }

    public Date getLandTermEndDate() {
        return landTermEndDate;
    }

    public void setLandTermEndDate(Date landTermEndDate) {
        this.landTermEndDate = landTermEndDate;
    }

    public String getRightsType() {
        return rightsType;
    }

    public void setRightsType(String rightsType) {
        this.rightsType = rightsType;
    }

    public String getRightsHolder() {
        return rightsHolder;
    }

    public void setRightsHolder(String rightsHolder) {
        this.rightsHolder = rightsHolder;
    }

    public String getCoOwnershipType() {
        return coOwnershipType;
    }

    public void setCoOwnershipType(String coOwnershipType) {
        this.coOwnershipType = coOwnershipType;
    }

    public String getMortgageStatus() {
        return mortgageStatus;
    }

    public void setMortgageStatus(String mortgageStatus) {
        this.mortgageStatus = mortgageStatus;
    }

    public String getMortgagee() {
        return mortgagee;
    }

    public void setMortgagee(String mortgagee) {
        this.mortgagee = mortgagee;
    }

    public String getSeizureStatus() {
        return seizureStatus;
    }

    public void setSeizureStatus(String seizureStatus) {
        this.seizureStatus = seizureStatus;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("realEstateId", getRealEstateId())
            .append("assetId", getAssetId())
            .append("propertyCertNo", getPropertyCertNo())
            .append("legacyCertNo", getLegacyCertNo())
            .append("realEstateUnitNo", getRealEstateUnitNo())
            .append("addressFull", getAddressFull())
            .append("landNature", getLandNature())
            .append("landUse", getLandUse())
            .append("buildingUse", getBuildingUse())
            .append("landArea", getLandArea())
            .append("sharedLandArea", getSharedLandArea())
            .append("buildingArea", getBuildingArea())
            .append("innerArea", getInnerArea())
            .append("buildingStructure", getBuildingStructure())
            .append("buildingNo", getBuildingNo())
            .append("floorNo", getFloorNo())
            .append("roomNo", getRoomNo())
            .append("completionDate", getCompletionDate())
            .append("builtYear", getBuiltYear())
            .append("landTermStartDate", getLandTermStartDate())
            .append("landTermEndDate", getLandTermEndDate())
            .append("rightsType", getRightsType())
            .append("rightsHolder", getRightsHolder())
            .append("coOwnershipType", getCoOwnershipType())
            .append("mortgageStatus", getMortgageStatus())
            .append("mortgagee", getMortgagee())
            .append("seizureStatus", getSeizureStatus())
            .append("registrationDate", getRegistrationDate())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
