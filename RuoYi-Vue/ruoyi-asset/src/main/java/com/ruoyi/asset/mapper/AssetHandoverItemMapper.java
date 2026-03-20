package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetHandoverItem;
import com.ruoyi.asset.domain.vo.AssetHandoverItemVo;

/**
 * 资产交接明细Mapper接口。
 *
 * @author Codex
 */
public interface AssetHandoverItemMapper
{
    /**
     * 按主单查询交接明细。
     *
     * @param handoverOrderId 主单ID
     * @return 明细列表
     */
    List<AssetHandoverItemVo> selectAssetHandoverItemsByOrderId(Long handoverOrderId);

    /**
     * 按资产查询交接明细。
     *
     * @param assetId 资产ID
     * @return 明细列表
     */
    List<AssetHandoverItemVo> selectAssetHandoverItemsByAssetId(Long assetId);

    /**
     * 批量新增交接明细。
     *
     * @param items 明细列表
     * @return 影响行数
     */
    int batchInsertAssetHandoverItems(List<AssetHandoverItem> items);
}
