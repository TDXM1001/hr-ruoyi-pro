package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetHandover;
import com.ruoyi.asset.domain.bo.AssetHandoverBo;
import com.ruoyi.asset.domain.vo.AssetHandoverVo;

/**
 * 资产交接数据层。
 *
 * @author Codex
 */
public interface AssetHandoverMapper
{
    /**
     * 查询交接记录列表。
     *
     * @param bo 查询条件
     * @return 交接列表
     */
    List<AssetHandoverVo> selectAssetHandoverList(AssetHandoverBo bo);

    /**
     * 查询当前前缀下最大交接单号。
     *
     * @param handoverNoPrefix 单号前缀
     * @return 最大单号
     */
    String selectMaxHandoverNoByPrefix(String handoverNoPrefix);

    /**
     * 新增交接记录。
     *
     * @param assetHandover 交接记录
     * @return 结果
     */
    int insertAssetHandover(AssetHandover assetHandover);
}
