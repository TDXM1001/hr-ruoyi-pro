package com.ruoyi.asset.service.impl;

import java.util.List;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetRealEstateDisposal;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.mapper.AssetRealEstateDisposalMapper;
import com.ruoyi.asset.service.IAssetRealEstateDisposalService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.workflow.service.IApprovalEngine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 不动产注销/处置服务实现。
 */
@Service
public class AssetRealEstateDisposalServiceImpl implements IAssetRealEstateDisposalService {
    private static final String REAL_ESTATE_ASSET_TYPE = "2";
    private static final String STATUS_PENDING = "pending";
    private static final String TARGET_STATUS_DISPOSED = "6";
    private static final String MISSING_ASSET_ID_MESSAGE = "不动产业务统一要求由资产台账带入资产主键";
    private static final String ASSET_NOT_FOUND_MESSAGE = "关联资产不存在";
    private static final String FIXED_ASSET_ERROR_MESSAGE = "固定资产不能发起不动产注销/处置";

    @Autowired
    private AssetRealEstateDisposalMapper disposalMapper;

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Autowired
    private IApprovalEngine approvalEngine;

    @Override
    public AssetRealEstateDisposal selectDisposalByNo(String disposalNo) {
        return fillWorkflowStatus(disposalMapper.selectDisposalByNo(disposalNo));
    }

    @Override
    public List<AssetRealEstateDisposal> selectDisposalList(AssetRealEstateDisposal disposal) {
        List<AssetRealEstateDisposal> list = disposalMapper.selectDisposalList(disposal);
        if (list != null) {
            list.forEach(this::fillWorkflowStatus);
        }
        return list;
    }

    @Transactional
    @Override
    public int insertDisposal(AssetRealEstateDisposal disposal) {
        disposal.setCreateTime(DateUtils.getNowDate());
        disposal.setStatus(STATUS_PENDING);
        disposal.setWfStatus(STATUS_PENDING);

        AssetInfo assetInfo = resolveAsset(disposal.getAssetId());
        validateRealEstateAsset(assetInfo, FIXED_ASSET_ERROR_MESSAGE);

        disposal.setAssetId(assetInfo.getAssetId());
        disposal.setAssetNo(assetInfo.getAssetNo());
        disposal.setOldAssetStatus(assetInfo.getAssetStatus());
        if (StringUtils.isBlank(disposal.getTargetAssetStatus())) {
            disposal.setTargetAssetStatus(TARGET_STATUS_DISPOSED);
        }

        int rows = disposalMapper.insertDisposal(disposal);
        approvalEngine.startProcess(disposal.getDisposalNo(), "asset_real_estate_disposal");
        return rows;
    }

    /**
     * 不动产业务统一要求由资产台账带入资产主键，避免处置流程误关联到错误资产。
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
     * 老数据未回填 wfStatus 时，统一按待审批展示。
     */
    private AssetRealEstateDisposal fillWorkflowStatus(AssetRealEstateDisposal disposal) {
        if (disposal == null || StringUtils.isNotBlank(disposal.getWfStatus())) {
            return disposal;
        }
        disposal.setWfStatus(StringUtils.defaultIfBlank(disposal.getStatus(), STATUS_PENDING));
        return disposal;
    }
}
