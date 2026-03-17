package com.ruoyi.workflow.service.impl;

import java.util.Collections;
import java.util.Date;
import com.ruoyi.workflow.domain.vo.WorkflowTaskVo;
import com.ruoyi.workflow.domain.WfApprovalInstance;
import com.ruoyi.workflow.domain.WfApprovalNode;
import com.ruoyi.workflow.domain.WfApprovalTemplate;
import com.ruoyi.workflow.mapper.WfApprovalInstanceMapper;
import com.ruoyi.workflow.mapper.WfApprovalNodeMapper;
import com.ruoyi.workflow.mapper.WfApprovalTemplateMapper;
import com.ruoyi.workflow.service.IApprovalEngine;
import com.ruoyi.workflow.service.WorkflowBusinessHandlerRegistry;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import org.slf4j.LoggerFactory;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 基于状态机的轻量级通用审批引擎实现。
 *
 * 当前实现有两个明确边界：
 * 1. 实例状态使用最小值域：pending / approved / rejected。
 * 2. 审批模板当前只支持“业务类型 -> 单一审批人”的单节点分派基线，
 *    先把待办收敛到按人可见，再为后续多级审批留出扩展位。
 */
@Service
public class SimpleApprovalEngineImpl implements IApprovalEngine {
    /** 实例初始状态。 */
    private static final String INSTANCE_STATUS_PENDING = "pending";

    /** 实例审批通过状态。 */
    private static final String INSTANCE_STATUS_APPROVED = "approved";

    /** 实例审批驳回状态。 */
    private static final String INSTANCE_STATUS_REJECTED = "rejected";

    /** 前端工作流待办字典值。 */
    private static final String VIEW_STATUS_IN_PROGRESS = "IN_PROGRESS";

    /** 前端工作流已完成字典值。 */
    private static final String VIEW_STATUS_COMPLETED = "COMPLETED";

    /** 前端工作流已驳回字典值。 */
    private static final String VIEW_STATUS_REJECTED = "REJECTED";

    /** 首次发起流程时给前端展示的节点名称。 */
    private static final String NODE_NAME_PENDING = "待审批";

    /** 审批通过后对外展示的最终节点名称。 */
    private static final String NODE_NAME_APPROVED = "审批完成";

    /** 审批驳回后对外展示的最终节点名称。 */
    private static final String NODE_NAME_REJECTED = "审批驳回";

    private static final Logger log = LoggerFactory.getLogger(SimpleApprovalEngineImpl.class);

    @Autowired
    private WfApprovalInstanceMapper approvalInstanceMapper;

    @Autowired
    private WfApprovalNodeMapper approvalNodeMapper;

    @Autowired
    private WfApprovalTemplateMapper approvalTemplateMapper;

    @Autowired(required = false)
    private WorkflowBusinessHandlerRegistry businessHandlerRegistry;

    @Override
    public Long startProcess(String businessId, String businessType) {
        log.info("发起审批流程: businessId={}, businessType={}", businessId, businessType);
        WfApprovalTemplate approvalTemplate = loadEnabledTemplate(businessType);

        WfApprovalInstance approvalInstance = new WfApprovalInstance();
        approvalInstance.setBusinessId(businessId);
        approvalInstance.setBusinessType(businessType);
        approvalInstance.setApproverId(approvalTemplate.getApproverId());
        approvalInstance.setCurrentNode(NODE_NAME_PENDING);
        approvalInstance.setStatus(INSTANCE_STATUS_PENDING);
        approvalInstance.setCreateTime(DateUtils.getNowDate());

        approvalInstanceMapper.insertWfApprovalInstance(approvalInstance);
        return approvalInstance.getInstanceId();
    }

    @Override
    public void approve(Long instanceId, Long approverId, String comment) {
        log.info("审批通过: instanceId={}, approverId={}, comment={}", instanceId, approverId, comment);
        updateInstanceAfterAction(instanceId, approverId, "approve", comment, INSTANCE_STATUS_APPROVED, NODE_NAME_APPROVED);
    }

    @Override
    public void reject(Long instanceId, Long approverId, String comment) {
        log.info("审批驳回: instanceId={}, approverId={}, comment={}", instanceId, approverId, comment);
        updateInstanceAfterAction(instanceId, approverId, "reject", comment, INSTANCE_STATUS_REJECTED, NODE_NAME_REJECTED);
    }

    @Override
    public List<WorkflowTaskVo> getTasks(Long approverId) {
        log.info("获取待办列表: approverId={}", approverId);
        return normalizeTasks(approvalInstanceMapper.selectTodoTaskList(approverId));
    }

    @Override
    public List<WorkflowTaskVo> getDoneTasks(Long approverId) {
        log.info("获取已办列表: approverId={}", approverId);
        return normalizeTasks(approvalInstanceMapper.selectDoneTaskList(approverId));
    }

