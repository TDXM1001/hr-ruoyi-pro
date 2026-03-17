package com.ruoyi.asset.service.impl;

import java.util.List;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetRealEstate;
import com.ruoyi.asset.domain.AssetRealEstateOwnershipChange;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.mapper.AssetRealEstateMapper;
import com.ruoyi.asset.mapper.AssetRealEstateOwnershipChangeMapper;
import com.ruoyi.asset.service.IAssetRealEstateOwnershipChangeService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.workflow.service.IApprovalEngine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 不动产权属变更服务实现。
 */
@Service
public class AssetRealEstateOwnershipChangeServiceImpl implements IAssetRealEstateOwnershipChangeService {
    private static final String REAL_ESTATE_ASSET_TYPE = "2";
    private static final String STATUS_PENDING = "pending";
    private static final String MISSING_ASSET_ID_MESSAGE = "不动产业务统一要求由资产台账带入资产主键";
    private static final String ASSET_NOT_FOUND_MESSAGE = "关联资产不存在";
    private static final String REAL_ESTATE_NOT_FOUND_MESSAGE = "不动产主档不存在";
    private static final String FIXED_ASSET_ERROR_MESSAGE = "固定资产不能发起不动产权属变更";

    @Autowired
    private AssetRealEstateOwnershipChangeMapper ownershipChangeMapper;

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Autowired
    private AssetRealEstateMapper assetRealEstateMapper;

    @Autowired
    private IApprovalEngine approvalEngine;

    @Override
    public AssetRealEstateOwnershipChange selectOwnershipChangeByNo(String ownershipChangeNo) {
        return fillWorkflowStatus(ownershipChangeMapper.selectOwnershipChangeByNo(ownershipChangeNo));
    }

    @Override
    public List<AssetRealEstateOwnershipChange> selectOwnershipChangeList(AssetRealEstateOwnershipChange ownershipChange) {
        List<AssetRealEstateOwnershipChange> list = ownershipChangeMapper.selectOwnershipChangeList(ownershipChange);
        if (list != null) {
            list.forEach(this::fillWorkflowStatus);
        }
        return list;
    }

    @Transactional
    @Override
    public int insertOwnershipChange(AssetRealEstateOwnershipChange ownershipChange) {
        ownershipChange.setCreateTime(DateUtils.getNowDate());
        ownershipChange.setStatus(STATUS_PENDING);
        ownershipChange.setWfStatus(STATUS_PENDING);

        AssetInfo assetInfo = resolveAsset(ownershipChange.getAssetId());
        validateRealEstateAsset(assetInfo, FIXED_ASSET_ERROR_MESSAGE);

        AssetRealEstate realEstate = assetRealEstateMapper.selectAssetRealEstateByAssetId(assetInfo.getAssetId());
        if (realEstate == null) {
            throw new ServiceException(REAL_ESTATE_NOT_FOUND_MESSAGE);
        }

        // 统一以资产主档和不动产主档回填老值，保证单据快照稳定可追溯。
        ownershipChange.setAssetId(assetInfo.getAssetId());
        ownershipChange.setAssetNo(assetInfo.getAssetNo());
        ownershipChange.setOldRightsHolder(realEstate.getRightsHolder());
        ownershipChange.setOldPropertyCertNo(realEstate.getPropertyCertNo());
        ownershipChange.setOldRegistrationDate(realEstate.getRegistrationDate());

        int rows = ownershipChangeMapper.insertOwnershipChange(ownershipChange);
        approvalEngine.startProcess(ownershipChange.getOwnershipChangeNo(), "asset_real_estate_ownership_change");
        return rows;
    }

    /**
     * 不动产业务统一要求由资产台账带入资产主键，避免单据误关联到错误主档。
     */
    private AssetInfo resolveAsset(Long assetId) {
        if (assetId == null || assetId <= 0) {
            throw new ServiceException(MISSING_ASSET_ID_MESSAGE);
        }

        AssetInfo assetInfo = assetInfoMapper.selectAssetInfoByAssetId(assetId);
        if (assetInfo == null) {
            throw new ServiceException(ASSET_NOT_FOUND_MESSAGE);
        }
        return assetInfo;
    }

    private void validateRealEstateAsset(AssetInfo assetInfo, String errorMessage) {
        if (!REAL_ESTATE_ASSET_TYPE.equals(assetInfo.getAssetType())) {
            throw new ServiceException(errorMessage);
        }
    }

    /**
     * 老数据未回填 wfStatus 时，前端仍可按统一流程状态展示。
     */
    private AssetRealEstateOwnershipChange fillWorkflowStatus(AssetRealEstateOwnershipChange ownershipChange) {
        if (ownershipChange == null || StringUtils.isNotBlank(ownershipChange.getWfStatus())) {
            return ownershipChange;
        }
        ownershipChange.setWfStatus(StringUtils.defaultIfBlank(ownershipChange.getStatus(), STATUS_PENDING));
        return ownershipChange;
    }
}
