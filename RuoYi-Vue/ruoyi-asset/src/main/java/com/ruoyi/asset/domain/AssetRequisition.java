package com.ruoyi.asset.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产领用实体
 */
public class AssetRequisition extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 领用单号 */
    private String requisitionNo;

    /** 资产ID */
    private Long assetId;

    /** 资产编号 */
    @Excel(name = "资产编号")
    private String assetNo;

    /** 申请人 */
    @Excel(name = "申请人")
    private Long applyUserId;

    /** 申请部门 */
    @Excel(name = "申请部门")
    private Long applyDeptId;

    /** 领用原因 */
    @Excel(name = "领用原因")
    private String reason;

    /** 状态：0=审批中 1=已通过 2=已驳回 3=已归还 */
    @Excel(name = "状态", readConverterExp = "0=审批中,1=已通过,2=已驳回,3=已归还")
    private Integer status;

    public void setRequisitionNo(String requisitionNo) {
        this.requisitionNo = requisitionNo;
    }

    public String getRequisitionNo() {
        return requisitionNo;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetNo(String assetNo) {
        this.assetNo = assetNo;
    }

    public String getAssetNo() {
        return assetNo;
    }

    public void setApplyUserId(Long applyUserId) {
        this.applyUserId = applyUserId;
    }

    public Long getApplyUserId() {
        return applyUserId;
    }

    public void setApplyDeptId(Long applyDeptId) {
        this.applyDeptId = applyDeptId;
    }

    public Long getApplyDeptId() {
        return applyDeptId;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }
}
