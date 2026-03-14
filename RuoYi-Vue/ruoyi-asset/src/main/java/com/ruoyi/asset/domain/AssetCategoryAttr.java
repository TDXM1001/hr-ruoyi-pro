package com.ruoyi.asset.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 资产分类扩展字段定义对象 asset_category_attr
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public class AssetCategoryAttr extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long attrId;
    private Long categoryId;
    private String attrCode;
    private String attrName;
    private String attrType;
    private String dataType;
    private String isRequired;
    private String isUnique;
    private String isListDisplay;
    private String isQueryCondition;
    private String defaultValue;
    private String optionSourceType;
    private String optionSource;
    private String validationRule;
    private Integer sortOrder;
    private String status;

    public Long getAttrId() {
        return attrId;
    }

    public void setAttrId(Long attrId) {
        this.attrId = attrId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getAttrCode() {
        return attrCode;
    }

    public void setAttrCode(String attrCode) {
        this.attrCode = attrCode;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getAttrType() {
        return attrType;
    }

    public void setAttrType(String attrType) {
        this.attrType = attrType;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(String isRequired) {
        this.isRequired = isRequired;
    }

    public String getIsUnique() {
        return isUnique;
    }

    public void setIsUnique(String isUnique) {
        this.isUnique = isUnique;
    }

    public String getIsListDisplay() {
        return isListDisplay;
    }

    public void setIsListDisplay(String isListDisplay) {
        this.isListDisplay = isListDisplay;
    }

    public String getIsQueryCondition() {
        return isQueryCondition;
    }

    public void setIsQueryCondition(String isQueryCondition) {
        this.isQueryCondition = isQueryCondition;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getOptionSourceType() {
        return optionSourceType;
    }

    public void setOptionSourceType(String optionSourceType) {
        this.optionSourceType = optionSourceType;
    }

    public String getOptionSource() {
        return optionSource;
    }

    public void setOptionSource(String optionSource) {
        this.optionSource = optionSource;
    }

    public String getValidationRule() {
        return validationRule;
    }

    public void setValidationRule(String validationRule) {
        this.validationRule = validationRule;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("attrId", getAttrId())
            .append("categoryId", getCategoryId())
            .append("attrCode", getAttrCode())
            .append("attrName", getAttrName())
            .append("attrType", getAttrType())
            .append("dataType", getDataType())
            .append("isRequired", getIsRequired())
            .append("isUnique", getIsUnique())
            .append("isListDisplay", getIsListDisplay())
            .append("isQueryCondition", getIsQueryCondition())
            .append("defaultValue", getDefaultValue())
            .append("optionSourceType", getOptionSourceType())
            .append("optionSource", getOptionSource())
            .append("validationRule", getValidationRule())
            .append("sortOrder", getSortOrder())
            .append("status", getStatus())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
