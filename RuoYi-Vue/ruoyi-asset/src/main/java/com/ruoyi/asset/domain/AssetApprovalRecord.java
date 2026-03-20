package com.ruoyi.asset.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产审批记录对象。
 *
 * <p>用于沉淀审批轨迹与结果，后续可用于审批页和详情页回放。</p>
 *
 * @author Codex
 */
public class AssetApprovalRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long approvalId;

    /** 资产ID */
    private Long assetId;

    /** 审批类型 */
    private String approvalType;

    /** 审批状态 */
    private String approvalStatus;

    /** 审批意见 */
    private String opinion;

    public Long getApprovalId()
    {
        return approvalId;
    }

    public void setApprovalId(Long approvalId)
    {
        this.approvalId = approvalId;
    }

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

    public String getApprovalType()
    {
        return approvalType;
    }

    public void setApprovalType(String approvalType)
    {
        this.approvalType = approvalType;
    }

    public String getApprovalStatus()
    {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus)
    {
        this.approvalStatus = approvalStatus;
    }

    public String getOpinion()
    {
        return opinion;
    }

    public void setOpinion(String opinion)
    {
        this.opinion = opinion;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("approvalId", getApprovalId())
            .append("assetId", getAssetId())
            .append("approvalType", getApprovalType())
            .append("approvalStatus", getApprovalStatus())
            .append("opinion", getOpinion())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}

