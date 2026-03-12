package com.ruoyi.workflow.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.util.Date;

/**
 * 审批流转记录对象 wf_approval_node
 */
public class WfApprovalNode extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 节点ID */
    private Long nodeId;

    /** 实例ID */
    private Long instanceId;

    /** 审批人ID */
    private Long approverId;

    /** 操作: approve/reject/transfer */
    private String action;

    /** 审批意见 */
    private String comment;

    /** 处理时间 */
    private Date processTime;

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getProcessTime() {
        return processTime;
    }

    public void setProcessTime(Date processTime) {
        this.processTime = processTime;
    }
}
