package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetCategoryAttr;

/**
 * 分类扩展字段Mapper接口
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public interface AssetCategoryAttrMapper {
    public AssetCategoryAttr selectAssetCategoryAttrByAttrId(Long attrId);

    public List<AssetCategoryAttr> selectAssetCategoryAttrList(AssetCategoryAttr assetCategoryAttr);

    public List<AssetCategoryAttr> selectAssetCategoryAttrByCategoryId(Long categoryId);

    public int insertAssetCategoryAttr(AssetCategoryAttr assetCategoryAttr);

    public int updateAssetCategoryAttr(AssetCategoryAttr assetCategoryAttr);

    public int deleteAssetCategoryAttrByAttrId(Long attrId);

    public int deleteAssetCategoryAttrByAttrIds(Long[] attrIds);
}
