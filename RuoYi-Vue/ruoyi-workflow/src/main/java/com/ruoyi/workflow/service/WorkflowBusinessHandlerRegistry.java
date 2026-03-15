package com.ruoyi.workflow.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 工作流业务处理器注册中心。
 *
 * 工作流模块只依赖接口和注册器，真正的业务回写逻辑由各业务模块自行提供，
 * 这样可以保证 `ruoyi-workflow` 不反向依赖 `ruoyi-asset`。
 */
@Component
public class WorkflowBusinessHandlerRegistry {
    private static final Logger log = LoggerFactory.getLogger(WorkflowBusinessHandlerRegistry.class);

    private final Map<String, WorkflowBusinessHandler> handlerMap;

    public WorkflowBusinessHandlerRegistry(List<WorkflowBusinessHandler> handlers) {
        Map<String, WorkflowBusinessHandler> mappings = new HashMap<>();
        for (WorkflowBusinessHandler handler : handlers) {
            for (String businessType : handler.getSupportedBusinessTypes()) {
                mappings.put(businessType, handler);
            }
        }
        this.handlerMap = Collections.unmodifiableMap(mappings);
    }

    /**
     * 分发审批通过事件。
     *
     * 当前注册中心对“未注册业务类型”采取记录日志而不是抛错的策略，
     * 这样不会阻断工作流模块本身的最小闭环，也给后续增量接入留出空间。
     */
    public void handleApproved(String businessType, String businessId) {
        WorkflowBusinessHandler handler = handlerMap.get(businessType);
        if (handler == null) {
            log.info("未找到审批通过回调处理器: businessType={}, businessId={}", businessType, businessId);
            return;
        }
        handler.onApprove(businessType, businessId);
    }

    /**
     * 分发审批驳回事件。
     */
    public void handleRejected(String businessType, String businessId) {
        WorkflowBusinessHandler handler = handlerMap.get(businessType);
        if (handler == null) {
            log.info("未找到审批驳回回调处理器: businessType={}, businessId={}", businessType, businessId);
            return;
        }
        handler.onReject(businessType, businessId);
    }
}
