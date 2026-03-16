package com.ruoyi.workflow.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 审批模板对象 wf_approval_template。
 */
public class WfApprovalTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 模板ID。 */
    private Long templateId;

    /** 业务类型。 */
    private String businessType;

    /** 模板名称。 */
    private String templateName;

    /** 默认审批人ID。 */
    private Long approverId;

    /** 模板状态：0=启用 1=停用。 */
    private String status;

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
