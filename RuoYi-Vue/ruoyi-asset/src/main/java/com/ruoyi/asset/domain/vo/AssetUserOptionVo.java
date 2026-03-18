package com.ruoyi.asset.domain.vo;

/**
 * 资产责任人下拉选项。
 *
 * <p>前端远程搜索只需要 value/label 即可完成选择，
 * 这里保持返回结构轻量，降低联调心智负担。</p>
 *
 * @author Codex
 */
public class AssetUserOptionVo
{
    /** 选项值 */
    private Long value;

    /** 选项展示文案 */
    private String label;

    public Long getValue()
    {
        return value;
    }

    public void setValue(Long value)
    {
        this.value = value;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }
}
