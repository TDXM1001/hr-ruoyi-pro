package com.ruoyi.asset.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 不动产权属扩展档案。
 *
 * <p>该对象用于承载不动产在统一台账之外的权属补充信息，
 * 供资产管理员在详情页进行权属核查和追溯。</p>
 *
 * @author Codex
 */
public class AssetRealEstateProfile extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 档案ID */
    private Long profileId;

    /** 资产ID */
    private Long assetId;

    /** 权属证号 */
    private String ownershipCertNo;

    /** 土地用途 */
    private String landUseType;

    /** 建筑面积 */
    private BigDecimal buildingArea;

    public Long getProfileId()
    {
        return profileId;
    }

    public void setProfileId(Long profileId)
    {
        this.profileId = profileId;
    }

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

    public String getOwnershipCertNo()
    {
        return ownershipCertNo;
    }

    public void setOwnershipCertNo(String ownershipCertNo)
    {
        this.ownershipCertNo = ownershipCertNo;
    }

    public String getLandUseType()
    {
        return landUseType;
    }

    public void setLandUseType(String landUseType)
    {
        this.landUseType = landUseType;
    }

    public BigDecimal getBuildingArea()
    {
        return buildingArea;
    }

    public void setBuildingArea(BigDecimal buildingArea)
    {
        this.buildingArea = buildingArea;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("profileId", getProfileId())
            .append("assetId", getAssetId())
            .append("ownershipCertNo", getOwnershipCertNo())
            .append("landUseType", getLandUseType())
            .append("buildingArea", getBuildingArea())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

