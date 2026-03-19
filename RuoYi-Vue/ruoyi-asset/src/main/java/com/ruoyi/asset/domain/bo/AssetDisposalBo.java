package com.ruoyi.asset.domain.bo;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 资产处置业务入参。
 *
 * @author Codex
 */
public class AssetDisposalBo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 处置单ID */
    private Long disposalId;

    /** 处置单号 */
    private String disposalNo;

    /** 资产ID */
    @NotNull(message = "资产ID不能为空")
    private Long assetId;

    /** 处置类型 */
    @NotBlank(message = "处置类型不能为空")
    private String disposalType;

    /** 处置状态 */
    private String disposalStatus;

    /** 处置原因 */
    @NotBlank(message = "处置原因不能为空")
    @Size(max = 255, message = "处置原因长度不能超过255个字符")
    private String disposalReason;

    /** 处置日期 */
    @NotNull(message = "处置日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date disposalDate;

    /** 处置金额 */
    private BigDecimal disposalAmount;

    /** 财务确认标识 */
    private String financeConfirmFlag;

    /** 备注 */
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

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

    public String getFinanceConfirmFlag()
    {
        return financeConfirmFlag;
    }

    public void setFinanceConfirmFlag(String financeConfirmFlag)
    {
        this.financeConfirmFlag = financeConfirmFlag;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
