package com.ruoyi.asset.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 不动产注销/处置单。
 */
public class AssetRealEstateDisposal extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 注销/处置单号 */
    private String disposalNo;

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

    /** 原资产状态 */
    private String oldAssetStatus;

    /** 目标资产状态 */
    private String targetAssetStatus;

    /** 处置类型 */
    private String disposalType;

    /** 处置原因 */
    private String reason;

    public String getDisposalNo() {
        return disposalNo;
    }

    public void setDisposalNo(String disposalNo) {
        this.disposalNo = disposalNo;
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

    public String getOldAssetStatus() {
        return oldAssetStatus;
    }

    public void setOldAssetStatus(String oldAssetStatus) {
        this.oldAssetStatus = oldAssetStatus;
    }

    public String getTargetAssetStatus() {
        return targetAssetStatus;
    }

    public void setTargetAssetStatus(String targetAssetStatus) {
        this.targetAssetStatus = targetAssetStatus;
    }

    public String getDisposalType() {
        return disposalType;
    }

    public void setDisposalType(String disposalType) {
        this.disposalType = disposalType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
