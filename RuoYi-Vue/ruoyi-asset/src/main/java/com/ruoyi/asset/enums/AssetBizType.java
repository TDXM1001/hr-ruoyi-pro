package com.ruoyi.asset.enums;

/**
 * 资产业务动作枚举。
 * <p>
 * 该枚举主要用于变更日志和业务单据的动作分类，
 * 确保后续资产轨迹查询时能够统一识别业务来源。
 * </p>
 *
 * @author Codex
 */
public enum AssetBizType
{
    /** 台账建账 */
    LEDGER_CREATE("LEDGER_CREATE", "台账建账"),

    /** 台账修改 */
    LEDGER_UPDATE("LEDGER_UPDATE", "台账修改"),

    /** 资产领用 */
    ASSIGN("ASSIGN", "资产领用"),

    /** 资产调拨 */
    TRANSFER("TRANSFER", "资产调拨"),

    /** 资产退还 */
    RETURN("RETURN", "资产退还"),

    /** 发起盘点 */
    INVENTORY_CREATE("INVENTORY_CREATE", "发起盘点"),

    /** 登记盘点结果 */
    INVENTORY_RESULT("INVENTORY_RESULT", "登记盘点结果"),

    /** 发起处置 */
    DISPOSAL_APPLY("DISPOSAL_APPLY", "发起处置"),

    /** 确认处置 */
    DISPOSAL_CONFIRM("DISPOSAL_CONFIRM", "确认处置");

    /** 业务编码 */
    private final String code;

    /** 业务说明 */
    private final String description;

    AssetBizType(String code, String description)
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
