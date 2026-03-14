package com.ruoyi.asset.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 资产扩展字段值对象 asset_attr_value
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public class AssetAttrValue extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long valueId;
    private Long assetId;
    private Long categoryId;
    private Long attrId;
    private String attrCode;
    private String attrValueText;
    private BigDecimal attrValueNumber;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date attrValueDate;
    private String attrValueJson;

    public Long getValueId() {
        return valueId;
    }

    public void setValueId(Long valueId) {
        this.valueId = valueId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getAttrId() {
        return attrId;
    }

    public void setAttrId(Long attrId) {
        this.attrId = attrId;
    }

    public String getAttrCode() {
        return attrCode;
    }

    public void setAttrCode(String attrCode) {
        this.attrCode = attrCode;
    }

    public String getAttrValueText() {
        return attrValueText;
    }

    public void setAttrValueText(String attrValueText) {
        this.attrValueText = attrValueText;
    }

    public BigDecimal getAttrValueNumber() {
        return attrValueNumber;
    }

    public void setAttrValueNumber(BigDecimal attrValueNumber) {
        this.attrValueNumber = attrValueNumber;
    }

    public Date getAttrValueDate() {
        return attrValueDate;
    }

    public void setAttrValueDate(Date attrValueDate) {
        this.attrValueDate = attrValueDate;
    }

    public String getAttrValueJson() {
        return attrValueJson;
    }

    public void setAttrValueJson(String attrValueJson) {
        this.attrValueJson = attrValueJson;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("valueId", getValueId())
            .append("assetId", getAssetId())
            .append("categoryId", getCategoryId())
            .append("attrId", getAttrId())
            .append("attrCode", getAttrCode())
            .append("attrValueText", getAttrValueText())
            .append("attrValueNumber", getAttrValueNumber())
            .append("attrValueDate", getAttrValueDate())
            .append("attrValueJson", getAttrValueJson())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
