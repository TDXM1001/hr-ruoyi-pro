package com.ruoyi.asset.mapper;

import com.ruoyi.asset.domain.AssetChangeLog;

/**
 * 资产变更日志数据层。
 *
 * @author Codex
 */
public interface AssetChangeLogMapper
{
    /**
     * 新增资产变更日志。
     *
     * @param assetChangeLog 变更日志
     * @return 结果
     */
    public int insertAssetChangeLog(AssetChangeLog assetChangeLog);
}
