package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateUsageChange;

/**
 * 不动产用途变更 Mapper。
 */
public interface AssetRealEstateUsageChangeMapper {
    public AssetRealEstateUsageChange selectUsageChangeByNo(String usageChangeNo);

    public List<AssetRealEstateUsageChange> selectUsageChangeList(AssetRealEstateUsageChange usageChange);

    public int insertUsageChange(AssetRealEstateUsageChange usageChange);
}
