package com.ruoyi.asset.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 资产折旧日志对象 asset_depreciation_log
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public class AssetDepreciationLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long assetId;
    private String period;
    private BigDecimal depreciationAmount;
    private BigDecimal accumulatedDepreciation;
    private BigDecimal netBookValue;
    private BigDecimal bookValue;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date calcTime;
    private String calcType;

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public BigDecimal getDepreciationAmount() {
        return depreciationAmount;
    }

    public void setDepreciationAmount(BigDecimal depreciationAmount) {
        this.depreciationAmount = depreciationAmount;
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

    public BigDecimal getBookValue() {
        return bookValue;
    }

    public void setBookValue(BigDecimal bookValue) {
        this.bookValue = bookValue;
    }

    public Date getCalcTime() {
        return calcTime;
    }

    public void setCalcTime(Date calcTime) {
        this.calcTime = calcTime;
    }

    public String getCalcType() {
        return calcType;
    }

    public void setCalcType(String calcType) {
        this.calcType = calcType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("assetId", getAssetId())
            .append("period", getPeriod())
            .append("depreciationAmount", getDepreciationAmount())
            .append("accumulatedDepreciation", getAccumulatedDepreciation())
            .append("netBookValue", getNetBookValue())
            .append("bookValue", getBookValue())
            .append("calcTime", getCalcTime())
            .append("calcType", getCalcType())
            .append("remark", getRemark())
            .append("createTime", getCreateTime())
            .toString();
    }
}
