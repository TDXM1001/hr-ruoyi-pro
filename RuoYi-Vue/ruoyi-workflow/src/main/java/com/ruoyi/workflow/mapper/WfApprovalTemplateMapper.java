package com.ruoyi.workflow.mapper;

import com.ruoyi.workflow.domain.WfApprovalTemplate;

/**
 * 审批模板 Mapper。
 */
public interface WfApprovalTemplateMapper {

    /**
     * 按业务类型查询启用中的审批模板。
     *
     * @param businessType 业务类型
     * @return 审批模板
     */
    WfApprovalTemplate selectEnabledTemplateByBusinessType(String businessType);
}
