package com.ruoyi.asset.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.asset.domain.AssetRealEstateProfile;
import com.ruoyi.asset.mapper.AssetRealEstateMapper;
import com.ruoyi.asset.service.IAssetRealEstateService;
import com.ruoyi.common.exception.ServiceException;

/**
 * 不动产档案服务实现。
 *
 * <p>当前实现仅提供最小查询能力，支撑资产管理员在台账详情页核查不动产权属信息。</p>
 *
 * @author Codex
 */
@Service
public class AssetRealEstateServiceImpl implements IAssetRealEstateService
{
    @Autowired
    private AssetRealEstateMapper assetRealEstateMapper;

    /**
     * 查询不动产权属档案列表。
     *
     * @param query 查询条件
     * @return 档案列表
     */
    @Override
    public List<AssetRealEstateProfile> selectList(AssetRealEstateProfile query)
    {
        return assetRealEstateMapper.selectList(query);
    }

    /**
     * 按资产ID查询不动产权属档案。
     *
     * @param assetId 资产ID
     * @return 权属档案
     */
    @Override
    public AssetRealEstateProfile selectByAssetId(Long assetId)
    {
        if (assetId == null)
        {
            throw new ServiceException("资产ID不能为空");
        }
        return assetRealEstateMapper.selectByAssetId(assetId);
    }
}
