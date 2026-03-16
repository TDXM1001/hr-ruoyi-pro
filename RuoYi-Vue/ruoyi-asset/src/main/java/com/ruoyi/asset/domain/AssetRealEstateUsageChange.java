package com.ruoyi.asset.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 不动产用途变更单。
 */
public class AssetRealEstateUsageChange extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 用途变更单号 */
    private String usageChangeNo;

    /** 资产ID */
    private Long assetId;

    /** 资产编号 */
    private String assetNo;

    /** 申请人 */
    private Long applyUserId;

    /** 申请部门 */
    private Long applyDeptId;

    /** 单据状态 */
    private String status;

    /** 流程状态 */
    private String wfStatus;

    /** 原土地用途 */
    private String oldLandUse;

    /** 目标土地用途 */
    private String targetLandUse;

    /** 原房屋用途 */
    private String oldBuildingUse;

    /** 目标房屋用途 */
    private String targetBuildingUse;

    /** 变更原因 */
    private String reason;

    public String getUsageChangeNo() {
        return usageChangeNo;
    }

    public void setUsageChangeNo(String usageChangeNo) {
        this.usageChangeNo = usageChangeNo;
    }

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

    public Long getApplyUserId() {
        return applyUserId;
    }

    public void setApplyUserId(Long applyUserId) {
        this.applyUserId = applyUserId;
    }

    public Long getApplyDeptId() {
        return applyDeptId;
    }

    public void setApplyDeptId(Long applyDeptId) {
        this.applyDeptId = applyDeptId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWfStatus() {
        return wfStatus;
    }

    public void setWfStatus(String wfStatus) {
        this.wfStatus = wfStatus;
    }

    public String getOldLandUse() {
        return oldLandUse;
    }

    public void setOldLandUse(String oldLandUse) {
        this.oldLandUse = oldLandUse;
    }

    public String getTargetLandUse() {
        return targetLandUse;
    }

    public void setTargetLandUse(String targetLandUse) {
        this.targetLandUse = targetLandUse;
    }

    public String getOldBuildingUse() {
        return oldBuildingUse;
    }

    public void setOldBuildingUse(String oldBuildingUse) {
        this.oldBuildingUse = oldBuildingUse;
    }

    public String getTargetBuildingUse() {
        return targetBuildingUse;
    }

    public void setTargetBuildingUse(String targetBuildingUse) {
        this.targetBuildingUse = targetBuildingUse;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
