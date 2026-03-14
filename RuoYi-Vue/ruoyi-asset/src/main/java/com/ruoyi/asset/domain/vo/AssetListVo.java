package com.ruoyi.asset.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;

/**
 * 资产列表视图对象。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public class AssetListVo {
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

    /** 资产类型 */
    @Excel(name = "资产类型", readConverterExp = "1=固定资产,2=不动产")
    private String assetType;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String specModel;

    /** 使用部门ID */
    private Long useDeptId;

    /** 位置描述 */
    @Excel(name = "位置描述")
    private String locationText;

    /** 资产状态 */
    @Excel(name = "资产状态", readConverterExp = "1=在用,2=领用中,3=维修中,4=盘点中,5=已报废,6=已处置,7=闲置")
    private String assetStatus;

    /** 购置日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "购置日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date purchaseDate;

    /** 入账日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "入账日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date capitalizationDate;

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

    public Long getUseDeptId() {
        return useDeptId;
    }

    public void setUseDeptId(Long useDeptId) {
        this.useDeptId = useDeptId;
    }

    public String getLocationText() {
        return locationText;
    }

    public void setLocationText(String locationText) {
        this.locationText = locationText;
    }

    public String getAssetStatus() {
        return assetStatus;
    }

    public void setAssetStatus(String assetStatus) {
        this.assetStatus = assetStatus;
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
}
