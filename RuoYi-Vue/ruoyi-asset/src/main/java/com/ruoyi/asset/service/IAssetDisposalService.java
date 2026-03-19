package com.ruoyi.asset.service;

import java.util.List;
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
     * 确认处置。
     *
     * @param bo 处置参数
     * @param operator 操作人
     * @return 处置ID
     */
    Long confirmDisposal(AssetDisposalBo bo, String operator);
}
