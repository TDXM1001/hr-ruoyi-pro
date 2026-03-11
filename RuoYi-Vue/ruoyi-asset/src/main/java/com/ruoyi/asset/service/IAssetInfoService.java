package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetInfo;

/**
 * 资产信息Service接口
 *
 * @author ruoyi
 * @date 2026-03-11
 */
public interface IAssetInfoService {
    /**
     * 查询资产信息
     *
     * @param assetNo 资产编号
     * @return 资产信息
     */
    public AssetInfo selectAssetInfoByAssetNo(String assetNo);

    /**
     * 查询资产信息列表
     *
     * @param assetInfo 资产信息
     * @return 资产信息集合
     */
    public List<AssetInfo> selectAssetInfoList(AssetInfo assetInfo);

    /**
     * 新增资产信息
     *
     * @param assetInfo 资产信息
     * @return 结果
     */
    public int insertAssetInfo(AssetInfo assetInfo);

    /**
     * 修改资产信息
     *
     * @param assetInfo 资产信息
     * @return 结果
     */
    public int updateAssetInfo(AssetInfo assetInfo);

    /**
     * 批量删除资产信息
     *
     * @param assetNos 需要删除的资产信息主键集合
     * @return 结果
     */
    public int deleteAssetInfoByAssetNos(String[] assetNos);

    /**
     * 删除资产信息信息
     *
     * @param assetNo 资产编号
     * @return 结果
     */
    public int deleteAssetInfoByAssetNo(String assetNo);
}
