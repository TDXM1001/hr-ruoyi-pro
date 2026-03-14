package com.ruoyi.asset.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 资产财务对象 asset_finance
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public class AssetFinance extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long financeId;
    private Long assetId;
    private String bookType;
    private String currencyCode;
    private BigDecimal originalValue;
    private BigDecimal salvageRate;
    private BigDecimal salvageValue;
    private BigDecimal depreciableValue;
    private String depreciationMethod;
    private Integer usefulLifeMonth;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date depreciationStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date depreciationEndDate;
    private BigDecimal monthlyDepreciationAmount;
    private BigDecimal accumulatedDepreciation;
    private BigDecimal netBookValue;
    private BigDecimal impairmentAmount;
    private BigDecimal bookValue;
    private BigDecimal disposedValue;
    private String financeStatus;
    private String lastDepreciationPeriod;
    private Integer versionNo;

    public Long getFinanceId() {
        return financeId;
    }

    public void setFinanceId(Long financeId) {
        this.financeId = financeId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getBookType() {
        return bookType;
    }

    public void setBookType(String bookType) {
        this.bookType = bookType;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(BigDecimal originalValue) {
        this.originalValue = originalValue;
    }

    public BigDecimal getSalvageRate() {
        return salvageRate;
    }

    public void setSalvageRate(BigDecimal salvageRate) {
        this.salvageRate = salvageRate;
    }

    public BigDecimal getSalvageValue() {
        return salvageValue;
    }

    public void setSalvageValue(BigDecimal salvageValue) {
        this.salvageValue = salvageValue;
    }

    public BigDecimal getDepreciableValue() {
        return depreciableValue;
    }

    public void setDepreciableValue(BigDecimal depreciableValue) {
        this.depreciableValue = depreciableValue;
    }

    public String getDepreciationMethod() {
        return depreciationMethod;
    }

    public void setDepreciationMethod(String depreciationMethod) {
        this.depreciationMethod = depreciationMethod;
    }

    public Integer getUsefulLifeMonth() {
        return usefulLifeMonth;
    }

    public void setUsefulLifeMonth(Integer usefulLifeMonth) {
        this.usefulLifeMonth = usefulLifeMonth;
    }

    public Date getDepreciationStartDate() {
        return depreciationStartDate;
    }

    public void setDepreciationStartDate(Date depreciationStartDate) {
        this.depreciationStartDate = depreciationStartDate;
    }

    public Date getDepreciationEndDate() {
        return depreciationEndDate;
    }

    public void setDepreciationEndDate(Date depreciationEndDate) {
        this.depreciationEndDate = depreciationEndDate;
    }

    public BigDecimal getMonthlyDepreciationAmount() {
        return monthlyDepreciationAmount;
    }

    public void setMonthlyDepreciationAmount(BigDecimal monthlyDepreciationAmount) {
        this.monthlyDepreciationAmount = monthlyDepreciationAmount;
    }

    public BigDecimal getAccumulatedDepreciation() {
        return accumulatedDepreciation;
    }

    public void setAccumulatedDepreciation(BigDecimal accumulatedDepreciation) {
        this.accumulatedDepreciation = accumulatedDepreciation;
    }

    public BigDecimal getNetBookValue() {
        return netBookValue;
    }

    public void setNetBookValue(BigDecimal netBookValue) {
        this.netBookValue = netBookValue;
    }

    public BigDecimal getImpairmentAmount() {
        return impairmentAmount;
    }

    public void setImpairmentAmount(BigDecimal impairmentAmount) {
        this.impairmentAmount = impairmentAmount;
    }

    public BigDecimal getBookValue() {
        return bookValue;
    }

    public void setBookValue(BigDecimal bookValue) {
        this.bookValue = bookValue;
    }

    public BigDecimal getDisposedValue() {
        return disposedValue;
    }

    public void setDisposedValue(BigDecimal disposedValue) {
        this.disposedValue = disposedValue;
    }

    public String getFinanceStatus() {
        return financeStatus;
    }

    public void setFinanceStatus(String financeStatus) {
        this.financeStatus = financeStatus;
    }

    public String getLastDepreciationPeriod() {
        return lastDepreciationPeriod;
    }

    public void setLastDepreciationPeriod(String lastDepreciationPeriod) {
        this.lastDepreciationPeriod = lastDepreciationPeriod;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("financeId", getFinanceId())
            .append("assetId", getAssetId())
            .append("bookType", getBookType())
            .append("currencyCode", getCurrencyCode())
            .append("originalValue", getOriginalValue())
            .append("salvageRate", getSalvageRate())
            .append("salvageValue", getSalvageValue())
            .append("depreciableValue", getDepreciableValue())
            .append("depreciationMethod", getDepreciationMethod())
            .append("usefulLifeMonth", getUsefulLifeMonth())
            .append("depreciationStartDate", getDepreciationStartDate())
            .append("depreciationEndDate", getDepreciationEndDate())
            .append("monthlyDepreciationAmount", getMonthlyDepreciationAmount())
            .append("accumulatedDepreciation", getAccumulatedDepreciation())
            .append("netBookValue", getNetBookValue())
            .append("impairmentAmount", getImpairmentAmount())
            .append("bookValue", getBookValue())
            .append("disposedValue", getDisposedValue())
            .append("financeStatus", getFinanceStatus())
            .append("lastDepreciationPeriod", getLastDepreciationPeriod())
            .append("versionNo", getVersionNo())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
