package com.ruoyi.asset.domain.bo;

import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 不动产整改审批动作入参。
 *
 * <p>本批仅承载审批意见，不引入多节点审批人分配等复杂能力。</p>
 *
 * @author Codex
 */
public class AssetRectificationApprovalActionBo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 审批意见 */
    @NotBlank(message = "审批意见不能为空")
    @Size(max = 500, message = "审批意见长度不能超过500个字符")
    private String opinion;

    public String getOpinion()
    {
        return opinion;
    }

    public void setOpinion(String opinion)
    {
        this.opinion = opinion;
    }
}
