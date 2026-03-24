package com.ruoyi.asset.domain.bo;

import jakarta.validation.constraints.NotBlank;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 审批动作请求对象。
 *
 * <p>当前用于处置审批通过/驳回动作，后续可复用于整改审批等场景。</p>
 *
 * @author Codex
 */
public class AssetApprovalActionBo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 审批意见 */
    @NotBlank(message = "审批意见不能为空")
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
