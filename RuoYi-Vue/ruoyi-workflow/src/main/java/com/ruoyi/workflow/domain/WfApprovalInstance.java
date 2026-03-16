package com.ruoyi.workflow.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

/**
 * 审批实例对象 wf_approval_instance
 */
public class WfApprovalInstance extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 实例ID */
    private Long instanceId;

    /** 业务单据ID */
    private String businessId;

    /** 业务类型 */
    private String businessType;

    /** 当前审批人ID */
    private Long approverId;

    /** 当前审批节点 */
    private String currentNode;

    /** 状态: pending/approved/rejected */
    private String status;

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public String getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(String currentNode) {
        this.currentNode = currentNode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
