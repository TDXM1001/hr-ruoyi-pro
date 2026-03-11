package com.ruoyi.asset.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 资产信息主表对象 asset_info
 *
 * @author ruoyi
 * @date 2026-03-11
 */
public class AssetInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 资产编号 */
    private String assetNo;

    /** 资产名称 */
    @Excel(name = "资产名称")
    private String assetName;

    /** 分类ID */
    @Excel(name = "分类ID")
    private Long categoryId;

    /** 类型：1=不动产 2=固定资产 */
    @Excel(name = "类型", readConverterExp = "1=不动产,2=固定资产")
    private Integer assetType;

    /** 归属部门 */
    @Excel(name = "归属部门")
    private Long deptId;

    /** 责任人 */
    @Excel(name = "责任人")
    private Long userId;

    /** 状态：1=正常 2=领用中 3=维修中 4=盘点中 5=已报废 */
    @Excel(name = "状态", readConverterExp = "1=正常,2=领用中,3=维修中,4=盘点中,5=已报废")
    private Integer status;

    public void setAssetNo(String assetNo) {
        this.assetNo = assetNo;
    }

    public String getAssetNo() {
        return assetNo;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setAssetType(Integer assetType) {
        this.assetType = assetType;
    }

    public Integer getAssetType() {
        return assetType;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("assetNo", getAssetNo())
            .append("assetName", getAssetName())
            .append("categoryId", getCategoryId())
            .append("assetType", getAssetType())
            .append("deptId", getDeptId())
            .append("userId", getUserId())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .toString();
    }
}
