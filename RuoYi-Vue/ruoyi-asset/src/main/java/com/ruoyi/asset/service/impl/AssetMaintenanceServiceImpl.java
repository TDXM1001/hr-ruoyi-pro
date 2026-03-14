package com.ruoyi.asset.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.asset.mapper.AssetMaintenanceMapper;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.domain.AssetMaintenance;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.service.IAssetMaintenanceService;
import com.ruoyi.workflow.service.IApprovalEngine;
import com.ruoyi.common.utils.DateUtils;

/**
 * 资产维修服务实现
 */
@Service
public class AssetMaintenanceServiceImpl implements IAssetMaintenanceService {
    
    @Autowired
    private AssetMaintenanceMapper assetMaintenanceMapper;

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Autowired
    private IApprovalEngine approvalEngine;

    @Override
    public AssetMaintenance selectAssetMaintenanceByMaintenanceNo(String maintenanceNo) {
        return assetMaintenanceMapper.selectAssetMaintenanceByMaintenanceNo(maintenanceNo);
    }

    @Override
    public List<AssetMaintenance> selectAssetMaintenanceList(AssetMaintenance assetMaintenance) {
        return assetMaintenanceMapper.selectAssetMaintenanceList(assetMaintenance);
    }

    @Transactional
    @Override
    public int insertAssetMaintenance(AssetMaintenance assetMaintenance) {
        assetMaintenance.setCreateTime(DateUtils.getNowDate());
        assetMaintenance.setStatus(0); // 审批中
        
        int rows = assetMaintenanceMapper.insertAssetMaintenance(assetMaintenance);

        // 修改资产状态为"维修中" (状态3为维修中)
        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetNo(assetMaintenance.getAssetNo());
        assetInfo.setStatus("3"); // 3=维修中
        assetInfoMapper.updateAssetInfo(assetInfo);

        // 发起审批流程
        approvalEngine.startProcess(assetMaintenance.getMaintenanceNo(), "asset_maintenance");
        
        return rows;
    }
}
