package com.ruoyi.asset.service.impl;

import java.util.List;
import com.ruoyi.asset.domain.AssetDisposal;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetMaintenance;
import com.ruoyi.asset.domain.AssetRealEstate;
import com.ruoyi.asset.domain.AssetRealEstateDisposal;
import com.ruoyi.asset.domain.AssetRealEstateOwnershipChange;
import com.ruoyi.asset.domain.AssetRequisition;
import com.ruoyi.asset.mapper.AssetDisposalMapper;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.mapper.AssetMaintenanceMapper;
import com.ruoyi.asset.mapper.AssetRealEstateDisposalMapper;
import com.ruoyi.asset.mapper.AssetRealEstateMapper;
import com.ruoyi.asset.mapper.AssetRealEstateOwnershipChangeMapper;
import com.ruoyi.asset.mapper.AssetRequisitionMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
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

    /** 资产已处置。 */
    private static final String ASSET_STATUS_DISPOSED = "6";

    /** 单据审批通过。 */
    private static final String DOC_STATUS_APPROVED = "approved";

    /** 单据审批驳回。 */
    private static final String DOC_STATUS_REJECTED = "rejected";

    @Autowired
    private AssetRequisitionMapper assetRequisitionMapper;

    @Autowired
    private AssetMaintenanceMapper assetMaintenanceMapper;

    @Autowired
    private AssetDisposalMapper assetDisposalMapper;

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Autowired
    private AssetRealEstateOwnershipChangeMapper assetRealEstateOwnershipChangeMapper;

    @Autowired
    private AssetRealEstateDisposalMapper assetRealEstateDisposalMapper;

    @Autowired
    private AssetRealEstateMapper assetRealEstateMapper;

    @Override
    public List<String> getSupportedBusinessTypes() {
        return List.of(
            "asset_requisition",
            "asset_maintenance",
            "asset_disposal",
            "asset_real_estate_ownership_change",
            "asset_real_estate_disposal"
        );
    }

    @Override
    public void onApprove(String businessType, String businessId) {
        switch (businessType) {
            case "asset_requisition" -> approveRequisition(businessId);
            case "asset_maintenance" -> approveMaintenance(businessId);
            case "asset_disposal" -> approveDisposal(businessId);
            case "asset_real_estate_ownership_change" -> approveRealEstateOwnershipChange(businessId);
            case "asset_real_estate_disposal" -> approveRealEstateDisposal(businessId);
            default -> throw new ServiceException("不支持的资产业务类型: " + businessType);
        }
    }

    @Override
    public void onReject(String businessType, String businessId) {
        switch (businessType) {
            case "asset_requisition" -> rejectRequisition(businessId);
            case "asset_maintenance" -> rejectMaintenance(businessId);
            case "asset_disposal" -> rejectDisposal(businessId);
            case "asset_real_estate_ownership_change" -> rejectRealEstateOwnershipChange(businessId);
            case "asset_real_estate_disposal" -> rejectRealEstateDisposal(businessId);
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
     * 处置审批通过后，按处置类型把资产收敛到“已报废”或“已处置”。
     */
    private void approveDisposal(String businessId) {
        AssetDisposal disposal = requireDisposal(businessId);
        AssetDisposal updatePayload = new AssetDisposal();
        updatePayload.setDisposalNo(disposal.getDisposalNo());
        updatePayload.setStatus(1);
        assetDisposalMapper.updateAssetDisposal(updatePayload);

        updateAssetStatus(disposal.getAssetId(), resolveApprovedDisposalStatus(disposal));
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
     * 不动产权属变更审批通过后，再把目标值回写到主档。
     */
    private void approveRealEstateOwnershipChange(String businessId) {
        AssetRealEstateOwnershipChange ownershipChange = requireOwnershipChange(businessId);
        AssetRealEstateOwnershipChange updatePayload = new AssetRealEstateOwnershipChange();
        updatePayload.setOwnershipChangeNo(ownershipChange.getOwnershipChangeNo());
        updatePayload.setStatus(DOC_STATUS_APPROVED);
        updatePayload.setUpdateTime(DateUtils.getNowDate());
        assetRealEstateOwnershipChangeMapper.updateOwnershipChange(updatePayload);

        AssetRealEstate realEstate = new AssetRealEstate();
        realEstate.setAssetId(ownershipChange.getAssetId());
        realEstate.setRightsHolder(ownershipChange.getTargetRightsHolder());
        realEstate.setPropertyCertNo(ownershipChange.getTargetPropertyCertNo());
        realEstate.setRegistrationDate(ownershipChange.getTargetRegistrationDate());
        assetRealEstateMapper.updateAssetRealEstate(realEstate);
    }

    /**
     * 不动产权属变更驳回时只改单据状态，不污染当前事实。
     */
    private void rejectRealEstateOwnershipChange(String businessId) {
        AssetRealEstateOwnershipChange ownershipChange = requireOwnershipChange(businessId);
        AssetRealEstateOwnershipChange updatePayload = new AssetRealEstateOwnershipChange();
        updatePayload.setOwnershipChangeNo(ownershipChange.getOwnershipChangeNo());
        updatePayload.setStatus(DOC_STATUS_REJECTED);
        updatePayload.setUpdateTime(DateUtils.getNowDate());
        assetRealEstateOwnershipChangeMapper.updateOwnershipChange(updatePayload);
    }

    /**
     * 不动产注销/处置审批通过后才改资产终态。
     */
    private void approveRealEstateDisposal(String businessId) {
        AssetRealEstateDisposal disposal = requireRealEstateDisposal(businessId);
        AssetRealEstateDisposal updatePayload = new AssetRealEstateDisposal();
        updatePayload.setDisposalNo(disposal.getDisposalNo());
        updatePayload.setStatus(DOC_STATUS_APPROVED);
        updatePayload.setUpdateTime(DateUtils.getNowDate());
        assetRealEstateDisposalMapper.updateDisposal(updatePayload);

        updateAssetStatus(
            disposal.getAssetId(),
            disposal.getTargetAssetStatus() == null ? ASSET_STATUS_DISPOSED : disposal.getTargetAssetStatus()
        );
    }

    /**
     * 不动产注销/处置驳回时只改单据状态。
     */
    private void rejectRealEstateDisposal(String businessId) {
        AssetRealEstateDisposal disposal = requireRealEstateDisposal(businessId);
        AssetRealEstateDisposal updatePayload = new AssetRealEstateDisposal();
        updatePayload.setDisposalNo(disposal.getDisposalNo());
        updatePayload.setStatus(DOC_STATUS_REJECTED);
        updatePayload.setUpdateTime(DateUtils.getNowDate());
        assetRealEstateDisposalMapper.updateDisposal(updatePayload);
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
     * 保证不动产权属变更单存在。
     */
    private AssetRealEstateOwnershipChange requireOwnershipChange(String businessId) {
        AssetRealEstateOwnershipChange ownershipChange =
            assetRealEstateOwnershipChangeMapper.selectOwnershipChangeByNo(businessId);
        if (ownershipChange == null) {
            throw new ServiceException("权属变更单不存在: " + businessId);
        }
        return ownershipChange;
    }

    /**
     * 保证不动产注销/处置单存在。
     */
    private AssetRealEstateDisposal requireRealEstateDisposal(String businessId) {
        AssetRealEstateDisposal disposal = assetRealEstateDisposalMapper.selectDisposalByNo(businessId);
        if (disposal == null) {
            throw new ServiceException("不动产注销/处置单不存在: " + businessId);
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

    /**
     * 历史空值仍按报废兼容，新增单据已由服务层强制填写 disposalType。
     */
    private String resolveApprovedDisposalStatus(AssetDisposal disposal) {
        return AssetDisposal.isScrapType(disposal.getDisposalType())
            ? ASSET_STATUS_SCRAPPED
            : ASSET_STATUS_DISPOSED;
    }
}
