package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateProfile;
import com.ruoyi.asset.domain.bo.AssetRealEstateOccupancyBo;
import com.ruoyi.asset.domain.bo.AssetRealEstateOccupancyReleaseBo;
import com.ruoyi.asset.domain.bo.AssetRealEstateBo;
import com.ruoyi.asset.domain.vo.AssetLedgerLifecycleVo;
import com.ruoyi.asset.domain.vo.AssetRealEstateOccupancyVo;
import com.ruoyi.asset.domain.vo.AssetRealEstateVo;

/**
 * 不动产档案服务接口。
 *
 * @author Codex
 */
public interface IAssetRealEstateService
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
     * @return 权属扩展档案
     */
    AssetRealEstateProfile selectByAssetId(Long assetId);

    /**
     * 查询不动产生命周期详情。
     *
     * @param assetId 资产ID
     * @return 生命周期详情
     */
    AssetLedgerLifecycleVo selectLifecycleByAssetId(Long assetId);

    List<AssetRealEstateOccupancyVo> selectOccupancyListByAssetId(Long assetId);

    /**
     * 获取下一条建议资产编码。
     *
     * @return 建议资产编码
     */
    String getNextAssetCode();

    /**
     * 新增不动产档案。
     *
     * @param bo 建档入参
     * @param operator 操作人
     * @return 新增后的资产ID
     */
    Long createAsset(AssetRealEstateBo bo, String operator);

    /**
     * 修改不动产档案。
     *
     * @param bo 编辑入参
     * @param operator 操作人
     * @return 修改结果
     */
    int updateAsset(AssetRealEstateBo bo, String operator);

    Long createOccupancy(Long assetId, AssetRealEstateOccupancyBo bo, String operator);

    Long changeOccupancy(Long assetId, Long occupancyId, AssetRealEstateOccupancyBo bo, String operator);

    int releaseOccupancy(Long assetId, Long occupancyId, AssetRealEstateOccupancyReleaseBo bo, String operator);
}
