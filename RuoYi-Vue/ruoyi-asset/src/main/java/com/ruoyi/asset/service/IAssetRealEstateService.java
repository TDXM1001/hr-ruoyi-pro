package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateProfile;

/**
 * 不动产档案服务接口。
 *
 * @author Codex
 */
public interface IAssetRealEstateService
{
    /**
     * 查询不动产权属档案列表。
     *
     * @param query 查询条件
     * @return 档案列表
     */
    List<AssetRealEstateProfile> selectList(AssetRealEstateProfile query);

    /**
     * 按资产ID查询不动产权属档案。
     *
     * @param assetId 资产ID
     * @return 权属档案
     */
    AssetRealEstateProfile selectByAssetId(Long assetId);
}
