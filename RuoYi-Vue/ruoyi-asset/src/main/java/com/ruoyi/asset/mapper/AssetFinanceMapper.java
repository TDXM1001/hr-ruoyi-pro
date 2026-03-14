package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetFinance;

/**
 * 资产财务Mapper接口
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public interface AssetFinanceMapper {
    public AssetFinance selectAssetFinanceByFinanceId(Long financeId);

    public AssetFinance selectAssetFinanceByAssetId(Long assetId);

    public List<AssetFinance> selectAssetFinanceList(AssetFinance assetFinance);

    public int insertAssetFinance(AssetFinance assetFinance);

    public int updateAssetFinance(AssetFinance assetFinance);

    public int deleteAssetFinanceByFinanceId(Long financeId);

    public int deleteAssetFinanceByFinanceIds(Long[] financeIds);
}
