package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetRectificationOrder;
import com.ruoyi.asset.domain.vo.AssetRectificationVo;

/**
 * 资产整改数据层。
 *
 * @author Codex
 */
public interface AssetRectificationMapper
{
    /**
     * 按资产查询整改记录。
     *
     * @param assetId 资产ID
     * @return 整改记录列表
     */
    List<AssetRectificationVo> selectAssetRectificationListByAssetId(Long assetId);

    /**
     * 查询整改详情。
     *
     * @param rectificationId 整改单ID
     * @return 整改单详情
     */
    AssetRectificationVo selectAssetRectificationById(Long rectificationId);

    /**
     * 按盘点明细查询整改单。
     *
     * @param inventoryItemId 盘点明细ID
     * @return 整改单
     */
    AssetRectificationOrder selectByInventoryItemId(Long inventoryItemId);

    /**
     * 查询当前编号前缀下最大整改单号。
     *
     * @param rectificationNoPrefix 编号前缀
     * @return 最大整改单号
     */
    String selectMaxRectificationNoByPrefix(String rectificationNoPrefix);

    /**
     * 新增整改单。
     *
     * @param order 整改单
     * @return 结果
     */
    int insertAssetRectification(AssetRectificationOrder order);

    /**
     * 更新整改单。
     *
     * @param order 整改单
     * @return 结果
     */
    int updateAssetRectification(AssetRectificationOrder order);
}
