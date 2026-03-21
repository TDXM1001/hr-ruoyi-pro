package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetRectificationApprovalRecord;

/**
 * 不动产整改审批轨迹数据层。
 *
 * @author Codex
 */
public interface AssetRectificationApprovalMapper
{
    /**
     * 新增整改审批轨迹。
     *
     * @param record 轨迹记录
     * @return 结果
     */
    int insertAssetRectificationApprovalRecord(AssetRectificationApprovalRecord record);

    /**
     * 查询整改审批轨迹。
     *
     * @param rectificationId 整改单ID
     * @return 审批轨迹
     */
    List<AssetRectificationApprovalRecord> selectAssetRectificationApprovalRecords(Long rectificationId);
}
