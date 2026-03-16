package com.ruoyi.asset.domain.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 资产预警视图对象。
 *
 * 按闲置、维修中、待审批、土地到期四类口径聚合，
 * 便于资产管理员在一个页面里做日常排查。
 */
public class AssetWarningVo {
    /** 闲置资产预警。 */
    private List<AssetWarningItem> idleAssets = new ArrayList<>();

    /** 维修中未关闭资产预警。 */
    private List<AssetWarningItem> maintenanceAssets = new ArrayList<>();

    /** 待审批关键动作预警。 */
    private List<AssetWarningItem> pendingApprovalItems = new ArrayList<>();

    /** 土地期限临近到期预警。 */
    private List<AssetWarningItem> landTermExpiringAssets = new ArrayList<>();

    public List<AssetWarningItem> getIdleAssets() {
        return idleAssets;
    }

    public void setIdleAssets(List<AssetWarningItem> idleAssets) {
        this.idleAssets = idleAssets == null ? new ArrayList<>() : idleAssets;
    }

    public List<AssetWarningItem> getMaintenanceAssets() {
        return maintenanceAssets;
    }

    public void setMaintenanceAssets(List<AssetWarningItem> maintenanceAssets) {
        this.maintenanceAssets = maintenanceAssets == null ? new ArrayList<>() : maintenanceAssets;
    }

    public List<AssetWarningItem> getPendingApprovalItems() {
        return pendingApprovalItems;
    }

    public void setPendingApprovalItems(List<AssetWarningItem> pendingApprovalItems) {
        this.pendingApprovalItems = pendingApprovalItems == null ? new ArrayList<>() : pendingApprovalItems;
    }

    public List<AssetWarningItem> getLandTermExpiringAssets() {
        return landTermExpiringAssets;
    }

    public void setLandTermExpiringAssets(List<AssetWarningItem> landTermExpiringAssets) {
        this.landTermExpiringAssets = landTermExpiringAssets == null ? new ArrayList<>() : landTermExpiringAssets;
    }

    /**
     * 单条预警记录。
     */
    public static class AssetWarningItem {
        private Long assetId;
        private String assetNo;
        private String assetName;
        private String assetType;
        private String assetStatus;
        private Long deptId;
        private String deptName;
        private String businessNo;
        private String businessType;
        private String handlerName;
        private String warningCode;
        private String warningLabel;
        private String detail;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date eventTime;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private Date dueDate;

        public Long getAssetId() {
            return assetId;
        }

        public void setAssetId(Long assetId) {
            this.assetId = assetId;
        }

        public String getAssetNo() {
            return assetNo;
        }

        public void setAssetNo(String assetNo) {
            this.assetNo = assetNo;
        }

        public String getAssetName() {
            return assetName;
        }

        public void setAssetName(String assetName) {
            this.assetName = assetName;
        }

        public String getAssetType() {
            return assetType;
        }

        public void setAssetType(String assetType) {
            this.assetType = assetType;
        }

        public String getAssetStatus() {
            return assetStatus;
        }

        public void setAssetStatus(String assetStatus) {
            this.assetStatus = assetStatus;
        }

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

        public String getBusinessNo() {
            return businessNo;
        }

        public void setBusinessNo(String businessNo) {
            this.businessNo = businessNo;
        }

        public String getBusinessType() {
            return businessType;
        }

        public void setBusinessType(String businessType) {
            this.businessType = businessType;
        }

        public String getHandlerName() {
            return handlerName;
        }

        public void setHandlerName(String handlerName) {
            this.handlerName = handlerName;
        }

        public String getWarningCode() {
            return warningCode;
        }

        public void setWarningCode(String warningCode) {
            this.warningCode = warningCode;
        }

        public String getWarningLabel() {
            return warningLabel;
        }

        public void setWarningLabel(String warningLabel) {
            this.warningLabel = warningLabel;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public Date getEventTime() {
            return eventTime;
        }

        public void setEventTime(Date eventTime) {
            this.eventTime = eventTime;
        }

        public Date getDueDate() {
            return dueDate;
        }

        public void setDueDate(Date dueDate) {
            this.dueDate = dueDate;
        }
    }
}