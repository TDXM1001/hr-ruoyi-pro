package com.ruoyi.asset.service.impl;

import java.util.List;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetMaintenance;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.mapper.AssetMaintenanceMapper;
import com.ruoyi.asset.service.IAssetMaintenanceService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.workflow.service.IApprovalEngine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 资产维修服务实现
 */
@Service
public class AssetMaintenanceServiceImpl implements IAssetMaintenanceService {
    private static final String ASSET_STATUS_ACTIVE = "1";
    private static final String ASSET_STATUS_MAINTENANCE = "3";
    private static final String ASSET_STATUS_SCRAPPED = "5";
    private static final String ASSET_STATUS_DISPOSED = "6";
    private static final String WF_STATUS_PENDING = "pending";
    private static final String WF_STATUS_APPROVED = "approved";
    private static final String WF_STATUS_REJECTED = "rejected";
    private static final String WF_STATUS_COMPLETED = "completed";
    
    @Autowired
    private AssetMaintenanceMapper assetMaintenanceMapper;

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Autowired
    private IApprovalEngine approvalEngine;

    @Override
    public AssetMaintenance selectAssetMaintenanceByMaintenanceNo(String maintenanceNo) {
        return fillWorkflowStatus(assetMaintenanceMapper.selectAssetMaintenanceByMaintenanceNo(maintenanceNo));
    }

    @Override
    public List<AssetMaintenance> selectAssetMaintenanceList(AssetMaintenance assetMaintenance) {
        List<AssetMaintenance> list = assetMaintenanceMapper.selectAssetMaintenanceList(assetMaintenance);
        if (list != null) {
            list.forEach(this::fillWorkflowStatus);
        }
        return list;
    }

    @Transactional
    @Override
    public int insertAssetMaintenance(AssetMaintenance assetMaintenance) {
        assetMaintenance.setCreateTime(DateUtils.getNowDate());
        assetMaintenance.setStatus(0); // 审批中
        assetMaintenance.setWfStatus(WF_STATUS_PENDING);

        // 统一解析资产主档，保证维保流程与资产主档通过 assetId 关联。
        AssetInfo assetInfo = resolveAsset(assetMaintenance.getAssetId(), assetMaintenance.getAssetNo());
        validateAssetForMaintenance(assetInfo);
        assetMaintenance.setAssetId(assetInfo.getAssetId());
        assetMaintenance.setAssetNo(assetInfo.getAssetNo());
        
        int rows = assetMaintenanceMapper.insertAssetMaintenance(assetMaintenance);

        // 修改资产状态为"维修中" (状态3为维修中)
        updateAssetStatus(assetInfo.getAssetId(), ASSET_STATUS_MAINTENANCE);

        // 发起审批流程
        approvalEngine.startProcess(assetMaintenance.getMaintenanceNo(), "asset_maintenance");
        
        return rows;
    }

    @Transactional
    @Override
    public int completeMaintenance(String maintenanceNo) {
        AssetMaintenance maintenance = assetMaintenanceMapper.selectAssetMaintenanceByMaintenanceNo(maintenanceNo);
        if (maintenance == null) {
            throw new ServiceException("维修单不存在");
        }
        if (!Integer.valueOf(1).equals(maintenance.getStatus())) {
            throw new ServiceException("仅审批通过的维修单允许完成维修");
        }

        AssetMaintenance updatePayload = new AssetMaintenance();
        updatePayload.setMaintenanceNo(maintenanceNo);
        updatePayload.setStatus(3);
        updatePayload.setWfStatus(WF_STATUS_COMPLETED);
        int rows = assetMaintenanceMapper.updateAssetMaintenance(updatePayload);

        // 维修完成意味着资产重新可用，因此需要回退到“在用”状态。
        updateAssetStatus(maintenance.getAssetId(), ASSET_STATUS_ACTIVE);
        return rows;
    }

    private AssetInfo resolveAsset(Long assetId, String assetNo) {
        AssetInfo assetInfo;
        if (assetId != null) {
            assetInfo = assetInfoMapper.selectAssetInfoByAssetId(assetId);
        } else if (StringUtils.isNotBlank(assetNo)) {
            assetInfo = assetInfoMapper.selectAssetInfoByAssetNo(assetNo);
        } else {
            throw new ServiceException("资产ID或资产编号不能为空");
        }
        if (assetInfo == null) {
            throw new ServiceException("关联资产不存在");
        }
        return assetInfo;
    }

    /**
     * 已处置或已报废资产不允许继续发起维修流程。
     */
    private void validateAssetForMaintenance(AssetInfo assetInfo) {
        if (ASSET_STATUS_DISPOSED.equals(assetInfo.getAssetStatus())) {
            throw new ServiceException("已处置资产不能维修");
        }
        if (ASSET_STATUS_SCRAPPED.equals(assetInfo.getAssetStatus())) {
            throw new ServiceException("已报废资产不能维修");
        }
    }

    private void updateAssetStatus(Long assetId, String assetStatus) {
        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetId(assetId);
        assetInfo.setAssetStatus(assetStatus);
        assetInfoMapper.updateAssetInfo(assetInfo);
    }

    /**
     * 在未落库 `wfStatus` 之前，统一根据单据状态补齐流程状态。
     */
    private AssetMaintenance fillWorkflowStatus(AssetMaintenance assetMaintenance) {
        if (assetMaintenance == null || StringUtils.isNotBlank(assetMaintenance.getWfStatus())) {
            return assetMaintenance;
        }
        if (assetMaintenance.getStatus() == null) {
            return assetMaintenance;
        }
        switch (assetMaintenance.getStatus()) {
            case 0 -> assetMaintenance.setWfStatus(WF_STATUS_PENDING);
            case 1 -> assetMaintenance.setWfStatus(WF_STATUS_APPROVED);
            case 2 -> assetMaintenance.setWfStatus(WF_STATUS_REJECTED);
            case 3 -> assetMaintenance.setWfStatus(WF_STATUS_COMPLETED);
            default -> {
            }
        }
        return assetMaintenance;
    }
}
