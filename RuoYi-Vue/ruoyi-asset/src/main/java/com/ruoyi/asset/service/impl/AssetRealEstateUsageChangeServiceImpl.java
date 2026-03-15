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

    @Autowired
    private AssetRealEstateUsageChangeMapper usageChangeMapper;

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Autowired
    private AssetRealEstateMapper assetRealEstateMapper;

    @Override
    public AssetRealEstateUsageChange selectUsageChangeByNo(String usageChangeNo) {
        return usageChangeMapper.selectUsageChangeByNo(usageChangeNo);
    }

    @Override
    public List<AssetRealEstateUsageChange> selectUsageChangeList(AssetRealEstateUsageChange usageChange) {
        return usageChangeMapper.selectUsageChangeList(usageChange);
    }

    @Transactional
    @Override
    public int insertUsageChange(AssetRealEstateUsageChange usageChange) {
        usageChange.setCreateTime(DateUtils.getNowDate());
        usageChange.setStatus(STATUS_COMPLETED);

        AssetInfo assetInfo = resolveAsset(usageChange.getAssetId(), usageChange.getAssetNo());
        validateRealEstateAsset(assetInfo, "仅不动产允许发起用途变更");

        AssetRealEstate realEstate = assetRealEstateMapper.selectAssetRealEstateByAssetId(assetInfo.getAssetId());
        if (realEstate == null) {
            throw new ServiceException("不动产主档不存在");
        }

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
