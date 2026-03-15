package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetMaintenance;

/**
 * 资产维修服务接口
 */
public interface IAssetMaintenanceService {
    public AssetMaintenance selectAssetMaintenanceByMaintenanceNo(String maintenanceNo);
    public List<AssetMaintenance> selectAssetMaintenanceList(AssetMaintenance assetMaintenance);
    public int insertAssetMaintenance(AssetMaintenance assetMaintenance);

    /**
     * 完成维修，资产状态回到在用。
     *
     * @param maintenanceNo 维修单号
     * @return 影响行数
     */
    public int completeMaintenance(String maintenanceNo);
}
