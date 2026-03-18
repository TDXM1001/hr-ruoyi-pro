package com.ruoyi.asset.domain.vo;

/**
 * 资产树形选项平铺节点。
 *
 * <p>用于承接资产分类树、部门树等需要在控制层组装为 TreeSelect
 * 的平铺数据结构，避免直接在 Mapper 层拼装递归 children。</p>
 *
 * @author Codex
 */
public class AssetTreeNodeVo
{
    /** 节点ID */
    private Long id;

    /** 父节点ID */
    private Long parentId;

    /** 节点名称 */
    private String label;

    /** 排序号 */
    private Integer orderNum;

    /** 是否禁用 */
    private Boolean disabled;

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getParentId()
    {
        return parentId;
    }

    public void setParentId(Long parentId)
    {
        this.parentId = parentId;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public Integer getOrderNum()
    {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum)
    {
        this.orderNum = orderNum;
    }

    public Boolean getDisabled()
    {
        return disabled;
    }

    public void setDisabled(Boolean disabled)
    {
        this.disabled = disabled;
    }
}
