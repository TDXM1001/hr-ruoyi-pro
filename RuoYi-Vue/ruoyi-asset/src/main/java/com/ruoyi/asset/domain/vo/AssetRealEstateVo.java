package com.ruoyi.asset.domain.vo;

import java.math.BigDecimal;

/**
 * 不动产档案展示对象。
 *
 * <p>在统一台账可读信息之外补齐权属档案字段，
 * 供列表页、详情页和表单回填直接消费。</p>
 *
 * @author Codex
 */
public class AssetRealEstateVo extends AssetLedgerVo
{
    private static final long serialVersionUID = 1L;

    /** 档案ID */
    private Long profileId;

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
}
