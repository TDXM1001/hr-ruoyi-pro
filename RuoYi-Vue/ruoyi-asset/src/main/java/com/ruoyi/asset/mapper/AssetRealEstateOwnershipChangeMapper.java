package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateOwnershipChange;

/**
 * 不动产权属变更 Mapper。
 */
public interface AssetRealEstateOwnershipChangeMapper {
    public AssetRealEstateOwnershipChange selectOwnershipChangeByNo(String ownershipChangeNo);

    public List<AssetRealEstateOwnershipChange> selectOwnershipChangeList(AssetRealEstateOwnershipChange ownershipChange);

    public int insertOwnershipChange(AssetRealEstateOwnershipChange ownershipChange);

    public int updateOwnershipChange(AssetRealEstateOwnershipChange ownershipChange);
}
