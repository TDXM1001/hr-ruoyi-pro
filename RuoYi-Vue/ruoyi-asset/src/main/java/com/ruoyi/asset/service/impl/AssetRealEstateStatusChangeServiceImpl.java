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
    private static final String MISSING_ASSET_ID_MESSAGE = "不动产业务统一要求由资产台账带入资产主键";
    private static final String ASSET_NOT_FOUND_MESSAGE = "关联资产不存在";
    private static final String FIXED_ASSET_ERROR_MESSAGE = "固定资产不能发起不动产状态变更";

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

        AssetInfo assetInfo = resolveAsset(statusChange.getAssetId());
        validateRealEstateAsset(assetInfo, FIXED_ASSET_ERROR_MESSAGE);

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

    /**
     * 不动产业务统一要求由资产台账带入资产主键，避免状态回写误伤其他主档。
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
     * 老数据未回填 wfStatus 时，统一按已完成展示。
     */
    private AssetRealEstateStatusChange fillWorkflowStatus(AssetRealEstateStatusChange statusChange) {
        if (statusChange == null || StringUtils.isNotBlank(statusChange.getWfStatus())) {
            return statusChange;
        }
        statusChange.setWfStatus(StringUtils.defaultIfBlank(statusChange.getStatus(), STATUS_COMPLETED));
        return statusChange;
    }
}
