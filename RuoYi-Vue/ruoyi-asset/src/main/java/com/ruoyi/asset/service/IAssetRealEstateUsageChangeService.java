package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateUsageChange;

/**
 * 不动产用途变更服务接口。
 */
public interface IAssetRealEstateUsageChangeService {
    public AssetRealEstateUsageChange selectUsageChangeByNo(String usageChangeNo);

    public List<AssetRealEstateUsageChange> selectUsageChangeList(AssetRealEstateUsageChange usageChange);

    public int insertUsageChange(AssetRealEstateUsageChange usageChange);
}
