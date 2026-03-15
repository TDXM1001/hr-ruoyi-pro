package com.ruoyi.workflow.domain.dto;

/**
 * 工作流审批请求对象。
 *
 * 前端目前把同意、驳回都统一提交到一个接口，
 * 因此这里保留 action 字段来描述动作，控制器再做分发。
 */
public class WorkflowApproveReq {
    /** 审批实例 ID。 */
    private Long instanceId;

    /** 审批动作，如 approve / reject。 */
    private String action;

    /** 审批意见。 */
    private String comment;

    /** 预留给后续转办能力的处理人 ID。 */
    private Long assigneeId;

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
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

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
}
