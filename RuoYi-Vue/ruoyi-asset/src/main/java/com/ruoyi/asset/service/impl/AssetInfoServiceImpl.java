package com.ruoyi.asset.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.service.IAssetInfoService;

/**
 * 资产信息Service业务层处理
 *
 * @author ruoyi
 * @date 2026-03-11
 */
@Service
public class AssetInfoServiceImpl implements IAssetInfoService {
    @Autowired
    private AssetInfoMapper assetInfoMapper;

    /**
     * 查询资产信息
     *
     * @param assetNo 资产编号
     * @return 资产信息
     */
    @Override
    public AssetInfo selectAssetInfoByAssetNo(String assetNo) {
        return assetInfoMapper.selectAssetInfoByAssetNo(assetNo);
    }

    /**
     * 查询资产信息列表
     *
     * @param assetInfo 资产信息
     * @return 资产信息
     */
    @Override
    public List<AssetInfo> selectAssetInfoList(AssetInfo assetInfo) {
        return assetInfoMapper.selectAssetInfoList(assetInfo);
    }

    /**
     * 新增资产信息
     *
     * @param assetInfo 资产信息
     * @return 结果
     */
    @Override
    public int insertAssetInfo(AssetInfo assetInfo) {
        assetInfo.setCreateTime(DateUtils.getNowDate());
        return assetInfoMapper.insertAssetInfo(assetInfo);
    }

    /**
     * 修改资产信息
     *
     * @param assetInfo 资产信息
     * @return 结果
     */
    @Override
    public int updateAssetInfo(AssetInfo assetInfo) {
        return assetInfoMapper.updateAssetInfo(assetInfo);
    }

    /**
     * 批量删除资产信息
     *
     * @param assetNos 需要删除的资产编号
     * @return 结果
     */
    @Override
    public int deleteAssetInfoByAssetNos(String[] assetNos) {
        return assetInfoMapper.deleteAssetInfoByAssetNos(assetNos);
    }

    /**
     * 删除资产信息信息
     *
     * @param assetNo 资产编号
     * @return 结果
     */
    @Override
    public int deleteAssetInfoByAssetNo(String assetNo) {
        return assetInfoMapper.deleteAssetInfoByAssetNo(assetNo);
    }
}
