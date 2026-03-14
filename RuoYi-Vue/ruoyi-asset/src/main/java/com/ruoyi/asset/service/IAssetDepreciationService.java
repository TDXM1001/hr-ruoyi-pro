package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetDepreciationLog;

/**
 * 资产折旧服务接口。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public interface IAssetDepreciationService {
    /**
     * 按资产ID查询折旧日志。
     *
     * @param assetId 资产ID
     * @return 折旧日志
     */
    public List<AssetDepreciationLog> selectAssetDepreciationLogByAssetId(Long assetId);

    /**
     * 按资产和期间执行一次折旧计提。
     *
     * @param assetId 资产ID
     * @param period 会计期间
     * @return 折旧日志
     */
    public AssetDepreciationLog accrueDepreciation(Long assetId, String period);
}
