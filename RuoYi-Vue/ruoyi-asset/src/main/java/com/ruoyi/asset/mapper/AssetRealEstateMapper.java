package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstate;

/**
 * 不动产Mapper接口
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public interface AssetRealEstateMapper {
    public AssetRealEstate selectAssetRealEstateByRealEstateId(Long realEstateId);

    public AssetRealEstate selectAssetRealEstateByAssetId(Long assetId);

    public List<AssetRealEstate> selectAssetRealEstateList(AssetRealEstate assetRealEstate);

    public int insertAssetRealEstate(AssetRealEstate assetRealEstate);

    public int updateAssetRealEstate(AssetRealEstate assetRealEstate);

    public int deleteAssetRealEstateByAssetId(Long assetId);

    public int deleteAssetRealEstateByRealEstateId(Long realEstateId);

    public int deleteAssetRealEstateByRealEstateIds(Long[] realEstateIds);
}
