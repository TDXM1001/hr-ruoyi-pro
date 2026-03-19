package com.ruoyi.asset.domain.bo;

import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 盘点任务资产查询入参。
 *
 * @author Codex
 */
public class AssetInventoryTaskAssetBo extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 资产编码 */
    private String assetCode;

    /** 资产名称 */
    private String assetName;

    /** 资产状态 */
    private String assetStatus;

    /**
     * 结果筛选类型：
     * ALL-全部，PENDING-未登记，REGISTERED-已登记，ABNORMAL-异常结果。
     */
    private String resultType;

    public Long getTaskId()
    {
        return taskId;
    }

    public void setTaskId(Long taskId)
    {
        this.taskId = taskId;
    }

    public String getAssetCode()
    {
        return assetCode;
    }

    public void setAssetCode(String assetCode)
    {
        this.assetCode = assetCode;
    }

    public String getAssetName()
    {
        return assetName;
    }

    public void setAssetName(String assetName)
    {
        this.assetName = assetName;
    }

    public String getAssetStatus()
    {
        return assetStatus;
    }

    public void setAssetStatus(String assetStatus)
    {
        this.assetStatus = assetStatus;
    }

    public String getResultType()
    {
        return resultType;
    }

    public void setResultType(String resultType)
    {
        this.resultType = resultType;
    }
}

