package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetRequisition;

/**
 * 资产领用服务接口
 */
public interface IAssetRequisitionService {
    /** 查询资产领用 */
    public AssetRequisition selectAssetRequisitionByRequisitionNo(String requisitionNo);
    
    /** 查询资产领用列表 */
    public List<AssetRequisition> selectAssetRequisitionList(AssetRequisition assetRequisition);
    
    /** 申请资产领用 */
    public int insertAssetRequisition(AssetRequisition assetRequisition);
}
