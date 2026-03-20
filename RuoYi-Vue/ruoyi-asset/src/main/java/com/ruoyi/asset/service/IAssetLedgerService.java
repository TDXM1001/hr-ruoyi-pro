package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.bo.AssetLedgerBo;
import com.ruoyi.asset.domain.vo.AssetLedgerLifecycleVo;
import com.ruoyi.asset.domain.vo.AssetLedgerVo;

/**
 * 资产台账服务接口。
 *
 * @author Codex
 */
public interface IAssetLedgerService
{
    /**
     * 查询资产台账列表。
     *
     * @param bo 查询条件
     * @return 台账列表
     */
    public List<AssetLedgerVo> selectAssetLedgerList(AssetLedgerBo bo);

    /**
     * 查询资产台账详情。
     *
     * @param assetId 资产ID
     * @return 台账详情
     */
    public AssetLedgerVo selectAssetLedgerById(Long assetId);

    /**
     * 查询资产生命周期聚合详情。
     *
     * @param assetId 资产ID
     * @return 生命周期聚合详情
     */
    public AssetLedgerLifecycleVo selectAssetLifecycleById(Long assetId);

    /**
     * 获取下一条建议资产编号。
     *
     * <p>固定资产一期先采用“FA-年份-四位流水”规则，
     * 由后端统一生成建议编号，避免前端手填导致口径漂移。</p>
     *
     * @return 建议资产编号
     */
    public String getNextAssetCode();

    /**
     * 新增资产台账。
     *
     * @param bo 台账入参
     * @param operator 操作人
     * @return 新增后的资产ID
     */
    public Long createAsset(AssetLedgerBo bo, String operator);

    /**
     * 修改资产台账。
     *
     * @param bo 台账入参
     * @param operator 操作人
     * @return 结果
     */
    public int updateAsset(AssetLedgerBo bo, String operator);
}
