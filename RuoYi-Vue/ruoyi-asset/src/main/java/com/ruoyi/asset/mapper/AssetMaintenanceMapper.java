package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetMaintenance;

/**
 * 资产维修 Mapper 接口。
 *
 * 维修与处置两个流水表本阶段统一回收到 XML 映射文件，
 * 避免注解 SQL 和资源目录混用时难以定位真实执行入口。
 */
public interface AssetMaintenanceMapper {
    /** 查询维修单详情 */
    public AssetMaintenance selectAssetMaintenanceByMaintenanceNo(String maintenanceNo);

    /** 查询列表 */
    public List<AssetMaintenance> selectAssetMaintenanceList(AssetMaintenance assetMaintenance);

    /** 新增 */
    public int insertAssetMaintenance(AssetMaintenance assetMaintenance);

    /** 更新 */
    public int updateAssetMaintenance(AssetMaintenance assetMaintenance);
}
