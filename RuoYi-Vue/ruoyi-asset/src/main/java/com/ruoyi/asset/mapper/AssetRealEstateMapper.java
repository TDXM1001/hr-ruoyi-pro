package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateProfile;
import com.ruoyi.asset.domain.bo.AssetRealEstateBo;
import com.ruoyi.asset.domain.vo.AssetRealEstateVo;

/**
 * 不动产档案数据层。
 *
 * @author Codex
 */
public interface AssetRealEstateMapper
{
    /**
     * 查询不动产档案列表。
     *
     * @param query 查询条件
     * @return 档案列表
     */
    List<AssetRealEstateVo> selectList(AssetRealEstateBo query);

    /**
     * 查询不动产档案详情。
     *
     * @param assetId 资产ID
     * @return 档案详情
     */
    AssetRealEstateVo selectDetailByAssetId(Long assetId);

    /**
     * 按资产ID查询权属扩展档案。
     *
     * @param assetId 资产ID
     * @return 权属档案
     */
    AssetRealEstateProfile selectByAssetId(Long assetId);

    /**
     * 新增权属扩展档案。
     *
     * @param profile 权属档案
     * @return 结果
     */
    int insertProfile(AssetRealEstateProfile profile);

    /**
     * 修改权属扩展档案。
     *
     * @param profile 权属档案
     * @return 结果
     */
    int updateProfile(AssetRealEstateProfile profile);
}
