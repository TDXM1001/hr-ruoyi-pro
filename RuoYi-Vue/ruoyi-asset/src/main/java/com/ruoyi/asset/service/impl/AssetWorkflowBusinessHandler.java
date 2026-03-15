package com.ruoyi.asset.service.impl;

import java.util.List;
import com.ruoyi.asset.domain.AssetDisposal;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetMaintenance;
import com.ruoyi.asset.domain.AssetRequisition;
import com.ruoyi.asset.mapper.AssetDisposalMapper;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.mapper.AssetMaintenanceMapper;
import com.ruoyi.asset.mapper.AssetRequisitionMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.workflow.service.WorkflowBusinessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 资产模块的工作流回调处理器。
 *
 * 它统一承接资产相关审批结果，把审批引擎的“通过/驳回”语义翻译成
 * 资产业务单据状态和资产主档生命周期状态，避免把这套判断分散到各个服务里。
 */
@Component
public class AssetWorkflowBusinessHandler implements WorkflowBusinessHandler {
    /** 资产在用。 */
    private static final String ASSET_STATUS_ACTIVE = "1";

    /** 资产领用中。 */
    private static final String ASSET_STATUS_REQUISITION = "2";

    /** 资产维修中。 */
    private static final String ASSET_STATUS_MAINTENANCE = "3";

    /** 资产已报废。 */
    private static final String ASSET_STATUS_SCRAPPED = "5";

    @Autowired
    private AssetRequisitionMapper assetRequisitionMapper;

    @Autowired
    private AssetMaintenanceMapper assetMaintenanceMapper;

    @Autowired
    private AssetDisposalMapper assetDisposalMapper;

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Override
    public List<String> getSupportedBusinessTypes() {
        return List.of("asset_requisition", "asset_maintenance", "asset_disposal");
    }

    @Override
    public void onApprove(String businessType, String businessId) {
        switch (businessType) {
            case "asset_requisition" -> approveRequisition(businessId);
            case "asset_maintenance" -> approveMaintenance(businessId);
            case "asset_disposal" -> approveDisposal(businessId);
            default -> throw new ServiceException("不支持的资产业务类型: " + businessType);
        }
    }

    @Override
    public void onReject(String businessType, String businessId) {
        switch (businessType) {
            case "asset_requisition" -> rejectRequisition(businessId);
            case "asset_maintenance" -> rejectMaintenance(businessId);
            case "asset_disposal" -> rejectDisposal(businessId);
            default -> throw new ServiceException("不支持的资产业务类型: " + businessType);
        }
    }

    /**
     * 领用审批通过后，单据进入“已通过”，资产继续保持“领用中”。
     *
     * 这里显式再写一次资产状态而不是依赖申请时留下的旧值，
     * 是为了确保审批补偿、重复部署后的回放场景仍能把最终状态收敛正确。
     */
    private void approveRequisition(String businessId) {
        AssetRequisition requisition = requireRequisition(businessId);
        AssetRequisition updatePayload = new AssetRequisition();
        updatePayload.setRequisitionNo(requisition.getRequisitionNo());
        updatePayload.setStatus(1);
        assetRequisitionMapper.updateAssetRequisition(updatePayload);

        updateAssetStatus(requisition.getAssetId(), ASSET_STATUS_REQUISITION);
    }

    /**
     * 领用审批驳回后，单据置为“已驳回”，资产退回“在用”。
     */
    private void rejectRequisition(String businessId) {
        AssetRequisition requisition = requireRequisition(businessId);
        AssetRequisition updatePayload = new AssetRequisition();
        updatePayload.setRequisitionNo(requisition.getRequisitionNo());
        updatePayload.setStatus(2);
        assetRequisitionMapper.updateAssetRequisition(updatePayload);

        updateAssetStatus(requisition.getAssetId(), ASSET_STATUS_ACTIVE);
    }

    /**
     * 维修审批通过后，单据进入“已通过”，资产保持“维修中”。
     */
    private void approveMaintenance(String businessId) {
        AssetMaintenance maintenance = requireMaintenance(businessId);
        AssetMaintenance updatePayload = new AssetMaintenance();
        updatePayload.setMaintenanceNo(maintenance.getMaintenanceNo());
        updatePayload.setStatus(1);
        assetMaintenanceMapper.updateAssetMaintenance(updatePayload);

        updateAssetStatus(maintenance.getAssetId(), ASSET_STATUS_MAINTENANCE);
    }

    /**
     * 维修审批驳回后，单据置为“已驳回”，资产退回“在用”。
     */
    private void rejectMaintenance(String businessId) {
        AssetMaintenance maintenance = requireMaintenance(businessId);
        AssetMaintenance updatePayload = new AssetMaintenance();
        updatePayload.setMaintenanceNo(maintenance.getMaintenanceNo());
        updatePayload.setStatus(2);
        assetMaintenanceMapper.updateAssetMaintenance(updatePayload);

        updateAssetStatus(maintenance.getAssetId(), ASSET_STATUS_ACTIVE);
    }

    /**
     * 处置审批通过后，当前阶段统一按“已报废”收口。
     *
     * 当前域模型还没有把 disposalType 正式扩成“报废/处置”两条精细分支，
     * 因此本批次先落到状态 5，等后续处置类型细化时再扩展到状态 6。
     */
    private void approveDisposal(String businessId) {
        AssetDisposal disposal = requireDisposal(businessId);
        AssetDisposal updatePayload = new AssetDisposal();
        updatePayload.setDisposalNo(disposal.getDisposalNo());
        updatePayload.setStatus(1);
        assetDisposalMapper.updateAssetDisposal(updatePayload);

        updateAssetStatus(disposal.getAssetId(), ASSET_STATUS_SCRAPPED);
    }

    /**
     * 处置审批驳回后，单据置为“已驳回”，资产退回“在用”。
     */
    private void rejectDisposal(String businessId) {
        AssetDisposal disposal = requireDisposal(businessId);
        AssetDisposal updatePayload = new AssetDisposal();
        updatePayload.setDisposalNo(disposal.getDisposalNo());
        updatePayload.setStatus(2);
        assetDisposalMapper.updateAssetDisposal(updatePayload);

        updateAssetStatus(disposal.getAssetId(), ASSET_STATUS_ACTIVE);
    }

    /**
     * 保证领用单存在，否则审批回调不应继续落库。
     */
    private AssetRequisition requireRequisition(String businessId) {
        AssetRequisition requisition = assetRequisitionMapper.selectAssetRequisitionByRequisitionNo(businessId);
        if (requisition == null) {
            throw new ServiceException("领用单不存在: " + businessId);
        }
        return requisition;
    }

    /**
     * 保证维修单存在。
     */
    private AssetMaintenance requireMaintenance(String businessId) {
        AssetMaintenance maintenance = assetMaintenanceMapper.selectAssetMaintenanceByMaintenanceNo(businessId);
        if (maintenance == null) {
            throw new ServiceException("维修单不存在: " + businessId);
        }
        return maintenance;
    }

    /**
     * 保证处置单存在。
     */
    private AssetDisposal requireDisposal(String businessId) {
        AssetDisposal disposal = assetDisposalMapper.selectAssetDisposalByDisposalNo(businessId);
        if (disposal == null) {
            throw new ServiceException("处置单不存在: " + businessId);
        }
        return disposal;
    }

    /**
     * 统一回写资产主档状态。
     */
    private void updateAssetStatus(Long assetId, String assetStatus) {
        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetId(assetId);
        assetInfo.setAssetStatus(assetStatus);
        assetInfoMapper.updateAssetInfo(assetInfo);
    }
}
