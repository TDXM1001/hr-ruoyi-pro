package com.ruoyi.asset.domain.bo;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 资产整改完成入参。
 *
 * <p>独立完成页只负责确认整改已落地，并补充完成说明与验收备注，
 * 不再承担整改登记基础信息维护，避免编辑与完成语义混杂。</p>
 *
 * @author Codex
 */
public class AssetRectificationCompleteBo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 完成说明 */
    @NotBlank(message = "完成说明不能为空")
    @Size(max = 500, message = "完成说明长度不能超过500个字符")
    private String completionDesc;

    /** 验收备注 */
    @Size(max = 500, message = "验收备注长度不能超过500个字符")
    private String acceptanceRemark;

    public String getCompletionDesc()
    {
        return completionDesc;
    }

    public void setCompletionDesc(String completionDesc)
    {
        this.completionDesc = completionDesc;
    }

    public String getAcceptanceRemark()
    {
        return acceptanceRemark;
    }

    public void setAcceptanceRemark(String acceptanceRemark)
    {
        this.acceptanceRemark = acceptanceRemark;
    }
}
