package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetHandoverOrder;
import com.ruoyi.asset.domain.bo.AssetHandoverOrderBo;
import com.ruoyi.asset.domain.vo.AssetHandoverOrderVo;

/**
 * 资产交接主单Mapper接口。
 *
 * @author Codex
 */
public interface AssetHandoverOrderMapper
{
    /**
     * 查询交接主单列表。
     *
     * @param bo 查询参数
     * @return 主单列表
     */
    List<AssetHandoverOrderVo> selectAssetHandoverOrderList(AssetHandoverOrderBo bo);

    /**
     * 查询交接主单详情。
     *
     * @param handoverOrderId 主单ID
     * @return 主单详情
     */
    AssetHandoverOrderVo selectAssetHandoverOrderById(Long handoverOrderId);

    /**
     * 查询指定前缀下的最大交接单号。
     *
     * @param handoverNoPrefix 单号前缀
     * @return 最大交接单号
     */
    String selectMaxHandoverNoByPrefix(String handoverNoPrefix);

    /**
     * 新增交接主单。
     *
     * @param assetHandoverOrder 交接主单
     * @return 影响行数
     */
    int insertAssetHandoverOrder(AssetHandoverOrder assetHandoverOrder);
}
