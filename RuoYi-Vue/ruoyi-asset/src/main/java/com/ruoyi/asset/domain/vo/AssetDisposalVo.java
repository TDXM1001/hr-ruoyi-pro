package com.ruoyi.asset.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产处置视图对象。
 *
 * @author Codex
 */
public class AssetDisposalVo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 处置单ID */
    private Long disposalId;

    /** 处置单号 */
    @Excel(name = "处置单号")
    private String disposalNo;

    /** 资产ID */
    private Long assetId;

    /** 资产编码 */
    @Excel(name = "资产编码")
    private String assetCode;

    /** 资产名称 */
    @Excel(name = "资产名称")
    private String assetName;

    /** 处置类型 */
    @Excel(name = "处置类型")
    private String disposalType;

    /** 处置状态 */
    @Excel(name = "处置状态")
    private String disposalStatus;

    /** 处置原因 */
    @Excel(name = "处置原因")
    private String disposalReason;

    /** 处置日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "处置日期", dateFormat = "yyyy-MM-dd")
    private Date disposalDate;

    /** 处置金额 */
    @Excel(name = "处置金额")
    private BigDecimal disposalAmount;

    /** 确认人 */
    @Excel(name = "确认人")
    private String confirmedBy;

    /** 确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "确认时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date confirmedTime;

    /** 财务确认标识 */
    @Excel(name = "财务确认")
    private String financeConfirmFlag;

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

    public String getAssetCode()
    {
        return assetCode;
    }

    public void setAssetCode(String assetCode)
    {
        this.assetCode = assetCode;
    }

    public String getAssetName()
    {
        return assetName;
    }

    public void setAssetName(String assetName)
    {
        this.assetName = assetName;
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
}
