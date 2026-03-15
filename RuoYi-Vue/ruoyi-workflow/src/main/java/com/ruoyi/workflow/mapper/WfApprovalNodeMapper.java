package com.ruoyi.workflow.mapper;

import com.ruoyi.workflow.domain.WfApprovalNode;

/**
 * 审批节点流水 Mapper。
 *
 * 它的职责很单一：把每次审批动作落成不可逆的流水，
 * 后续如果要扩展转办、加签、撤回，也都沿着这张表继续演进。
 */
public interface WfApprovalNodeMapper {

    /**
     * 新增审批节点流水。
     *
     * @param approvalNode 审批节点
     * @return 影响行数
     */
    int insertWfApprovalNode(WfApprovalNode approvalNode);
}
