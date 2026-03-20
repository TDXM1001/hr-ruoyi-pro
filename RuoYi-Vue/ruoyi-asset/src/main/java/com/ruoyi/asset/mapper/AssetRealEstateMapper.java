package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateProfile;

/**
 * 不动产权属扩展档案 Mapper。
 *
 * @author Codex
 */
public interface AssetRealEstateMapper
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
