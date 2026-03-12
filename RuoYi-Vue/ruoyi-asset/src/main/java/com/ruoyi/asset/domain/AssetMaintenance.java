package com.ruoyi.asset.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 资产维保实体
 */
public class AssetMaintenance extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 维修单号 */
    private String maintenanceNo;

    /** 资产编号 */
    @Excel(name = "资产编号")
    private String assetNo;

    /** 申请人 */
    @Excel(name = "申请人")
    private Long applyUserId;

    /** 申请部门 */
    @Excel(name = "申请部门")
    private Long applyDeptId;

    /** 维修原因 */
    @Excel(name = "维修原因")
    private String reason;

    /** 状态：0=审批中 1=已通过(维修中) 2=已驳回 3=维修完成 */
    @Excel(name = "状态")
    private Integer status;

    public void setMaintenanceNo(String maintenanceNo) {
        this.maintenanceNo = maintenanceNo;
    }

    public String getMaintenanceNo() {
        return maintenanceNo;
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
