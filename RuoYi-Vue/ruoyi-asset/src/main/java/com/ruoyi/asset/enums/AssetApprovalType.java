package com.ruoyi.asset.enums;

/**
 * 审批类型枚举。
 *
 * @author Codex
 */
public enum AssetApprovalType
{
    /** 处置审批 */
    DISPOSAL("DISPOSAL", "处置审批"),

    /** 整改审批 */
    RECTIFICATION("RECTIFICATION", "整改审批"),

    /** 高价值资产审批 */
    HIGH_VALUE("HIGH_VALUE", "高价值资产审批");

    private final String code;

    private final String description;

    AssetApprovalType(String code, String description)
    {
        this.code = code;
        this.description = description;
    }

    public String getCode()
    {
        return code;
    }

    public String getDescription()
    {
        return description;
    }
}

