package com.ruoyi.asset.enums;

/**
 * 资产状态枚举。
 * <p>
 * 当前只保留固定资产一期最小闭环需要的状态，
 * 后续新增状态时应优先评估是否会改变既有业务流转规则。
 * </p>
 *
 * @author Codex
 */
public enum AssetStatus
{
    /** 草稿 */
    DRAFT("DRAFT", "草稿"),

    /** 在册 */
    IN_LEDGER("IN_LEDGER", "在册"),

    /** 使用中 */
    IN_USE("IN_USE", "使用中"),

    /** 闲置中 */
    IDLE("IDLE", "闲置中"),

    /** 盘点中 */
    INVENTORYING("INVENTORYING", "盘点中"),

    /** 待处置 */
    PENDING_DISPOSAL("PENDING_DISPOSAL", "待处置"),

    /** 已处置 */
    DISPOSED("DISPOSED", "已处置");

    /** 状态编码 */
    private final String code;

    /** 状态说明 */
    private final String description;

    AssetStatus(String code, String description)
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
