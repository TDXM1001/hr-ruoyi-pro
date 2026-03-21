package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetRectificationApprovalRecord;
import com.ruoyi.asset.domain.bo.AssetRectificationApprovalActionBo;
import com.ruoyi.asset.domain.bo.AssetRectificationBo;
import com.ruoyi.asset.domain.bo.AssetRectificationCompleteBo;
import com.ruoyi.asset.domain.vo.AssetRectificationVo;

/**
 * 资产整改服务接口。
 *
 * @author Codex
 */
public interface IAssetRectificationService
{
    /**
     * 按资产查询整改列表。
     *
     * @param assetId 资产ID
     * @return 整改列表
     */
    List<AssetRectificationVo> selectAssetRectificationListByAssetId(Long assetId);

    /**
     * 查询整改详情。
     *
     * @param rectificationId 整改单ID
     * @return 整改详情
     */
    AssetRectificationVo selectAssetRectificationById(Long rectificationId);

    /**
     * 发起整改。
     *
     * @param bo 整改入参
     * @param operator 操作人
     * @return 整改单ID
     */
    Long createAssetRectification(AssetRectificationBo bo, String operator);

    /**
     * 更新整改。
     *
     * @param bo 整改入参
     * @param operator 操作人
     * @return 更新结果
     */
    int updateAssetRectification(AssetRectificationBo bo, String operator);

    int completeAssetRectification(Long assetId, Long rectificationId, AssetRectificationCompleteBo bo,
        String operator);

    List<AssetRectificationApprovalRecord> selectRectificationApprovalRecords(Long assetId, Long rectificationId);

    int submitRectificationApproval(Long assetId, Long rectificationId, AssetRectificationApprovalActionBo bo,
        String operator);

    int approveRectificationApproval(Long assetId, Long rectificationId, AssetRectificationApprovalActionBo bo,
        String operator);

    int rejectRectificationApproval(Long assetId, Long rectificationId, AssetRectificationApprovalActionBo bo,
        String operator);
}
