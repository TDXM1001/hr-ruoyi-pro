package com.ruoyi.workflow.service.impl;

import com.ruoyi.workflow.domain.WfApprovalInstance;
import com.ruoyi.workflow.service.IApprovalEngine;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * 基于状态机的轻量级通用审批引擎实现
 */
@Service
public class SimpleApprovalEngineImpl implements IApprovalEngine {
    
    private static final Logger log = LoggerFactory.getLogger(SimpleApprovalEngineImpl.class);

    @Override
    public Long startProcess(String businessId, String businessType) {
        log.info("发起审批流程: businessId={}, businessType={}", businessId, businessType);
        // FIXME: 实际需注入 Mapper 执行插入流程实例记录，初始状态设为 pending
        // 这里返回由于还没有写入数据库，因此以固定数字做个桩
        return System.currentTimeMillis(); 
    }

    @Override
    public void approve(Long instanceId, Long approverId, String comment) {
        log.info("审批通过: instanceId={}, approverId={}, comment={}", instanceId, approverId, comment);
        // FIXME: 实际需执行节点操作（记录日志）并且判定是否为最终节点，若为最终则修改状态为 approved
    }

    @Override
    public void reject(Long instanceId, Long approverId, String comment) {
        log.info("审批驳回: instanceId={}, approverId={}, comment={}", instanceId, approverId, comment);
        // FIXME: 实际需执行拒绝节点操作，修改状态为 rejected
    }

    @Override
    public List<WfApprovalInstance> getTasks(Long approverId) {
        log.info("获取待办列表: approverId={}", approverId);
        // FIXME: 实际应查询当前用户对应的 pending 状态流转节点
        return Collections.emptyList();
    }
}
