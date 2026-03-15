package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateDisposal;

/**
 * 不动产注销/处置服务接口。
 */
public interface IAssetRealEstateDisposalService {
    public AssetRealEstateDisposal selectDisposalByNo(String disposalNo);

    public List<AssetRealEstateDisposal> selectDisposalList(AssetRealEstateDisposal disposal);

    public int insertDisposal(AssetRealEstateDisposal disposal);
}
