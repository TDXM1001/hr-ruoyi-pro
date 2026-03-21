package com.ruoyi.asset.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.asset.domain.AssetInventoryItem;
import com.ruoyi.asset.domain.AssetInventoryTask;
import com.ruoyi.asset.domain.bo.AssetInventoryTaskAssetBo;
import com.ruoyi.asset.domain.bo.AssetInventoryTaskBo;
import com.ruoyi.asset.domain.vo.AssetInventoryRecordVo;
import com.ruoyi.asset.domain.vo.AssetInventoryTaskAssetVo;
import com.ruoyi.asset.domain.vo.AssetInventoryTaskVo;

/**
 * 资产盘点数据层。
 *
 * @author Codex
 */
public interface AssetInventoryMapper
{
    /**
     * 查询盘点任务列表。
     *
     * @param bo 查询参数
     * @return 盘点任务列表
     */
    List<AssetInventoryTaskVo> selectAssetInventoryTaskList(AssetInventoryTaskBo bo);

    /**
     * 查询盘点任务资产明细列表。
     *
     * @param bo 查询参数
     * @return 任务资产明细列表
     */
    List<AssetInventoryTaskAssetVo> selectTaskAssetList(AssetInventoryTaskAssetBo bo);

    /**
     * 查询盘点任务详情。
     *
     * @param taskId 任务ID
     * @return 盘点任务
     */
    AssetInventoryTask selectAssetInventoryTaskById(Long taskId);

    /**
     * 查询盘点任务下某资产是否已登记结果。
     *
     * @param taskId 任务ID
     * @param assetId 资产ID
     * @return 盘点明细
     */
    AssetInventoryItem selectAssetInventoryItem(@Param("taskId") Long taskId, @Param("assetId") Long assetId);

    /**
     * 按明细ID查询盘点结果。
     *
     * @param itemId 明细ID
     * @return 盘点明细
     */
    AssetInventoryItem selectAssetInventoryItemById(Long itemId);

    /**
     * 按资产查询盘点历史记录。
     *
     * @param assetId 资产ID
     * @return 盘点历史记录
     */
    List<AssetInventoryRecordVo> selectAssetInventoryRecordsByAssetId(Long assetId);

    /**
     * 回写巡检结果的整改跟进状态。
     *
     * @param itemId 明细ID
     * @param processStatus 处理状态
     * @param processTime 处理时间
     * @param followUpBizId 关联业务ID
     * @return 结果
     */
    int updateInventoryItemFollowUp(@Param("itemId") Long itemId, @Param("processStatus") String processStatus,
        @Param("processTime") Date processTime, @Param("followUpBizId") Long followUpBizId);

    /**
     * 查询指定前缀下最大盘点任务号。
     *
     * @param taskNoPrefix 任务号前缀
     * @return 最大任务号
     */
    String selectMaxTaskNoByPrefix(String taskNoPrefix);

    /**
     * 新增盘点任务。
     *
     * @param assetInventoryTask 任务对象
     * @return 结果
     */
    int insertAssetInventoryTask(AssetInventoryTask assetInventoryTask);

    /**
     * 新增盘点明细结果。
     *
     * @param assetInventoryItem 明细对象
     * @return 结果
     */
    int insertAssetInventoryItem(AssetInventoryItem assetInventoryItem);

    /**
     * 统计任务下已登记结果数量。
     *
     * @param taskId 任务ID
     * @return 已登记数量
     */
    Long countInventoryItemByTaskId(Long taskId);

    /**
     * 更新任务状态。
     *
     * @param taskId 任务ID
     * @param taskStatus 状态
     * @param completedDate 完成日期
     * @param updateBy 更新人
     * @return 结果
     */
    int updateTaskStatus(@Param("taskId") Long taskId, @Param("taskStatus") String taskStatus,
        @Param("completedDate") Date completedDate, @Param("updateBy") String updateBy);

    /**
     * 统计超期未完成任务数量。
     *
     * @return 超期任务数
     */
    Long countOverdueInventoryTask();
}
