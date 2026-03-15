package com.ruoyi.workflow.domain.vo;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 工作流任务视图对象。
 *
 * 这个 VO 面向前端待办/已办页面，字段刻意保持扁平，
 * 这样前端可以直接拿来渲染表格，不必再额外拼装实例与节点两套数据。
 */
public class WorkflowTaskVo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 审批实例 ID。 */
    private Long instanceId;

    /** 业务单据编号。 */
    private String businessId;

    /** 业务类型，返回前端时会统一转成字典可识别的值。 */
    private String businessType;

    /** 当前审批节点文案。 */
    private String currentNode;

    /** 工作流状态，返回前端时会统一转成字典可识别的值。 */
    private String status;

    /** 最近一次处理动作，便于后续已办页扩展展示。 */
    private String action;

    /** 最近一次审批意见。 */
    private String comment;

    /** 最近一次处理人 ID。 */
    private Long approverId;

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

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }
}
