package com.ruoyi.asset.service.impl;

import java.util.List;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.service.IAssetInfoService;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 资产主档Service业务层处理
 *
 * @author ruoyi
 * @date 2026-03-14
 */
@Service
public class AssetInfoServiceImpl implements IAssetInfoService {
    @Autowired
    private AssetInfoMapper assetInfoMapper;

    /**
     * 按资产ID查询资产主档。
     *
     * @param assetId 资产ID
     * @return 资产主档
     */
    @Override
    public AssetInfo selectAssetInfoByAssetId(Long assetId) {
        return assetInfoMapper.selectAssetInfoByAssetId(assetId);
    }

    /**
     * 按资产编号查询资产主档。
     *
     * @param assetNo 资产编号
     * @return 资产主档
     */
    @Override
    public AssetInfo selectAssetInfoByAssetNo(String assetNo) {
        return assetInfoMapper.selectAssetInfoByAssetNo(assetNo);
    }

    /**
     * 查询资产主档列表。
     *
     * @param assetInfo 查询条件
     * @return 资产主档集合
     */
    @Override
    public List<AssetInfo> selectAssetInfoList(AssetInfo assetInfo) {
        return assetInfoMapper.selectAssetInfoList(assetInfo);
    }

    /**
     * 新增资产主档。
     *
     * @param assetInfo 资产主档
     * @return 结果
     */
    @Override
    public int insertAssetInfo(AssetInfo assetInfo) {
        assetInfo.setCreateTime(DateUtils.getNowDate());
        assetInfo.setUpdateTime(DateUtils.getNowDate());
        if (assetInfo.getAssetStatus() == null || assetInfo.getAssetStatus().isEmpty()) {
            assetInfo.setAssetStatus("1");
        }
        return assetInfoMapper.insertAssetInfo(assetInfo);
    }

    /**
     * 修改资产主档。
     *
     * @param assetInfo 资产主档
     * @return 结果
     */
    @Override
    public int updateAssetInfo(AssetInfo assetInfo) {
        assetInfo.setUpdateTime(DateUtils.getNowDate());
        return assetInfoMapper.updateAssetInfo(assetInfo);
    }

    /**
     * 批量删除资产主档。
     *
     * @param assetIds 资产ID集合
     * @return 结果
     */
    @Override
    public int deleteAssetInfoByAssetIds(Long[] assetIds) {
        return assetInfoMapper.deleteAssetInfoByAssetIds(assetIds);
    }

    /**
     * 删除单个资产主档。
     *
     * @param assetId 资产ID
     * @return 结果
     */
    @Override
    public int deleteAssetInfoByAssetId(Long assetId) {
        return assetInfoMapper.deleteAssetInfoByAssetId(assetId);
    }
}
