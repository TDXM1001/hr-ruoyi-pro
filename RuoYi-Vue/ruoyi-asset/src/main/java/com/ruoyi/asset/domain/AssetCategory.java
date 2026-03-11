package com.ruoyi.asset.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 资产分类对象 asset_category
 *
 * @author ruoyi
 * @date 2026-03-11
 */
public class AssetCategory extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 分类ID */
    private Long id;

    /** 父节点ID */
    @Excel(name = "父节点ID")
    private Long parentId;

    /** 分类名称 */
    @Excel(name = "分类名称")
    private String name;

    /** 分类编码 */
    @Excel(name = "分类编码")
    private String code;

    /** 层级 */
    @Excel(name = "层级")
    private Integer level;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("parentId", getParentId())
            .append("name", getName())
            .append("code", getCode())
            .append("level", getLevel())
            .append("createTime", getCreateTime())
            .toString();
    }
}
