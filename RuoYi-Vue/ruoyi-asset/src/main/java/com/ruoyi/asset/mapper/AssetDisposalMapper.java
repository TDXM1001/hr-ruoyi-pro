package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetDisposal;
import com.ruoyi.asset.domain.bo.AssetDisposalBo;
import com.ruoyi.asset.domain.vo.AssetDisposalVo;

/**
 * 资产处置数据层。
 *
 * @author Codex
 */
public interface AssetDisposalMapper
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
     * 按资产查询处置记录。
     *
     * @param assetId 资产ID
     * @return 处置记录列表
     */
    List<AssetDisposalVo> selectAssetDisposalsByAssetId(Long assetId);

    /**
     * 查询指定前缀下最大处置单号。
     *
     * @param disposalNoPrefix 单号前缀
     * @return 最大处置单号
     */
    String selectMaxDisposalNoByPrefix(String disposalNoPrefix);

    /**
     * 新增处置单。
     *
     * @param assetDisposal 处置对象
     * @return 结果
     */
    int insertAssetDisposal(AssetDisposal assetDisposal);
}
