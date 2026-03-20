package com.ruoyi.asset.enums;

/**
 * 审批状态枚举。
 *
 * @author Codex
 */
public enum AssetApprovalStatus
{
    /** 已提交 */
    SUBMITTED("SUBMITTED", "已提交"),

    /** 审批通过 */
    APPROVED("APPROVED", "审批通过"),

    /** 审批驳回 */
    REJECTED("REJECTED", "审批驳回");

    private final String code;

    private final String description;

    AssetApprovalStatus(String code, String description)
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

