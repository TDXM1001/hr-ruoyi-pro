package com.ruoyi.asset.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.bo.AssetLedgerBo;
import com.ruoyi.asset.domain.vo.AssetLedgerVo;

/**
 * 资产台账数据层。
 *
 * @author Codex
 */
public interface AssetLedgerMapper
{
    /**
     * 查询台账主档。
     *
     * @param assetId 资产ID
     * @return 台账主档
     */
    AssetLedger selectAssetById(Long assetId);

    /**
     * 根据资产编码查询台账主档。
     *
     * @param assetCode 资产编码
     * @return 台账主档
     */
    AssetLedger selectByAssetCode(String assetCode);

    /**
     * 按资产编码前缀查询当前最大编码。
     *
     * @param assetCodePrefix 编码前缀
     * @return 当前最大编码
     */
    String selectMaxAssetCodeByPrefix(String assetCodePrefix);

    /**
     * 查询台账列表。
     *
     * @param bo 查询条件
     * @return 台账列表
     */
    List<AssetLedgerVo> selectAssetLedgerList(AssetLedgerBo bo);

    /**
     * 查询台账详情。
     *
     * @param assetId 资产ID
     * @return 台账详情
     */
    AssetLedgerVo selectAssetLedgerById(Long assetId);

    /**
     * 新增资产台账。
     *
     * @param assetLedger 台账主档
     * @return 结果
     */
    int insertAsset(AssetLedger assetLedger);

    /**
     * 修改资产台账。
     *
     * @param assetLedger 台账主档
     * @return 结果
     */
    int updateAsset(AssetLedger assetLedger);

    /**
     * 回写资产交接后的使用信息与状态。
     *
     * @param assetLedger 台账主档
     * @return 结果
     */
    int updateAssetUsageInfo(AssetLedger assetLedger);

    /**
     * 仅更新资产状态。
     *
     * @param assetId 资产ID
     * @param assetStatus 资产状态
     * @return 结果
     */
    int updateStatus(@Param("assetId") Long assetId, @Param("assetStatus") String assetStatus);

    /**
     * 回写盘点后的台账信息。
     *
     * @param assetLedger 台账对象
     * @return 结果
     */
    int updateInventoryResult(AssetLedger assetLedger);

    /**
     * 统计资产总量。
     *
     * @return 资产总量
     */
    Long countAssetTotal();

    /**
     * 按状态统计资产数量。
     *
     * @param assetStatus 资产状态
     * @return 资产数量
     */
    Long countAssetByStatus(String assetStatus);
}
