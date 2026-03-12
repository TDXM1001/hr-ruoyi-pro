package com.ruoyi.workflow.service;

import com.ruoyi.workflow.domain.WfApprovalInstance;

import java.util.List;

/**
 * 通用审批引擎服务接口
 * 
 * 用于业务侧对接到审批流程，预留抽象以方便未来切换到 Flowable 等重量级工作流引擎。
 */
public interface IApprovalEngine {

    /**
     * 发起流程审批
     *
     * @param businessId   业务单据ID
     * @param businessType 业务类型
     * @return 审批实例ID
     */
    Long startProcess(String businessId, String businessType);

    /**
     * 同意审批
     *
     * @param instanceId 审批实例ID
     * @param approverId 审批人ID
     * @param comment    审批意见
     */
    void approve(Long instanceId, Long approverId, String comment);

    /**
     * 驳回审批
     *
     * @param instanceId 审批实例ID
     * @param approverId 审批人ID
     * @param comment    审批意见
     */
    void reject(Long instanceId, Long approverId, String comment);

    /**
     * 获取指定用户的待办任务列表
     *
     * @param approverId 审批人ID
     * @return 待办实例列表
     */
    List<WfApprovalInstance> getTasks(Long approverId);
}
