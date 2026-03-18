package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.bo.AssetHandoverCreateBo;
import com.ruoyi.asset.domain.bo.AssetHandoverOrderBo;
import com.ruoyi.asset.domain.vo.AssetHandoverItemVo;
import com.ruoyi.asset.domain.vo.AssetHandoverOrderVo;

/**
 * 资产交接服务接口。
 *
 * @author Codex
 */
public interface IAssetHandoverService
{
    /**
     * 查询交接主单列表。
     *
     * @param bo 查询参数
     * @return 主单列表
     */
    List<AssetHandoverOrderVo> selectAssetHandoverOrderList(AssetHandoverOrderBo bo);

    /**
     * 查询交接主单下的资产明细。
     *
     * @param handoverOrderId 主单ID
     * @return 明细列表
     */
    List<AssetHandoverItemVo> selectAssetHandoverItemsByOrderId(Long handoverOrderId);

    /**
     * 创建交接主单并回写台账使用信息。
     *
     * @param bo 建单参数
     * @param operator 操作人
     * @return 交接主单ID
     */
    Long createHandoverOrder(AssetHandoverCreateBo bo, String operator);
}
