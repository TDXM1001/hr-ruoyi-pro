package com.ruoyi.asset.service.impl;

import java.util.List;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetRealEstateStatusChange;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.mapper.AssetRealEstateStatusChangeMapper;
import com.ruoyi.asset.service.IAssetRealEstateStatusChangeService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 不动产状态变更服务实现。
 */
@Service
public class AssetRealEstateStatusChangeServiceImpl implements IAssetRealEstateStatusChangeService {
    private static final String REAL_ESTATE_ASSET_TYPE = "2";
    private static final String STATUS_COMPLETED = "completed";

    @Autowired
    private AssetRealEstateStatusChangeMapper statusChangeMapper;

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Override
    public AssetRealEstateStatusChange selectStatusChangeByNo(String statusChangeNo) {
        return fillWorkflowStatus(statusChangeMapper.selectStatusChangeByNo(statusChangeNo));
    }

    @Override
    public List<AssetRealEstateStatusChange> selectStatusChangeList(AssetRealEstateStatusChange statusChange) {
        List<AssetRealEstateStatusChange> list = statusChangeMapper.selectStatusChangeList(statusChange);
        if (list != null) {
            list.forEach(this::fillWorkflowStatus);
        }
        return list;
    }

    @Transactional
    @Override
    public int insertStatusChange(AssetRealEstateStatusChange statusChange) {
        statusChange.setCreateTime(DateUtils.getNowDate());
        statusChange.setStatus(STATUS_COMPLETED);
        statusChange.setWfStatus(STATUS_COMPLETED);

        AssetInfo assetInfo = resolveAsset(statusChange.getAssetId(), statusChange.getAssetNo());
        validateRealEstateAsset(assetInfo, "仅不动产允许发起状态变更");

        statusChange.setAssetId(assetInfo.getAssetId());
        statusChange.setAssetNo(assetInfo.getAssetNo());
        statusChange.setOldAssetStatus(assetInfo.getAssetStatus());

        int rows = statusChangeMapper.insertStatusChange(statusChange);

        AssetInfo updatePayload = new AssetInfo();
        updatePayload.setAssetId(assetInfo.getAssetId());
        updatePayload.setAssetStatus(statusChange.getTargetAssetStatus());
        assetInfoMapper.updateAssetInfo(updatePayload);
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

    /**
     * 状态变更为即时动作，流程态直接跟随单据态。
     */
    private AssetRealEstateStatusChange fillWorkflowStatus(AssetRealEstateStatusChange statusChange) {
        if (statusChange == null || StringUtils.isNotBlank(statusChange.getWfStatus())) {
            return statusChange;
        }
        statusChange.setWfStatus(StringUtils.defaultIfBlank(statusChange.getStatus(), STATUS_COMPLETED));
        return statusChange;
    }
}
