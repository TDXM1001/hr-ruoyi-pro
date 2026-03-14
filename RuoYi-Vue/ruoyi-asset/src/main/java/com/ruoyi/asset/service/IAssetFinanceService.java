package com.ruoyi.asset.service;

import com.ruoyi.asset.domain.AssetFinance;

/**
 * 资产财务计算服务接口。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public interface IAssetFinanceService {
    /**
     * 按资产ID查询财务信息。
     *
     * @param assetId 资产ID
     * @return 财务信息
     */
    public AssetFinance selectAssetFinanceByAssetId(Long assetId);

    /**
     * 重新计算资产财务结果并回写数据表。
     *
     * @param assetId 资产ID
     * @return 重算后的财务信息
     */
    public AssetFinance recalculateFinanceByAssetId(Long assetId);

    /**
     * 计算基础财务结果，默认预览首期折旧金额。
     *
     * @param assetFinance 财务信息
     * @return 计算后的财务信息
     */
    public AssetFinance calculateFinance(AssetFinance assetFinance);

    /**
     * 计算指定折旧期末的财务快照。
     *
     * @param assetFinance 财务信息
     * @param periodIndex 折旧期次，从1开始
     * @return 期末财务快照
     */
    public AssetFinance calculateDepreciatedFinance(AssetFinance assetFinance, int periodIndex);
}
