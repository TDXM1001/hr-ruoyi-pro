package com.ruoyi.asset.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.ruoyi.asset.enums.AssetApprovalStatus;
import com.ruoyi.asset.enums.AssetApprovalType;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.common.exception.ServiceException;

/**
 * 审批回写矩阵测试。
 *
 * <p>以资产管理员视角固定审批结果回写规则，
 * 避免审批流与资产状态机口径不一致。</p>
 *
 * @author Codex
 */
class AssetApprovalStatusMatrixTest
{
    private final AssetApprovalStatusMatrix matrix = new AssetApprovalStatusMatrix();

    @Test
    @DisplayName("处置审批通过后应回写为已处置")
    void shouldMapDisposalApprovedToDisposed()
    {
        AssetStatus result = matrix.mapToAssetStatus(AssetApprovalType.DISPOSAL, AssetApprovalStatus.APPROVED);
        assertEquals(AssetStatus.DISPOSED, result);
    }

    @Test
    @DisplayName("整改审批通过后应回写为在用")
    void shouldMapRectificationApprovedToInUse()
    {
        AssetStatus result = matrix.mapToAssetStatus(AssetApprovalType.RECTIFICATION, AssetApprovalStatus.APPROVED);
        assertEquals(AssetStatus.IN_USE, result);
    }

    @Test
    @DisplayName("驳回结果不应触发资产状态回写")
    void shouldRejectRejectedToAssetStatus()
    {
        ServiceException exception = assertThrows(ServiceException.class,
            () -> matrix.mapToAssetStatus(AssetApprovalType.DISPOSAL, AssetApprovalStatus.REJECTED));

        assertEquals("审批结果不触发资产状态回写", exception.getMessage());
    }
}

