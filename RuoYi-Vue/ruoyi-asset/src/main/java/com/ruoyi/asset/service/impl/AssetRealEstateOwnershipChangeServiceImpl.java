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
        return ownershipChangeMapper.selectOwnershipChangeByNo(ownershipChangeNo);
    }

    @Override
    public List<AssetRealEstateOwnershipChange> selectOwnershipChangeList(AssetRealEstateOwnershipChange ownershipChange) {
        return ownershipChangeMapper.selectOwnershipChangeList(ownershipChange);
    }

    @Transactional
    @Override
    public int insertOwnershipChange(AssetRealEstateOwnershipChange ownershipChange) {
        ownershipChange.setCreateTime(DateUtils.getNowDate());
        ownershipChange.setStatus(STATUS_PENDING);

        AssetInfo assetInfo = resolveAsset(ownershipChange.getAssetId(), ownershipChange.getAssetNo());
        validateRealEstateAsset(assetInfo, "仅不动产允许发起权属变更");

        AssetRealEstate realEstate = assetRealEstateMapper.selectAssetRealEstateByAssetId(assetInfo.getAssetId());
        if (realEstate == null) {
            throw new ServiceException("不动产主档不存在");
        }

        // 单据记录前值和目标值，审批通过前不改当前事实。
        ownershipChange.setAssetId(assetInfo.getAssetId());
        ownershipChange.setAssetNo(assetInfo.getAssetNo());
        ownershipChange.setOldRightsHolder(realEstate.getRightsHolder());
        ownershipChange.setOldPropertyCertNo(realEstate.getPropertyCertNo());
        ownershipChange.setOldRegistrationDate(realEstate.getRegistrationDate());

        int rows = ownershipChangeMapper.insertOwnershipChange(ownershipChange);
        approvalEngine.startProcess(ownershipChange.getOwnershipChangeNo(), "asset_real_estate_ownership_change");
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

    private void validateRealEstateAsset(AssetInfo assetInfo, String errorMessage) {
        if (!REAL_ESTATE_ASSET_TYPE.equals(assetInfo.getAssetType())) {
            throw new ServiceException(errorMessage);
        }
    }
}
