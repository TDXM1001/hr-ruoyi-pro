package com.ruoyi.asset.service.impl;

import java.util.List;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetRealEstate;
import com.ruoyi.asset.domain.AssetRealEstateUsageChange;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.mapper.AssetRealEstateMapper;
import com.ruoyi.asset.mapper.AssetRealEstateUsageChangeMapper;
import com.ruoyi.asset.service.IAssetRealEstateUsageChangeService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 不动产用途变更服务实现。
 */
@Service
public class AssetRealEstateUsageChangeServiceImpl implements IAssetRealEstateUsageChangeService {
    private static final String REAL_ESTATE_ASSET_TYPE = "2";
    private static final String STATUS_COMPLETED = "completed";
    private static final String MISSING_ASSET_ID_MESSAGE = "不动产业务统一要求由资产台账带入资产主键";
    private static final String ASSET_NOT_FOUND_MESSAGE = "关联资产不存在";
    private static final String REAL_ESTATE_NOT_FOUND_MESSAGE = "不动产主档不存在";
    private static final String FIXED_ASSET_ERROR_MESSAGE = "固定资产不能发起不动产用途变更";

    @Autowired
    private AssetRealEstateUsageChangeMapper usageChangeMapper;

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Autowired
    private AssetRealEstateMapper assetRealEstateMapper;

    @Override
    public AssetRealEstateUsageChange selectUsageChangeByNo(String usageChangeNo) {
        return fillWorkflowStatus(usageChangeMapper.selectUsageChangeByNo(usageChangeNo));
    }

    @Override
    public List<AssetRealEstateUsageChange> selectUsageChangeList(AssetRealEstateUsageChange usageChange) {
        List<AssetRealEstateUsageChange> list = usageChangeMapper.selectUsageChangeList(usageChange);
        if (list != null) {
            list.forEach(this::fillWorkflowStatus);
        }
        return list;
    }

    @Transactional
    @Override
    public int insertUsageChange(AssetRealEstateUsageChange usageChange) {
        usageChange.setCreateTime(DateUtils.getNowDate());
        usageChange.setStatus(STATUS_COMPLETED);
        usageChange.setWfStatus(STATUS_COMPLETED);

        AssetInfo assetInfo = resolveAsset(usageChange.getAssetId());
        validateRealEstateAsset(assetInfo, FIXED_ASSET_ERROR_MESSAGE);

        AssetRealEstate realEstate = assetRealEstateMapper.selectAssetRealEstateByAssetId(assetInfo.getAssetId());
        if (realEstate == null) {
            throw new ServiceException(REAL_ESTATE_NOT_FOUND_MESSAGE);
        }

        // 先从主档回填旧值，再按目标值即时回写主档，保证前后口径一致。
        usageChange.setAssetId(assetInfo.getAssetId());
        usageChange.setAssetNo(assetInfo.getAssetNo());
        usageChange.setOldLandUse(realEstate.getLandUse());
        usageChange.setOldBuildingUse(realEstate.getBuildingUse());

        int rows = usageChangeMapper.insertUsageChange(usageChange);

        AssetRealEstate updatePayload = new AssetRealEstate();
        updatePayload.setAssetId(assetInfo.getAssetId());
        updatePayload.setLandUse(usageChange.getTargetLandUse());
        updatePayload.setBuildingUse(usageChange.getTargetBuildingUse());
        assetRealEstateMapper.updateAssetRealEstate(updatePayload);
        return rows;
    }

    /**
     * 不动产业务统一要求由资产台账带入资产主键，避免用途回写误命中错误资产。
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
    private AssetRealEstateUsageChange fillWorkflowStatus(AssetRealEstateUsageChange usageChange) {
        if (usageChange == null || StringUtils.isNotBlank(usageChange.getWfStatus())) {
            return usageChange;
        }
        usageChange.setWfStatus(StringUtils.defaultIfBlank(usageChange.getStatus(), STATUS_COMPLETED));
        return usageChange;
    }
}
