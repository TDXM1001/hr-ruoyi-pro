package com.ruoyi.asset.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产处置对象。
 *
 * @author Codex
 */
public class AssetDisposal extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 处置单ID */
    private Long disposalId;

    /** 处置单号 */
    private String disposalNo;

    /** 资产ID */
    private Long assetId;

    /** 处置类型 */
    private String disposalType;

    /** 处置状态 */
    private String disposalStatus;

    /** 处置原因 */
    private String disposalReason;

    /** 处置日期 */
    private Date disposalDate;

    /** 处置金额 */
    private BigDecimal disposalAmount;

    /** 确认人 */
    private String confirmedBy;

    /** 确认时间 */
    private Date confirmedTime;

    /** 财务确认标识 */
    private String financeConfirmFlag;

    /** 财务确认人 */
    private String financeConfirmBy;

    /** 财务确认时间 */
    private Date financeConfirmTime;

    public Long getDisposalId()
    {
        return disposalId;
    }

    public void setDisposalId(Long disposalId)
    {
        this.disposalId = disposalId;
    }

    public String getDisposalNo()
    {
        return disposalNo;
    }

    public void setDisposalNo(String disposalNo)
    {
        this.disposalNo = disposalNo;
    }

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

    public String getDisposalType()
    {
        return disposalType;
    }

    public void setDisposalType(String disposalType)
    {
        this.disposalType = disposalType;
    }

    public String getDisposalStatus()
    {
        return disposalStatus;
    }

    public void setDisposalStatus(String disposalStatus)
    {
        this.disposalStatus = disposalStatus;
    }

    public String getDisposalReason()
    {
        return disposalReason;
    }

    public void setDisposalReason(String disposalReason)
    {
        this.disposalReason = disposalReason;
    }

    public Date getDisposalDate()
    {
        return disposalDate;
    }

    public void setDisposalDate(Date disposalDate)
    {
        this.disposalDate = disposalDate;
    }

    public BigDecimal getDisposalAmount()
    {
        return disposalAmount;
    }

    public void setDisposalAmount(BigDecimal disposalAmount)
    {
        this.disposalAmount = disposalAmount;
    }

    public String getConfirmedBy()
    {
        return confirmedBy;
    }

    public void setConfirmedBy(String confirmedBy)
    {
        this.confirmedBy = confirmedBy;
    }

    public Date getConfirmedTime()
    {
        return confirmedTime;
    }

    public void setConfirmedTime(Date confirmedTime)
    {
        this.confirmedTime = confirmedTime;
    }

    public String getFinanceConfirmFlag()
    {
        return financeConfirmFlag;
    }

    public void setFinanceConfirmFlag(String financeConfirmFlag)
    {
        this.financeConfirmFlag = financeConfirmFlag;
    }

    public String getFinanceConfirmBy()
    {
        return financeConfirmBy;
    }

    public void setFinanceConfirmBy(String financeConfirmBy)
    {
        this.financeConfirmBy = financeConfirmBy;
    }

    public Date getFinanceConfirmTime()
    {
        return financeConfirmTime;
    }

    public void setFinanceConfirmTime(Date financeConfirmTime)
    {
        this.financeConfirmTime = financeConfirmTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("disposalId", getDisposalId())
            .append("disposalNo", getDisposalNo())
            .append("assetId", getAssetId())
            .append("disposalType", getDisposalType())
            .append("disposalStatus", getDisposalStatus())
            .append("disposalReason", getDisposalReason())
            .append("disposalDate", getDisposalDate())
            .append("disposalAmount", getDisposalAmount())
            .append("confirmedBy", getConfirmedBy())
            .append("confirmedTime", getConfirmedTime())
            .append("financeConfirmFlag", getFinanceConfirmFlag())
            .append("financeConfirmBy", getFinanceConfirmBy())
            .append("financeConfirmTime", getFinanceConfirmTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