    /**
     * 统一处理审批动作。
     *
     * 这样做可以避免 approve / reject 两套逻辑各自维护节点流水和实例状态，
     * 后续如果扩展转办、撤回，只需要复用同一个骨架。
     */
    private void updateInstanceAfterAction(
        Long instanceId,
        Long approverId,
        String action,
        String comment,
        String targetStatus,
        String targetNodeName
    ) {
        WfApprovalInstance approvalInstance = getPendingInstance(instanceId);
        validateAssignedApprover(approvalInstance, approverId);

        WfApprovalNode approvalNode = new WfApprovalNode();
        approvalNode.setInstanceId(instanceId);
        approvalNode.setApproverId(approverId);
        approvalNode.setAction(action);
        approvalNode.setComment(comment);
        approvalNode.setProcessTime(DateUtils.getNowDate());
        approvalNodeMapper.insertWfApprovalNode(approvalNode);

        WfApprovalInstance updatePayload = new WfApprovalInstance();
        updatePayload.setInstanceId(approvalInstance.getInstanceId());
        updatePayload.setStatus(targetStatus);
        updatePayload.setCurrentNode(targetNodeName);
        approvalInstanceMapper.updateWfApprovalInstance(updatePayload);

        // 审批实例状态落库后再触发业务回调，保证业务侧读取到的是最终状态而不是中间态。
        if (businessHandlerRegistry != null) {
            if ("approve".equals(action)) {
                businessHandlerRegistry.handleApproved(approvalInstance.getBusinessType(), approvalInstance.getBusinessId());
            } else if ("reject".equals(action)) {
                businessHandlerRegistry.handleRejected(approvalInstance.getBusinessType(), approvalInstance.getBusinessId());
            }
        }
    }

    /**
     * 校验审批实例必须存在且仍处于待处理状态。
     *
     * 当前阶段的最小审批闭环不允许重复审批同一实例，
     * 否则前后端都会出现“已处理单据再次提交”的脏状态。
     */
    private WfApprovalInstance getPendingInstance(Long instanceId) {
        WfApprovalInstance approvalInstance = approvalInstanceMapper.selectWfApprovalInstanceByInstanceId(instanceId);
        if (approvalInstance == null) {
            throw new ServiceException("审批实例不存在");
        }
        if (!INSTANCE_STATUS_PENDING.equals(approvalInstance.getStatus())) {
            throw new ServiceException("当前审批实例已处理，不能重复审批");
        }
        return approvalInstance;
    }

    /**
     * 发起审批前必须先命中启用中的模板，避免再次回退到“所有人都可审批”的旧行为。
     */
    private WfApprovalTemplate loadEnabledTemplate(String businessType) {
        WfApprovalTemplate approvalTemplate = approvalTemplateMapper.selectEnabledTemplateByBusinessType(businessType);
        if (approvalTemplate == null) {
            throw new ServiceException("业务类型未配置启用中的审批模板: " + businessType);
        }
        if (approvalTemplate.getApproverId() == null) {
            throw new ServiceException("审批模板未配置审批人: " + businessType);
        }
        return approvalTemplate;
    }

    /**
     * 审批动作必须由实例当前分派的审批人执行，避免手工构造请求越权处理他人待办。
     */
    private void validateAssignedApprover(WfApprovalInstance approvalInstance, Long approverId) {
        if (approvalInstance.getApproverId() == null) {
            throw new ServiceException("审批实例未分配审批人，请先补齐审批模板");
        }
        if (!approvalInstance.getApproverId().equals(approverId)) {
            throw new ServiceException("当前审批人无权处理该待办");
        }
    }

    /**
     * 把数据库里的最小状态值域映射成前端字典已经能识别的值，
     * 这样批次一就能先把真实接口接通，不必等到菜单和字典脚本批次再改页面。
     */
    private List<WorkflowTaskVo> normalizeTasks(List<WorkflowTaskVo> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }
        return tasks.stream().map(this::normalizeTask).toList();
    }

    /**
     * 单条任务归一化。
     *
     * 这里直接修改查询结果对象是有意为之：
     * mapper 查询结果仅作为展示层载体，不会再回写数据库，避免额外复制对象。
     */
    private WorkflowTaskVo normalizeTask(WorkflowTaskVo task) {
        task.setBusinessType(normalizeBusinessType(task.getBusinessType()));
        task.setStatus(normalizeStatus(task.getStatus()));
        // 同步补齐统一别名字段，方便前端和控制器按新口径读取。
        task.setBizNo(task.getBusinessId());
        task.setBizType(task.getBusinessType());
        task.setWfStatus(task.getStatus());
        return task;
    }

    /**
     * 把后端资产业务类型映射成前端工作流字典值。
     */
    private String normalizeBusinessType(String businessType) {
        if (businessType == null) {
            return null;
        }
        return switch (businessType) {
            case "asset_requisition" -> "REQUISITION";
            case "asset_return" -> "RETURN";
            case "asset_maintenance" -> "REPAIR";
            case "asset_disposal" -> "SCRAP";
            case "asset_real_estate_ownership_change" -> "REAL_ESTATE_OWNERSHIP_CHANGE";
            case "asset_real_estate_usage_change" -> "REAL_ESTATE_USAGE_CHANGE";
            case "asset_real_estate_status_change" -> "REAL_ESTATE_STATUS_CHANGE";
            case "asset_real_estate_disposal" -> "REAL_ESTATE_DISPOSAL";
            default -> businessType;
        };
    }

    /**
     * 把实例状态映射成前端 `wf_status` 字典值。
     */
    private String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case INSTANCE_STATUS_PENDING -> VIEW_STATUS_IN_PROGRESS;
            case INSTANCE_STATUS_APPROVED -> VIEW_STATUS_COMPLETED;
            case INSTANCE_STATUS_REJECTED -> VIEW_STATUS_REJECTED;
            default -> status;
        };
    }
}
