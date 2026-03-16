package com.ruoyi.workflow.mapper;

import java.util.List;
import com.ruoyi.workflow.domain.WfApprovalInstance;
import com.ruoyi.workflow.domain.vo.WorkflowTaskVo;

/**
 * 审批实例 Mapper。
 *
 * 这里故意把“实例读写”和“任务视图查询”放在同一个 Mapper 里，
 * 是为了先满足本阶段最小审批闭环，不提前引入额外的 Repository 分层。
 */
public interface WfApprovalInstanceMapper {

    /**
     * 新增审批实例。
     *
     * @param approvalInstance 审批实例
     * @return 影响行数
     */
    int insertWfApprovalInstance(WfApprovalInstance approvalInstance);

    /**
     * 按实例 ID 查询审批实例。
     *
     * @param instanceId 实例 ID
     * @return 审批实例
     */
    WfApprovalInstance selectWfApprovalInstanceByInstanceId(Long instanceId);

    /**
     * 更新审批实例状态与当前节点。
     *
     * @param approvalInstance 审批实例
     * @return 影响行数
     */
    int updateWfApprovalInstance(WfApprovalInstance approvalInstance);

    /**
     * 查询待办任务列表。
     *
     * @param approverId 审批人 ID
     * @return 待办任务
     */
    List<WorkflowTaskVo> selectTodoTaskList(Long approverId);

    /**
     * 查询已办任务列表。
     *
     * @param approverId 审批人 ID
     * @return 已办任务
     */
    List<WorkflowTaskVo> selectDoneTaskList(Long approverId);
}
