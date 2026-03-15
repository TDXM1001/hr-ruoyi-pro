package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateDisposal;

/**
 * 不动产注销/处置 Mapper。
 */
public interface AssetRealEstateDisposalMapper {
    public AssetRealEstateDisposal selectDisposalByNo(String disposalNo);

    public List<AssetRealEstateDisposal> selectDisposalList(AssetRealEstateDisposal disposal);

    public int insertDisposal(AssetRealEstateDisposal disposal);

    public int updateDisposal(AssetRealEstateDisposal disposal);
}
