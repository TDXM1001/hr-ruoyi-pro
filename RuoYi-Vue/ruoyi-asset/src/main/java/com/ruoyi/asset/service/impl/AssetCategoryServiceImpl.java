package com.ruoyi.asset.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.asset.mapper.AssetCategoryMapper;
import com.ruoyi.asset.domain.AssetCategory;
import com.ruoyi.asset.service.IAssetCategoryService;

/**
 * 资产分类Service业务层处理
 *
 * @author ruoyi
 * @date 2026-03-11
 */
@Service
public class AssetCategoryServiceImpl implements IAssetCategoryService {
    @Autowired
    private AssetCategoryMapper assetCategoryMapper;

    /**
     * 查询资产分类
     *
     * @param id 资产分类主键
     * @return 资产分类
     */
    @Override
    public AssetCategory selectAssetCategoryById(Long id) {
        return assetCategoryMapper.selectAssetCategoryById(id);
    }

    /**
     * 查询资产分类列表
     *
     * @param assetCategory 资产分类
     * @return 资产分类
     */
    @Override
    public List<AssetCategory> selectAssetCategoryList(AssetCategory assetCategory) {
        return assetCategoryMapper.selectAssetCategoryList(assetCategory);
    }

    /**
     * 新增资产分类
     *
     * @param assetCategory 资产分类
     * @return 结果
     */
    @Override
    public int insertAssetCategory(AssetCategory assetCategory) {
        assetCategory.setCreateTime(DateUtils.getNowDate());
        return assetCategoryMapper.insertAssetCategory(assetCategory);
    }

    /**
     * 修改资产分类
     *
     * @param assetCategory 资产分类
     * @return 结果
     */
    @Override
    public int updateAssetCategory(AssetCategory assetCategory) {
        return assetCategoryMapper.updateAssetCategory(assetCategory);
    }

    /**
     * 批量删除资产分类
     *
     * @param ids 需要删除的资产分类主键
     * @return 结果
     */
    @Override
    public int deleteAssetCategoryByIds(Long[] ids) {
        return assetCategoryMapper.deleteAssetCategoryByIds(ids);
    }

    /**
     * 删除资产分类信息
     *
     * @param id 资产分类主键
     * @return 结果
     */
    @Override
    public int deleteAssetCategoryById(Long id) {
        return assetCategoryMapper.deleteAssetCategoryById(id);
    }
}
