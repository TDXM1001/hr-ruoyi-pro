package com.ruoyi.asset.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 不动产权属变更单。
 */
public class AssetRealEstateOwnershipChange extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 权属变更单号 */
    private String ownershipChangeNo;

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

    /** 原权属人 */
    private String oldRightsHolder;

    /** 目标权属人 */
    private String targetRightsHolder;

    /** 原产权证号 */
    private String oldPropertyCertNo;

    /** 目标产权证号 */
    private String targetPropertyCertNo;

    /** 原登记日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date oldRegistrationDate;

    /** 目标登记日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date targetRegistrationDate;

    /** 变更原因 */
    private String reason;

    public String getOwnershipChangeNo() {
        return ownershipChangeNo;
    }

    public void setOwnershipChangeNo(String ownershipChangeNo) {
        this.ownershipChangeNo = ownershipChangeNo;
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

    public String getOldRightsHolder() {
        return oldRightsHolder;
    }

    public void setOldRightsHolder(String oldRightsHolder) {
        this.oldRightsHolder = oldRightsHolder;
    }

    public String getTargetRightsHolder() {
        return targetRightsHolder;
    }

    public void setTargetRightsHolder(String targetRightsHolder) {
        this.targetRightsHolder = targetRightsHolder;
    }

    public String getOldPropertyCertNo() {
        return oldPropertyCertNo;
    }

    public void setOldPropertyCertNo(String oldPropertyCertNo) {
        this.oldPropertyCertNo = oldPropertyCertNo;
    }

    public String getTargetPropertyCertNo() {
        return targetPropertyCertNo;
    }

    public void setTargetPropertyCertNo(String targetPropertyCertNo) {
        this.targetPropertyCertNo = targetPropertyCertNo;
    }

    public Date getOldRegistrationDate() {
        return oldRegistrationDate;
    }

    public void setOldRegistrationDate(Date oldRegistrationDate) {
        this.oldRegistrationDate = oldRegistrationDate;
    }

    public Date getTargetRegistrationDate() {
        return targetRegistrationDate;
    }

    public void setTargetRegistrationDate(Date targetRegistrationDate) {
        this.targetRegistrationDate = targetRegistrationDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
