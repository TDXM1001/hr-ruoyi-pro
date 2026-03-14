package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetCategoryAttr;

/**
 * 资产分类扩展字段服务接口。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public interface IAssetCategoryAttrService {
    /**
     * 查询扩展字段定义。
     *
     * @param attrId 字段ID
     * @return 字段定义
     */
    public AssetCategoryAttr selectAssetCategoryAttrByAttrId(Long attrId);

    /**
     * 查询扩展字段定义列表。
     *
     * @param assetCategoryAttr 查询条件
     * @return 字段定义集合
     */
    public List<AssetCategoryAttr> selectAssetCategoryAttrList(AssetCategoryAttr assetCategoryAttr);

    /**
     * 按分类查询扩展字段定义。
     *
     * @param categoryId 分类ID
     * @return 字段定义集合
     */
    public List<AssetCategoryAttr> selectAssetCategoryAttrByCategoryId(Long categoryId);

    /**
     * 新增扩展字段定义。
     *
     * @param assetCategoryAttr 字段定义
     * @return 结果
     */
    public int insertAssetCategoryAttr(AssetCategoryAttr assetCategoryAttr);

    /**
     * 修改扩展字段定义。
     *
     * @param assetCategoryAttr 字段定义
     * @return 结果
     */
    public int updateAssetCategoryAttr(AssetCategoryAttr assetCategoryAttr);

    /**
     * 禁用扩展字段定义。
     *
     * @param attrId 字段ID
     * @param updateBy 更新人
     * @return 结果
     */
    public int disableAssetCategoryAttr(Long attrId, String updateBy);

    /**
     * 批量删除扩展字段定义。
     *
     * @param attrIds 字段ID集合
     * @return 结果
     */
    public int deleteAssetCategoryAttrByAttrIds(Long[] attrIds);
}
