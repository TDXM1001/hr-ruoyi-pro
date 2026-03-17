package com.ruoyi.asset.service.impl;

import java.util.List;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetRequisition;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.mapper.AssetRequisitionMapper;
import com.ruoyi.asset.service.IAssetRequisitionService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.workflow.service.IApprovalEngine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 固定资产领用服务实现。
 */
@Service
public class AssetRequisitionServiceImpl implements IAssetRequisitionService {
    private static final String ASSET_STATUS_REQUISITION = "2";
    private static final String ASSET_STATUS_ACTIVE = "1";
    private static final String ASSET_STATUS_MAINTENANCE = "3";
    private static final String ASSET_STATUS_SCRAPPED = "5";
    private static final String ASSET_STATUS_DISPOSED = "6";
    private static final String WF_STATUS_PENDING = "pending";
    private static final String WF_STATUS_APPROVED = "approved";
    private static final String WF_STATUS_REJECTED = "rejected";
    private static final String WF_STATUS_COMPLETED = "completed";

    @Autowired
    private AssetRequisitionMapper assetRequisitionMapper;

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Autowired
    private IApprovalEngine approvalEngine;

    @Override
    public AssetRequisition selectAssetRequisitionByRequisitionNo(String requisitionNo) {
        return fillWorkflowStatus(assetRequisitionMapper.selectAssetRequisitionByRequisitionNo(requisitionNo));
    }

    @Override
    public List<AssetRequisition> selectAssetRequisitionList(AssetRequisition assetRequisition) {
        List<AssetRequisition> list = assetRequisitionMapper.selectAssetRequisitionList(assetRequisition);
        if (list != null) {
            list.forEach(this::fillWorkflowStatus);
        }
        return list;
    }

    @Transactional
    @Override
    public int insertAssetRequisition(AssetRequisition assetRequisition) {
        assetRequisition.setCreateTime(DateUtils.getNowDate());
        assetRequisition.setStatus(0);
        assetRequisition.setWfStatus(WF_STATUS_PENDING);

        AssetInfo assetInfo = resolveAsset(assetRequisition.getAssetId());
        validateAssetForRequisition(assetInfo);
        assetRequisition.setAssetId(assetInfo.getAssetId());
        assetRequisition.setAssetNo(assetInfo.getAssetNo());

        int rows = assetRequisitionMapper.insertAssetRequisition(assetRequisition);
        updateAssetStatus(assetInfo.getAssetId(), ASSET_STATUS_REQUISITION);
        approvalEngine.startProcess(assetRequisition.getRequisitionNo(), "asset_requisition");
        return rows;
    }

    @Transactional
    @Override
    public int returnAsset(String requisitionNo) {
        AssetRequisition requisition = assetRequisitionMapper.selectAssetRequisitionByRequisitionNo(requisitionNo);
        if (requisition == null) {
            throw new ServiceException("领用单不存在");
        }
        if (!Integer.valueOf(1).equals(requisition.getStatus())) {
            throw new ServiceException("只有已审批的领用单才能归还资产");
        }

        AssetRequisition updatePayload = new AssetRequisition();
        updatePayload.setRequisitionNo(requisitionNo);
        updatePayload.setStatus(3);
        updatePayload.setWfStatus(WF_STATUS_COMPLETED);
        int rows = assetRequisitionMapper.updateAssetRequisition(updatePayload);

        updateAssetStatus(requisition.getAssetId(), ASSET_STATUS_ACTIVE);
        return rows;
    }

    /**
     * 固定资产业务统一要求由资产台账带入资产主键。
     */
    private AssetInfo resolveAsset(Long assetId) {
        if (assetId == null || assetId <= 0) {
            throw new ServiceException("固定资产业务必须传入资产主键");
        }

        AssetInfo assetInfo = assetInfoMapper.selectAssetInfoByAssetId(assetId);
        if (assetInfo == null) {
            throw new ServiceException("关联资产不存在");
        }
        return assetInfo;
    }

    /**
     * 校验资产当前状态是否允许继续发起领用。
     */
    private void validateAssetForRequisition(AssetInfo assetInfo) {
        if (ASSET_STATUS_MAINTENANCE.equals(assetInfo.getAssetStatus())) {
            throw new ServiceException("维修中的资产不能继续领用");
        }
        if (ASSET_STATUS_DISPOSED.equals(assetInfo.getAssetStatus())) {
            throw new ServiceException("已处置资产不能继续领用");
        }
        if (ASSET_STATUS_SCRAPPED.equals(assetInfo.getAssetStatus())) {
            throw new ServiceException("已报废资产不能继续领用");
        }
    }

    /**
     * 同步回写资产主档状态。
     */
    private void updateAssetStatus(Long assetId, String assetStatus) {
        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetId(assetId);
        assetInfo.setAssetStatus(assetStatus);
        assetInfoMapper.updateAssetInfo(assetInfo);
    }

    /**
     * 历史数据可能缺失 `wfStatus`，查询时按单据状态回填。
     */
    private AssetRequisition fillWorkflowStatus(AssetRequisition assetRequisition) {
        if (assetRequisition == null || StringUtils.isNotBlank(assetRequisition.getWfStatus())) {
            return assetRequisition;
        }
        if (assetRequisition.getStatus() == null) {
            return assetRequisition;
        }
        switch (assetRequisition.getStatus()) {
            case 0 -> assetRequisition.setWfStatus(WF_STATUS_PENDING);
            case 1 -> assetRequisition.setWfStatus(WF_STATUS_APPROVED);
            case 2 -> assetRequisition.setWfStatus(WF_STATUS_REJECTED);
            case 3 -> assetRequisition.setWfStatus(WF_STATUS_COMPLETED);
            default -> {
            }
        }
        return assetRequisition;
    }
}
