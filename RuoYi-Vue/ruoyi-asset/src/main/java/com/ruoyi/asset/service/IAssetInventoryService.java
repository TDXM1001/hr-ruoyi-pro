package com.ruoyi.asset.service;

import java.util.List;
import com.ruoyi.asset.domain.AssetInventoryTask;
import com.ruoyi.asset.domain.bo.AssetInventoryResultBo;
import com.ruoyi.asset.domain.bo.AssetInventoryTaskBo;
import com.ruoyi.asset.domain.vo.AssetInventoryTaskVo;

/**
 * 资产盘点服务接口。
 *
 * @author Codex
 */
public interface IAssetInventoryService
{
    /**
     * 查询盘点任务列表。
     *
     * @param bo 查询参数
     * @return 任务列表
     */
    List<AssetInventoryTaskVo> selectAssetInventoryTaskList(AssetInventoryTaskBo bo);

    /**
     * 查询盘点任务详情。
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    AssetInventoryTask selectAssetInventoryTaskById(Long taskId);

    /**
     * 创建盘点任务。
     *
     * @param bo 创建参数
     * @param operator 操作人
     * @return 任务ID
     */
    Long createAssetInventoryTask(AssetInventoryTaskBo bo, String operator);

    /**
     * 提交盘点结果。
     *
     * @param bo 盘点结果
     * @param operator 操作人
     */
    void submitResult(AssetInventoryResultBo bo, String operator);
}
