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
 * 资产领用服务实现
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
        // 设置默认值
        assetRequisition.setCreateTime(DateUtils.getNowDate());
        assetRequisition.setStatus(0); // 0=审批中
        assetRequisition.setWfStatus(WF_STATUS_PENDING);

        // 优先基于资产ID解析资产，兼容旧调用方仅传资产编号的情况。
        AssetInfo assetInfo = resolveAsset(assetRequisition.getAssetId(), assetRequisition.getAssetNo());
        validateAssetForRequisition(assetInfo);
        assetRequisition.setAssetId(assetInfo.getAssetId());
        assetRequisition.setAssetNo(assetInfo.getAssetNo());
        
        // 保存领用单
        int rows = assetRequisitionMapper.insertAssetRequisition(assetRequisition);

        // 修改资产状态为"领用中"
        updateAssetStatus(assetInfo.getAssetId(), ASSET_STATUS_REQUISITION);

        // 发起审批流程
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
            throw new ServiceException("仅审批通过的领用单允许归还");
        }

        AssetRequisition updatePayload = new AssetRequisition();
        updatePayload.setRequisitionNo(requisitionNo);
        updatePayload.setStatus(3);
        updatePayload.setWfStatus(WF_STATUS_COMPLETED);
        int rows = assetRequisitionMapper.updateAssetRequisition(updatePayload);

        // 归还动作是生命周期上的闭环点，完成后资产必须回到“在用”状态。
        updateAssetStatus(requisition.getAssetId(), ASSET_STATUS_ACTIVE);
        return rows;
    }

    /**
     * 统一解析业务单据关联的资产，内部始终落到 assetId。
     */
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
     * 领用前校验资产生命周期状态，避免和维保、处置流程冲突。
     */
    private void validateAssetForRequisition(AssetInfo assetInfo) {
        if (ASSET_STATUS_MAINTENANCE.equals(assetInfo.getAssetStatus())) {
            throw new ServiceException("维修中的资产不能领用");
        }
        if (ASSET_STATUS_DISPOSED.equals(assetInfo.getAssetStatus())) {
            throw new ServiceException("已处置资产不能领用");
        }
        if (ASSET_STATUS_SCRAPPED.equals(assetInfo.getAssetStatus())) {
            throw new ServiceException("已报废资产不能领用");
        }
    }

    /**
     * 资产状态更新统一使用 assetId，避免继续依赖业务编码更新。
     */
    private void updateAssetStatus(Long assetId, String assetStatus) {
        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetId(assetId);
        assetInfo.setAssetStatus(assetStatus);
        assetInfoMapper.updateAssetInfo(assetInfo);
    }

    /**
     * 在未落库 `wfStatus` 之前，统一根据单据状态推导流程态，保证接口返回稳定。
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
