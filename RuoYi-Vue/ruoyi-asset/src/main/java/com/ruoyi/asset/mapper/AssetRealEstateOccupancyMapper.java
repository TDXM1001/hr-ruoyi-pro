package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateOccupancyOrder;
import com.ruoyi.asset.domain.vo.AssetRealEstateOccupancyVo;

/**
 * 不动产占用数据层。
 *
 * @author Codex
 */
public interface AssetRealEstateOccupancyMapper
{
    AssetRealEstateOccupancyOrder selectActiveOccupancyByAssetId(Long assetId);

    AssetRealEstateOccupancyOrder selectOccupancyById(Long occupancyId);

    List<AssetRealEstateOccupancyVo> selectOccupancyListByAssetId(Long assetId);

    String selectMaxOccupancyNoByPrefix(String occupancyNoPrefix);

    int insertOccupancy(AssetRealEstateOccupancyOrder occupancyOrder);

    int updateOccupancy(AssetRealEstateOccupancyOrder occupancyOrder);
}
