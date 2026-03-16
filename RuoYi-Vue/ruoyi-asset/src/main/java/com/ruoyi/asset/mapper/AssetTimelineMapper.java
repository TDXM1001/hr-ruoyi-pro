package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.vo.AssetTimelineVo;

/**
 * 资产统一时间线 Mapper。
 */
public interface AssetTimelineMapper {
    /**
     * 按资产主键查询统一动作时间线。
     *
     * @param assetId 资产ID
     * @return 统一动作时间线
     */
    public List<AssetTimelineVo> selectAssetTimelineByAssetId(Long assetId);
}
