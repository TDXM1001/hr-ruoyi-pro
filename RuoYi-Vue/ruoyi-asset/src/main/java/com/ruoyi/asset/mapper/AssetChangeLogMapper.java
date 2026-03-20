package com.ruoyi.asset.mapper;

import java.util.List;
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

    /**
     * 按资产ID查询变更日志列表。
     *
     * @param assetId 资产ID
     * @return 变更日志列表
     */
    public List<AssetChangeLog> selectAssetChangeLogListByAssetId(Long assetId);
}
