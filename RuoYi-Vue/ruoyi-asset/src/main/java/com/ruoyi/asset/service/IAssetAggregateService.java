package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.dto.AssetCreateReq;
import com.ruoyi.asset.domain.dto.AssetUpdateReq;
import com.ruoyi.asset.domain.vo.AssetDetailVo;
import com.ruoyi.asset.domain.vo.AssetListVo;

/**
 * 资产聚合服务接口。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public interface IAssetAggregateService {
    /**
     * 查询资产列表。
     *
     * @param assetInfo 查询条件
     * @return 列表视图
     */
    public List<AssetListVo> selectAssetList(AssetInfo assetInfo);

    /**
     * 查询资产聚合详情。
     *
     * @param assetId 资产ID
     * @return 资产详情
     */
    public AssetDetailVo selectAssetDetailByAssetId(Long assetId);

    /**
     * 新增聚合资产。
     *
     * @param assetCreateReq 聚合请求
     * @return 结果
     */
    public int insertAsset(AssetCreateReq assetCreateReq);

    /**
     * 修改聚合资产。
     *
     * @param assetUpdateReq 聚合请求
     * @return 结果
     */
    public int updateAsset(AssetUpdateReq assetUpdateReq);
}
