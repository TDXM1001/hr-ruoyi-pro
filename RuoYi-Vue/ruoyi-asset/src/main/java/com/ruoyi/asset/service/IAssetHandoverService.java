package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.bo.AssetHandoverBo;
import com.ruoyi.asset.domain.vo.AssetHandoverVo;

/**
 * 资产交接服务接口。
 *
 * @author Codex
 */
public interface IAssetHandoverService
{
    /**
     * 查询交接记录列表。
     *
     * @param bo 查询条件
     * @return 交接列表
     */
    List<AssetHandoverVo> selectAssetHandoverList(AssetHandoverBo bo);

    /**
     * 新增交接记录并回写台账使用关系。
     *
     * @param bo 交接入参
     * @param operator 操作人
     * @return 交接ID
     */
    Long createHandover(AssetHandoverBo bo, String operator);
}
