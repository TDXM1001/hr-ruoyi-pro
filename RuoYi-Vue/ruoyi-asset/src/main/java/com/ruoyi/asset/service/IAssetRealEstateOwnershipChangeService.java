package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateOwnershipChange;

/**
 * 不动产权属变更服务接口。
 */
public interface IAssetRealEstateOwnershipChangeService {
    public AssetRealEstateOwnershipChange selectOwnershipChangeByNo(String ownershipChangeNo);

    public List<AssetRealEstateOwnershipChange> selectOwnershipChangeList(AssetRealEstateOwnershipChange ownershipChange);

    public int insertOwnershipChange(AssetRealEstateOwnershipChange ownershipChange);
}
