package com.ruoyi.asset.service;

import org.springframework.stereotype.Component;
import com.ruoyi.asset.enums.AssetApprovalStatus;
import com.ruoyi.asset.enums.AssetApprovalType;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.common.exception.ServiceException;

/**
 * 审批结果到资产状态的回写矩阵。
 *
 * <p>该矩阵统一控制审批结果如何回写资产状态，
 * 防止各业务服务各自定义导致口径漂移。</p>
 *
 * @author Codex
 */
@Component
public class AssetApprovalStatusMatrix
{
    /**
     * 将审批结果映射为资产状态。
     *
     * @param approvalType 审批类型
     * @param approvalStatus 审批状态
     * @return 资产状态
     */
    public AssetStatus mapToAssetStatus(AssetApprovalType approvalType, AssetApprovalStatus approvalStatus)
    {
        if (approvalType == null || approvalStatus == null)
        {
            throw new ServiceException("审批类型和审批状态不能为空");
        }

        if (approvalStatus != AssetApprovalStatus.APPROVED)
        {
            throw new ServiceException("审批结果不触发资产状态回写");
        }

        switch (approvalType)
        {
            case DISPOSAL:
                return AssetStatus.DISPOSED;
            case RECTIFICATION:
                return AssetStatus.IN_USE;
            case HIGH_VALUE:
                return AssetStatus.IN_LEDGER;
            default:
                throw new ServiceException("暂不支持的审批类型");
        }
    }
}

