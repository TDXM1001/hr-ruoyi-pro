package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetDepreciationLog;
import org.apache.ibatis.annotations.Param;

/**
 * 资产折旧日志Mapper接口
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public interface AssetDepreciationLogMapper {
    public AssetDepreciationLog selectAssetDepreciationLogByLogId(Long logId);

    public AssetDepreciationLog selectAssetDepreciationLogByAssetIdAndPeriod(@Param("assetId") Long assetId, @Param("period") String period);

    public List<AssetDepreciationLog> selectAssetDepreciationLogList(AssetDepreciationLog assetDepreciationLog);

    public List<AssetDepreciationLog> selectAssetDepreciationLogByAssetId(Long assetId);

    public int insertAssetDepreciationLog(AssetDepreciationLog assetDepreciationLog);

    public int updateAssetDepreciationLog(AssetDepreciationLog assetDepreciationLog);

    public int deleteAssetDepreciationLogByLogId(Long logId);

    public int deleteAssetDepreciationLogByLogIds(Long[] logIds);
}
