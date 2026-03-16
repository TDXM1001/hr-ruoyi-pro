package com.ruoyi.asset.domain.vo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 资产报表汇总视图对象。
 *
 * 汇总资产类型、状态、部门三个维度的最小管理口径，
 * 供中小企业管理页快速查看台账规模与分布。
 */
public class AssetReportSummaryVo {
    /** 资产总数。 */
    private Long totalAssetCount = 0L;

    /** 资产原值合计。 */
    private BigDecimal totalOriginalValue = BigDecimal.ZERO;

    /** 固定资产数量。 */
    private Long fixedAssetCount = 0L;

    /** 固定资产原值。 */
    private BigDecimal fixedAssetOriginalValue = BigDecimal.ZERO;

    /** 不动产数量。 */
    private Long realEstateCount = 0L;

    /** 不动产原值。 */
    private BigDecimal realEstateOriginalValue = BigDecimal.ZERO;

    /** 资产类型汇总。 */
    private List<AssetTypeSummaryItem> typeSummaries = new ArrayList<>();

    /** 资产状态汇总。 */
    private List<AssetStatusSummaryItem> statusSummaries = new ArrayList<>();

    /** 在用资产部门汇总。 */
    private List<AssetDeptSummaryItem> deptSummaries = new ArrayList<>();

    public Long getTotalAssetCount() {
        return totalAssetCount;
    }

    public void setTotalAssetCount(Long totalAssetCount) {
        this.totalAssetCount = totalAssetCount;
    }

    public BigDecimal getTotalOriginalValue() {
        return totalOriginalValue;
    }

    public void setTotalOriginalValue(BigDecimal totalOriginalValue) {
        this.totalOriginalValue = totalOriginalValue;
    }

    public Long getFixedAssetCount() {
        return fixedAssetCount;
    }

    public void setFixedAssetCount(Long fixedAssetCount) {
        this.fixedAssetCount = fixedAssetCount;
    }

    public BigDecimal getFixedAssetOriginalValue() {
        return fixedAssetOriginalValue;
    }

    public void setFixedAssetOriginalValue(BigDecimal fixedAssetOriginalValue) {
        this.fixedAssetOriginalValue = fixedAssetOriginalValue;
    }

    public Long getRealEstateCount() {
        return realEstateCount;
    }

    public void setRealEstateCount(Long realEstateCount) {
        this.realEstateCount = realEstateCount;
    }

    public BigDecimal getRealEstateOriginalValue() {
        return realEstateOriginalValue;
    }

    public void setRealEstateOriginalValue(BigDecimal realEstateOriginalValue) {
        this.realEstateOriginalValue = realEstateOriginalValue;
    }

    public List<AssetTypeSummaryItem> getTypeSummaries() {
        return typeSummaries;
    }

    public void setTypeSummaries(List<AssetTypeSummaryItem> typeSummaries) {
        this.typeSummaries = typeSummaries == null ? new ArrayList<>() : typeSummaries;
    }

    public List<AssetStatusSummaryItem> getStatusSummaries() {
        return statusSummaries;
    }

    public void setStatusSummaries(List<AssetStatusSummaryItem> statusSummaries) {
        this.statusSummaries = statusSummaries == null ? new ArrayList<>() : statusSummaries;
    }

    public List<AssetDeptSummaryItem> getDeptSummaries() {
        return deptSummaries;
    }

    public void setDeptSummaries(List<AssetDeptSummaryItem> deptSummaries) {
        this.deptSummaries = deptSummaries == null ? new ArrayList<>() : deptSummaries;
    }

    /**
     * 资产类型汇总行。
     */
    public static class AssetTypeSummaryItem {
        private String assetType;
        private Long assetCount;
        private BigDecimal originalValueTotal;

        public String getAssetType() {
            return assetType;
        }

        public void setAssetType(String assetType) {
            this.assetType = assetType;
        }

        public Long getAssetCount() {
            return assetCount;
        }

        public void setAssetCount(Long assetCount) {
            this.assetCount = assetCount;
        }

        public BigDecimal getOriginalValueTotal() {
            return originalValueTotal;
        }

        public void setOriginalValueTotal(BigDecimal originalValueTotal) {
            this.originalValueTotal = originalValueTotal;
        }
    }

    /**
     * 资产状态汇总行。
     */
    public static class AssetStatusSummaryItem {
        private String assetStatus;
        private Long assetCount;
        private BigDecimal originalValueTotal;

        public String getAssetStatus() {
            return assetStatus;
        }

        public void setAssetStatus(String assetStatus) {
            this.assetStatus = assetStatus;
        }

        public Long getAssetCount() {
            return assetCount;
        }

        public void setAssetCount(Long assetCount) {
            this.assetCount = assetCount;
        }

        public BigDecimal getOriginalValueTotal() {
            return originalValueTotal;
        }

        public void setOriginalValueTotal(BigDecimal originalValueTotal) {
            this.originalValueTotal = originalValueTotal;
        }
    }

    /**
     * 部门汇总行。
     */
    public static class AssetDeptSummaryItem {
        private Long deptId;
        private String deptName;
        private Long assetCount;
        private BigDecimal originalValueTotal;

        public Long getDeptId() {
            return deptId;
        }

        public void setDeptId(Long deptId) {
            this.deptId = deptId;
        }

        public String getDeptName() {
            return deptName;
        }

        public void setDeptName(String deptName) {
            this.deptName = deptName;
        }

        public Long getAssetCount() {
            return assetCount;
        }

        public void setAssetCount(Long assetCount) {
            this.assetCount = assetCount;
        }

        public BigDecimal getOriginalValueTotal() {
            return originalValueTotal;
        }

        public void setOriginalValueTotal(BigDecimal originalValueTotal) {
            this.originalValueTotal = originalValueTotal;
        }
    }
}