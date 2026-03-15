package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateStatusChange;

/**
 * 不动产状态变更服务接口。
 */
public interface IAssetRealEstateStatusChangeService {
    public AssetRealEstateStatusChange selectStatusChangeByNo(String statusChangeNo);

    public List<AssetRealEstateStatusChange> selectStatusChangeList(AssetRealEstateStatusChange statusChange);

    public int insertStatusChange(AssetRealEstateStatusChange statusChange);
}
