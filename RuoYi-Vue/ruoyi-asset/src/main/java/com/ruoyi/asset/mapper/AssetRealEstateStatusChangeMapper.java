package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateStatusChange;

/**
 * 不动产状态变更 Mapper。
 */
public interface AssetRealEstateStatusChangeMapper {
    public AssetRealEstateStatusChange selectStatusChangeByNo(String statusChangeNo);

    public List<AssetRealEstateStatusChange> selectStatusChangeList(AssetRealEstateStatusChange statusChange);

    public int insertStatusChange(AssetRealEstateStatusChange statusChange);
}
