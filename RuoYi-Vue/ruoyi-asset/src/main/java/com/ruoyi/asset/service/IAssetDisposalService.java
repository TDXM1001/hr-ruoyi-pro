package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetDisposal;

/**
 * 固定资产报废/处置服务接口。
 */
public interface IAssetDisposalService {
    public AssetDisposal selectAssetDisposalByDisposalNo(String disposalNo);
    public List<AssetDisposal> selectAssetDisposalList(AssetDisposal assetDisposal);
    public int insertAssetDisposal(AssetDisposal assetDisposal);
}
