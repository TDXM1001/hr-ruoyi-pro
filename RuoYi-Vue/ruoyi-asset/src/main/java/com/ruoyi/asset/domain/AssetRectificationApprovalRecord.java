package com.ruoyi.asset.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 不动产整改审批轨迹对象。
 *
 * <p>本表只服务于整改域审批挂载位，记录提交、通过、驳回三个动作，
 * 暂不复用通用审批记录，避免与当前工作区的审批脏文件发生耦合。</p>
 *
 * @author Codex
 */
public class AssetRectificationApprovalRecord
{
    private Long approvalRecordId;

    private Long rectificationId;

    private Long assetId;

    private String approvalStatus;

    private String opinion;

    private String operateBy;

    private Date operateTime;

    public Long getApprovalRecordId()
    {
        return approvalRecordId;
    }

    public void setApprovalRecordId(Long approvalRecordId)
    {
        this.approvalRecordId = approvalRecordId;
    }

    public Long getRectificationId()
    {
        return rectificationId;
    }

    public void setRectificationId(Long rectificationId)
    {
        this.rectificationId = rectificationId;
    }

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
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

    public String getOperateBy()
    {
        return operateBy;
    }

    public void setOperateBy(String operateBy)
    {
        this.operateBy = operateBy;
    }

    public Date getOperateTime()
    {
        return operateTime;
    }

    public void setOperateTime(Date operateTime)
    {
        this.operateTime = operateTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("approvalRecordId", getApprovalRecordId())
            .append("rectificationId", getRectificationId())
            .append("assetId", getAssetId())
            .append("approvalStatus", getApprovalStatus())
            .append("opinion", getOpinion())
            .append("operateBy", getOperateBy())
            .append("operateTime", getOperateTime())
            .toString();
    }
}
