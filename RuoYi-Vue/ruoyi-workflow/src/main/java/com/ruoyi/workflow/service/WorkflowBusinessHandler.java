package com.ruoyi.workflow.service;

import java.util.Collection;

/**
 * 工作流业务回调处理器。
 *
 * 它只定义“审批结果如何回写到具体业务”的抽象，
 * 不关心具体业务来自资产、采购还是后续别的模块。
 */
public interface WorkflowBusinessHandler {

    /**
     * 当前处理器支持的业务类型集合。
     *
     * 之所以返回集合而不是单值，是因为资产模块的一个处理器会同时覆盖
     * 领用、维修、处置三种业务单据，避免拆成三个高度重复的 Bean。
     *
     * @return 支持的业务类型
     */
    Collection<String> getSupportedBusinessTypes();

    /**
     * 处理审批通过回调。
     *
     * @param businessType 业务类型
     * @param businessId   业务单据 ID
     */
    void onApprove(String businessType, String businessId);

    /**
     * 处理审批驳回回调。
     *
     * @param businessType 业务类型
     * @param businessId   业务单据 ID
     */
    void onReject(String businessType, String businessId);
}
