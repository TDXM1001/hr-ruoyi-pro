package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetInfo;

/**
 * 资产主档Mapper接口
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public interface AssetInfoMapper {
    /**
     * 按资产ID查询资产主档。
     *
     * @param assetId 资产ID
     * @return 资产主档
     */
    public AssetInfo selectAssetInfoByAssetId(Long assetId);

    /**
     * 按资产编号查询资产主档。
     *
     * @param assetNo 资产编号
     * @return 资产主档
     */
    public AssetInfo selectAssetInfoByAssetNo(String assetNo);

    /**
     * 查询资产主档列表。
     *
     * @param assetInfo 查询条件
     * @return 资产主档集合
     */
    public List<AssetInfo> selectAssetInfoList(AssetInfo assetInfo);

    /**
     * 新增资产主档。
     *
     * @param assetInfo 资产主档
     * @return 结果
     */
    public int insertAssetInfo(AssetInfo assetInfo);

    /**
     * 修改资产主档。
     *
     * @param assetInfo 资产主档
     * @return 结果
     */
    public int updateAssetInfo(AssetInfo assetInfo);

    /**
     * 按资产ID删除资产主档。
     *
     * @param assetId 资产ID
     * @return 结果
     */
    public int deleteAssetInfoByAssetId(Long assetId);

    /**
     * 批量删除资产主档。
     *
     * @param assetIds 资产ID集合
     * @return 结果
     */
    public int deleteAssetInfoByAssetIds(Long[] assetIds);
}
