package com.ruoyi.asset.domain;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.asset.enums.AssetBizType;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.common.utils.DateUtils;

/**
 * 资产变更日志对象。
 * <p>
 * 该对象用于记录台账关键动作的业务轨迹，
 * 当前优先覆盖建账与台账修改两个基础动作，便于后续资产全生命周期追溯。
 * </p>
 *
 * @author Codex
 */
public class AssetChangeLog implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 资产ID */
    private Long assetId;

    /** 业务类型 */
    private String bizType;

    /** 业务单据ID */
    private Long bizId;

    /** 变更前状态 */
    private String beforeStatus;

    /** 变更后状态 */
    private String afterStatus;

    /** 操作人 */
    private String operateBy;

    /** 操作时间 */
    private Date operateTime;

    /** 变更说明 */
    private String changeDesc;

    /** 备注 */
    private String remark;

    public static AssetChangeLog ofCreate(Long assetId, String operator)
    {
        return build(assetId, AssetBizType.LEDGER_CREATE.getCode(), null,
            AssetStatus.IN_LEDGER.getCode(), operator, "新增资产台账");
    }

    public static AssetChangeLog ofUpdate(Long assetId, String beforeStatus, String afterStatus, String operator)
    {
        return build(assetId, AssetBizType.LEDGER_UPDATE.getCode(), beforeStatus,
            afterStatus, operator, "修改资产台账");
    }

    /**
     * 构建交接动作留痕日志。
     *
     * @param assetId 资产ID
     * @param bizType 交接业务类型
     * @param bizId 交接单据ID
     * @param beforeStatus 变更前状态
     * @param afterStatus 变更后状态
     * @param operator 操作人
     * @param changeDesc 变更说明
     * @return 变更日志
     */
    public static AssetChangeLog ofHandover(Long assetId, String bizType, Long bizId, String beforeStatus,
        String afterStatus, String operator, String changeDesc)
    {
        AssetChangeLog changeLog = build(assetId, bizType, beforeStatus, afterStatus, operator, changeDesc);
        changeLog.setBizId(bizId);
        return changeLog;
    }

    private static AssetChangeLog build(Long assetId, String bizType, String beforeStatus,
        String afterStatus, String operator, String changeDesc)
    {
        AssetChangeLog changeLog = new AssetChangeLog();
        changeLog.setAssetId(assetId);
        changeLog.setBizType(bizType);
        changeLog.setBeforeStatus(beforeStatus);
        changeLog.setAfterStatus(afterStatus);
        changeLog.setOperateBy(operator);
        changeLog.setOperateTime(DateUtils.getNowDate());
        changeLog.setChangeDesc(changeDesc);
        return changeLog;
    }

    public Long getLogId()
    {
        return logId;
    }

    public void setLogId(Long logId)
    {
        this.logId = logId;
    }

    public Long getAssetId()
    {
        return assetId;
    }

    public void setAssetId(Long assetId)
    {
        this.assetId = assetId;
    }

    public String getBizType()
    {
        return bizType;
    }

    public void setBizType(String bizType)
    {
        this.bizType = bizType;
    }

    public Long getBizId()
    {
        return bizId;
    }

    public void setBizId(Long bizId)
    {
        this.bizId = bizId;
    }

    public String getBeforeStatus()
    {
        return beforeStatus;
    }

    public void setBeforeStatus(String beforeStatus)
    {
        this.beforeStatus = beforeStatus;
    }

    public String getAfterStatus()
    {
        return afterStatus;
    }

    public void setAfterStatus(String afterStatus)
    {
        this.afterStatus = afterStatus;
    }

    public String getOperateBy()
    {
        return operateBy;
    }

    public void setOperateBy(String operateBy)
    {
        this.operateBy = operateBy;
    }

    public Date getOperateTime()
    {
        return operateTime;
    }

    public void setOperateTime(Date operateTime)
    {
        this.operateTime = operateTime;
    }

    public String getChangeDesc()
    {
        return changeDesc;
    }

    public void setChangeDesc(String changeDesc)
    {
        this.changeDesc = changeDesc;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("assetId", getAssetId())
            .append("bizType", getBizType())
            .append("bizId", getBizId())
            .append("beforeStatus", getBeforeStatus())
            .append("afterStatus", getAfterStatus())
            .append("operateBy", getOperateBy())
            .append("operateTime", getOperateTime())
            .append("changeDesc", getChangeDesc())
            .append("remark", getRemark())
            .toString();
    }
}
