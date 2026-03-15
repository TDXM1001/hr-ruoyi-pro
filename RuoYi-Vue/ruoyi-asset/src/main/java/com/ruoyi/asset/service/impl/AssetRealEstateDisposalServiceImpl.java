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

    @Autowired
    private AssetRealEstateDisposalMapper disposalMapper;

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Autowired
    private IApprovalEngine approvalEngine;

    @Override
    public AssetRealEstateDisposal selectDisposalByNo(String disposalNo) {
        return disposalMapper.selectDisposalByNo(disposalNo);
    }

    @Override
    public List<AssetRealEstateDisposal> selectDisposalList(AssetRealEstateDisposal disposal) {
        return disposalMapper.selectDisposalList(disposal);
    }

    @Transactional
    @Override
    public int insertDisposal(AssetRealEstateDisposal disposal) {
        disposal.setCreateTime(DateUtils.getNowDate());
        disposal.setStatus(STATUS_PENDING);

        AssetInfo assetInfo = resolveAsset(disposal.getAssetId(), disposal.getAssetNo());
        validateRealEstateAsset(assetInfo, "仅不动产允许发起注销/处置");

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
