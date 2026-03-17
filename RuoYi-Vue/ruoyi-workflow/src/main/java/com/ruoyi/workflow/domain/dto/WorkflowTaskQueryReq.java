package com.ruoyi.workflow.domain.dto;

/**
 * 流程任务查询请求。
 *
 * 同时兼容统一别名字段和历史字段，便于流程中心逐步切换到新契约。
 */
public class WorkflowTaskQueryReq {
    /** 页码。 */
    private Integer pageNum;

    /** 每页条数。 */
    private Integer pageSize;

    /** 统一业务单号。 */
    private String bizNo;

    /** 统一业务类型。 */
    private String bizType;

    /** 统一流程状态。 */
    private String wfStatus;

    /** 历史业务单号。 */
    private String businessId;

    /** 历史业务类型。 */
    private String businessType;

    /** 历史流程状态。 */
    private String status;

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getWfStatus() {
        return wfStatus;
    }

    public void setWfStatus(String wfStatus) {
        this.wfStatus = wfStatus;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
