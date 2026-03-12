package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetRequisition;

/**
 * 资产领用Mapper接口
 */
public interface AssetRequisitionMapper {
    /** 查询领用详情 */
    public AssetRequisition selectAssetRequisitionByRequisitionNo(String requisitionNo);

    /** 查询领用列表 */
    public List<AssetRequisition> selectAssetRequisitionList(AssetRequisition assetRequisition);

    /** 新增领用 */
    public int insertAssetRequisition(AssetRequisition assetRequisition);

    /** 更新领用 */
    public int updateAssetRequisition(AssetRequisition assetRequisition);
}
