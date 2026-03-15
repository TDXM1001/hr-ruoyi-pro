package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetDisposal;

/**
 * 资产处置 Mapper 接口。
 *
 * 本阶段统一采用 XML 映射，确保后续详情查询、列表过滤和状态更新
 * 能与 SQL 脚本和测试契约保持一致。
 */
public interface AssetDisposalMapper {
    public AssetDisposal selectAssetDisposalByDisposalNo(String disposalNo);

    public List<AssetDisposal> selectAssetDisposalList(AssetDisposal assetDisposal);

    public int insertAssetDisposal(AssetDisposal assetDisposal);

    public int updateAssetDisposal(AssetDisposal assetDisposal);
}
