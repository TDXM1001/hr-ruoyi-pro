package com.ruoyi.asset.mapper;

import java.util.List;
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
    public AssetLedger selectAssetById(Long assetId);

    /**
     * 根据资产编码查询台账主档。
     *
     * @param assetCode 资产编码
     * @return 台账主档
     */
    public AssetLedger selectByAssetCode(String assetCode);

    /**
     * 按资产编码前缀查询当前最大编码。
     *
     * @param assetCodePrefix 编码前缀
     * @return 当前最大编码
     */
    public String selectMaxAssetCodeByPrefix(String assetCodePrefix);

    /**
     * 查询台账列表。
     *
     * @param bo 查询条件
     * @return 台账列表
     */
    public List<AssetLedgerVo> selectAssetLedgerList(AssetLedgerBo bo);

    /**
     * 查询台账详情。
     *
     * @param assetId 资产ID
     * @return 台账详情
     */
    public AssetLedgerVo selectAssetLedgerById(Long assetId);

    /**
     * 新增资产台账。
     *
     * @param assetLedger 台账主档
     * @return 结果
     */
    public int insertAsset(AssetLedger assetLedger);

    /**
     * 修改资产台账。
     *
     * @param assetLedger 台账主档
     * @return 结果
     */
    public int updateAsset(AssetLedger assetLedger);

    /**
     * 回写资产交接后的使用信息与状态。
     *
     * @param assetLedger 台账主档
     * @return 结果
     */
    public int updateAssetUsageInfo(AssetLedger assetLedger);
}
