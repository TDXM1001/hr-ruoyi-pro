package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetDisposal;

/**
 * 资产报废服务接口
 */
public interface IAssetDisposalService {
    public AssetDisposal selectAssetDisposalByDisposalNo(String disposalNo);
    public List<AssetDisposal> selectAssetDisposalList(AssetDisposal assetDisposal);
    public int insertAssetDisposal(AssetDisposal assetDisposal);
}
