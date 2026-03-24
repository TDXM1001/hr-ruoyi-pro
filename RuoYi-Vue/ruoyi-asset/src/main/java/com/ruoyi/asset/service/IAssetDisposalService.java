package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetApprovalRecord;
import com.ruoyi.asset.domain.bo.AssetApprovalActionBo;
import com.ruoyi.asset.domain.bo.AssetDisposalBo;
import com.ruoyi.asset.domain.vo.AssetDisposalVo;

/**
 * 资产处置服务接口。
 *
 * @author Codex
 */
public interface IAssetDisposalService
{
    /**
     * 查询处置列表。
     *
     * @param bo 查询参数
     * @return 处置列表
     */
    List<AssetDisposalVo> selectAssetDisposalList(AssetDisposalBo bo);

    /**
     * 查询处置详情。
     *
     * @param disposalId 处置ID
     * @return 处置详情
     */
    AssetDisposalVo selectAssetDisposalById(Long disposalId);

    /**
     * 提交处置审批。
     *
     * @param bo 处置参数
     * @param operator 操作人
     * @return 处置ID
     */
    Long submitDisposalApproval(AssetDisposalBo bo, String operator);

    /**
     * 审批通过处置单。
     *
     * @param disposalId 处置单ID
     * @param bo 审批动作
     * @param operator 审批人
     */
    void approveDisposal(Long disposalId, AssetApprovalActionBo bo, String operator);

    /**
     * 驳回处置单。
     *
     * @param disposalId 处置单ID
     * @param bo 审批动作
     * @param operator 审批人
     */
    void rejectDisposal(Long disposalId, AssetApprovalActionBo bo, String operator);

    /**
     * 查询处置审批轨迹。
     *
     * @param disposalId 处置单ID
     * @return 审批轨迹
     */
    List<AssetApprovalRecord> selectDisposalApprovalRecords(Long disposalId);
}
