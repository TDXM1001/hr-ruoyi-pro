package com.ruoyi.asset.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.asset.domain.AssetApprovalRecord;

/**
 * 资产审批记录数据层。
 *
 * @author Codex
 */
public interface AssetApprovalMapper
{
    /**
     * 新增审批记录。
     *
     * @param approvalRecord 审批记录
     * @return 结果
     */
    int insertAssetApprovalRecord(AssetApprovalRecord approvalRecord);

    /**
     * 查询业务单据审批轨迹。
     *
     * @param approvalType 审批类型
     * @param bizId 单据ID
     * @return 审批轨迹
     */
    List<AssetApprovalRecord> selectAssetApprovalRecords(@Param("approvalType") String approvalType,
        @Param("bizId") Long bizId);
}
