package com.ruoyi.asset.domain.bo;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 不动产档案业务入参对象。
 *
 * <p>该对象同时承载统一台账字段与不动产权属扩展字段，
 * 用于列表筛选、新建建档和编辑档案三类场景。</p>
 *
 * @author Codex
 */
public class AssetRealEstateBo extends AssetLedgerBo
{
    private static final long serialVersionUID = 1L;

    /** 权属证号 */
    @NotBlank(message = "权属证号不能为空")
    @Size(max = 64, message = "权属证号长度不能超过64个字符")
    private String ownershipCertNo;

    /** 土地用途 */
    @NotBlank(message = "土地用途不能为空")
    @Size(max = 64, message = "土地用途长度不能超过64个字符")
    private String landUseType;

    /** 建筑面积 */
    @NotNull(message = "建筑面积不能为空")
    private BigDecimal buildingArea;

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
