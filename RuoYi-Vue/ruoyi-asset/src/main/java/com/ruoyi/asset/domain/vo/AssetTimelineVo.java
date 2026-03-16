package com.ruoyi.asset.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 资产动作时间线视图对象。
 *
 * 用于把固定资产与不动产动作统一收口到详情页时间线展示结构，
 * 让前端不必理解每张业务单据表的差异字段。
 */
public class AssetTimelineVo {
    /** 动作类型编码。 */
    private String actionType;

    /** 动作类型中文名。 */
    private String actionLabel;

    /** 业务单号。 */
    private String businessNo;

    /** 单据状态：pending/approved/rejected/completed。 */
    private String docStatus;

    /** 动作时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date actionTime;

    /** 业务原因。 */
    private String reason;

    /** 动作前资产状态。 */
    private String assetStatusBefore;

    /** 动作后资产状态。 */
    private String assetStatusAfter;

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public void setActionLabel(String actionLabel) {
        this.actionLabel = actionLabel;
    }

    public String getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(String businessNo) {
        this.businessNo = businessNo;
    }

    public String getDocStatus() {
        return docStatus;
    }

    public void setDocStatus(String docStatus) {
        this.docStatus = docStatus;
    }

    public Date getActionTime() {
        return actionTime;
    }

    public void setActionTime(Date actionTime) {
        this.actionTime = actionTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAssetStatusBefore() {
        return assetStatusBefore;
    }

    public void setAssetStatusBefore(String assetStatusBefore) {
        this.assetStatusBefore = assetStatusBefore;
    }

    public String getAssetStatusAfter() {
        return assetStatusAfter;
    }

    public void setAssetStatusAfter(String assetStatusAfter) {
        this.assetStatusAfter = assetStatusAfter;
    }
}
