package com.ruoyi.asset.service.impl;

import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.asset.domain.AssetChangeLog;
import com.ruoyi.asset.domain.AssetInventoryItem;
import com.ruoyi.asset.domain.AssetInventoryTask;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.bo.AssetInventoryTaskAssetBo;
import com.ruoyi.asset.domain.bo.AssetInventoryResultBo;
import com.ruoyi.asset.domain.bo.AssetInventoryTaskBo;
import com.ruoyi.asset.domain.vo.AssetInventoryTaskAssetVo;
import com.ruoyi.asset.domain.vo.AssetInventoryTaskVo;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetInventoryMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.asset.service.AssetStatusMachine;
import com.ruoyi.asset.service.IAssetInventoryService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 资产盘点服务实现。
 *
 * @author Codex
 */
@Service
public class AssetInventoryServiceImpl implements IAssetInventoryService
{
    private static final String INVENTORY_TASK_NO_PREFIX = "IV";

    private static final int INVENTORY_TASK_NO_SERIAL_LENGTH = 4;

    private static final String TASK_STATUS_IN_PROGRESS = "IN_PROGRESS";

    private static final String TASK_STATUS_COMPLETED = "COMPLETED";

    private static final String SCOPE_TYPE_ASSET = "ASSET";

    private static final String FOLLOW_UP_NONE = "NONE";

    private static final String FOLLOW_UP_UPDATE_LEDGER = "UPDATE_LEDGER";

    private static final String FOLLOW_UP_CREATE_DISPOSAL = "CREATE_DISPOSAL";

    private static final String RESULT_TYPE_ALL = "ALL";

    private static final String RESULT_TYPE_PENDING = "PENDING";

    private static final String RESULT_TYPE_REGISTERED = "REGISTERED";

    private static final String RESULT_TYPE_ABNORMAL = "ABNORMAL";

    private static final String PROCESS_STATUS_PENDING = "PENDING";

    private static final String PROCESS_STATUS_PROCESSED = "PROCESSED";

    private static final Set<String> ABNORMAL_RESULTS = Set.of("DAMAGED", "LOSS", "MISSING");

    @Autowired
    private AssetInventoryMapper assetInventoryMapper;

    @Autowired
    private AssetLedgerMapper assetLedgerMapper;

    @Autowired
    private AssetChangeLogMapper assetChangeLogMapper;

    @Autowired
    private AssetStatusMachine assetStatusMachine;

    /**
     * 查询盘点任务列表。
     *
     * @param bo 查询参数
     * @return 任务列表
     */
    @Override
    public List<AssetInventoryTaskVo> selectAssetInventoryTaskList(AssetInventoryTaskBo bo)
    {
        return assetInventoryMapper.selectAssetInventoryTaskList(bo);
    }

    /**
     * 查询盘点任务资产明细。
     *
     * @param bo 查询参数
     * @return 任务资产明细列表
     */
    @Override
    public List<AssetInventoryTaskAssetVo> selectTaskAssetList(AssetInventoryTaskAssetBo bo)
    {
        if (bo == null || bo.getTaskId() == null)
        {
            throw new ServiceException("盘点任务ID不能为空");
        }
        AssetInventoryTask task = assetInventoryMapper.selectAssetInventoryTaskById(bo.getTaskId());
        if (task == null)
        {
            throw new ServiceException("盘点任务不存在");
        }
        bo.setResultType(normalizeResultType(bo.getResultType()));
        return assetInventoryMapper.selectTaskAssetList(bo);
    }

    /**
     * 查询盘点任务详情。
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    @Override
    public AssetInventoryTask selectAssetInventoryTaskById(Long taskId)
    {
        if (taskId == null)
        {
            throw new ServiceException("盘点任务ID不能为空");
        }
        return assetInventoryMapper.selectAssetInventoryTaskById(taskId);
    }

    /**
     * 创建盘点任务并将指定资产置为盘点中。
     *
     * @param bo 创建参数
     * @param operator 操作人
     * @return 任务ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAssetInventoryTask(AssetInventoryTaskBo bo, String operator)
    {
        validateCreateParams(bo);
        List<Long> assetIds = normalizeAssetIds(bo.getAssetIds());
        Date now = DateUtils.getNowDate();

        AssetInventoryTask task = new AssetInventoryTask();
        task.setTaskNo(generateNextTaskNo());
        task.setTaskName(StringUtils.trim(bo.getTaskName()));
        task.setTaskStatus(TASK_STATUS_IN_PROGRESS);
        task.setScopeType(SCOPE_TYPE_ASSET);
        task.setScopeValue(joinScopeValue(assetIds));
        task.setPlannedDate(bo.getPlannedDate());
        task.setCreateBy(operator);
        task.setRemark(bo.getRemark());

        int taskRows = assetInventoryMapper.insertAssetInventoryTask(task);
        if (taskRows <= 0)
        {
            throw new ServiceException("新增盘点任务失败");
        }

        for (Long assetId : assetIds)
        {
            AssetLedger asset = requireAsset(assetId);
            AssetStatus beforeStatus = parseAssetStatus(asset.getAssetStatus());
            if (!AssetStatus.INVENTORYING.equals(beforeStatus)
                && !assetStatusMachine.canTransit(beforeStatus, AssetStatus.INVENTORYING))
            {
                throw new ServiceException("资产状态不允许进入盘点流程");
            }

            if (!AssetStatus.INVENTORYING.equals(beforeStatus))
            {
                int updateRows = assetLedgerMapper.updateStatus(assetId, AssetStatus.INVENTORYING.getCode());
                if (updateRows <= 0)
                {
                    throw new ServiceException("回写资产盘点状态失败");
                }
            }

            assetChangeLogMapper.insertAssetChangeLog(AssetChangeLog.ofInventoryCreate(assetId, task.getTaskId(),
                beforeStatus.getCode(), AssetStatus.INVENTORYING.getCode(), operator));
        }

        return task.getTaskId();
    }

    /**
     * 提交盘点结果并回写台账。
     *
     * @param bo 盘点结果
     * @param operator 操作人
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitResult(AssetInventoryResultBo bo, String operator)
    {
        validateResultParams(bo);

        AssetInventoryTask task = assetInventoryMapper.selectAssetInventoryTaskById(bo.getTaskId());
        if (task == null)
        {
            throw new ServiceException("盘点任务不存在");
        }
        if (assetInventoryMapper.selectAssetInventoryItem(bo.getTaskId(), bo.getAssetId()) != null)
        {
            throw new ServiceException("当前资产已提交盘点结果");
        }

        AssetLedger asset = requireAsset(bo.getAssetId());
        AssetStatus beforeStatus = parseAssetStatus(asset.getAssetStatus());

        String inventoryResult = normalizeInventoryResult(bo.getInventoryResult());
        String followUpAction = normalizeFollowUpAction(bo.getFollowUpAction());
        validateFollowUpRule(inventoryResult, followUpAction);

        AssetStatus targetStatus = resolveTargetStatus(inventoryResult, bo.getConfirmedUse());
        if (!targetStatus.equals(beforeStatus) && !assetStatusMachine.canTransit(beforeStatus, targetStatus))
        {
            throw new ServiceException("资产状态不允许按当前盘点结果流转");
        }

        Date checkedTime = bo.getCheckedTime() != null ? bo.getCheckedTime() : DateUtils.getNowDate();
        AssetInventoryItem item = buildInventoryItem(bo, operator, checkedTime, inventoryResult, followUpAction);
        int itemRows = assetInventoryMapper.insertAssetInventoryItem(item);
        if (itemRows <= 0)
        {
            throw new ServiceException("新增盘点结果失败");
        }

        if (ABNORMAL_RESULTS.contains(inventoryResult))
        {
            int statusRows = assetLedgerMapper.updateStatus(bo.getAssetId(), targetStatus.getCode());
            if (statusRows <= 0)
            {
                throw new ServiceException("回写资产状态失败");
            }
        }

        AssetLedger updateEntity = buildInventoryLedgerUpdate(bo, operator, checkedTime, targetStatus, inventoryResult);
        int updateRows = assetLedgerMapper.updateInventoryResult(updateEntity);
        if (updateRows <= 0)
        {
            throw new ServiceException("回写盘点台账失败");
        }

        updateTaskStatus(task, operator, checkedTime);
        assetChangeLogMapper.insertAssetChangeLog(AssetChangeLog.ofInventoryResult(bo.getAssetId(), bo.getTaskId(),
            beforeStatus.getCode(), targetStatus.getCode(), operator,
            buildInventoryChangeDesc(task.getTaskNo(), inventoryResult, followUpAction)));
    }

    /**
     * 校验创建盘点任务参数。
     *
     * @param bo 创建参数
     */
    private void validateCreateParams(AssetInventoryTaskBo bo)
    {
        if (bo == null)
        {
            throw new ServiceException("盘点任务参数不能为空");
        }
        if (StringUtils.isBlank(bo.getTaskName()))
        {
            throw new ServiceException("盘点任务名称不能为空");
        }
        if (bo.getPlannedDate() == null)
        {
            throw new ServiceException("计划盘点日期不能为空");
        }
        if (bo.getAssetIds() == null || bo.getAssetIds().isEmpty())
        {
            throw new ServiceException("盘点资产不能为空");
        }
    }

    /**
     * 规范化资产ID列表并校验重复与空值。
     *
     * @param sourceAssetIds 原始资产ID列表
     * @return 规范化后的资产ID列表
     */
    private List<Long> normalizeAssetIds(List<Long> sourceAssetIds)
    {
        Set<Long> uniqueIds = new LinkedHashSet<Long>();
        for (Long assetId : sourceAssetIds)
        {
            if (assetId == null)
            {
                throw new ServiceException("盘点资产ID不能为空");
            }
            if (!uniqueIds.add(assetId))
            {
                throw new ServiceException("同一盘点任务不允许重复选择同一资产");
            }
        }
        return new ArrayList<Long>(uniqueIds);
    }

    /**
     * 资产ID列表转换为范围值字符串。
     *
     * @param assetIds 资产ID列表
     * @return 范围值
     */
    private String joinScopeValue(List<Long> assetIds)
    {
        StringJoiner joiner = new StringJoiner(",");
        for (Long assetId : assetIds)
        {
            joiner.add(String.valueOf(assetId));
        }
        return joiner.toString();
    }

    /**
     * 校验盘点结果入参。
     *
     * @param bo 盘点结果参数
     */
    private void validateResultParams(AssetInventoryResultBo bo)
    {
        if (bo == null)
        {
            throw new ServiceException("盘点结果参数不能为空");
        }
        if (bo.getTaskId() == null)
        {
            throw new ServiceException("盘点任务ID不能为空");
        }
        if (bo.getAssetId() == null)
        {
            throw new ServiceException("资产ID不能为空");
        }
        if (StringUtils.isBlank(bo.getInventoryResult()))
        {
            throw new ServiceException("盘点结果不能为空");
        }
    }

    /**
     * 规范化盘点结果编码。
     *
     * @param inventoryResult 原始结果
     * @return 规范化结果
     */
    private String normalizeInventoryResult(String inventoryResult)
    {
        String normalizedResult = StringUtils.upperCase(StringUtils.trimToEmpty(inventoryResult));
        if (StringUtils.isBlank(normalizedResult))
        {
            throw new ServiceException("盘点结果不能为空");
        }
        return normalizedResult;
    }

    /**
     * 规范化后续动作编码。
     *
     * @param followUpAction 原始后续动作
     * @return 规范化后续动作
     */
    private String normalizeFollowUpAction(String followUpAction)
    {
        String normalizedAction = StringUtils.upperCase(StringUtils.defaultIfBlank(StringUtils.trim(followUpAction), FOLLOW_UP_NONE));
        if (!FOLLOW_UP_NONE.equals(normalizedAction)
            && !FOLLOW_UP_UPDATE_LEDGER.equals(normalizedAction)
            && !FOLLOW_UP_CREATE_DISPOSAL.equals(normalizedAction))
        {
            throw new ServiceException("盘点后续动作不合法");
        }
        return normalizedAction;
    }

    /**
     * 规范化任务资产结果筛选类型。
     *
     * @param resultType 原始筛选类型
     * @return 规范化筛选类型
     */
    private String normalizeResultType(String resultType)
    {
        String normalizedType = StringUtils.upperCase(StringUtils.defaultIfBlank(StringUtils.trim(resultType), RESULT_TYPE_ALL));
        if (!RESULT_TYPE_ALL.equals(normalizedType)
            && !RESULT_TYPE_PENDING.equals(normalizedType)
            && !RESULT_TYPE_REGISTERED.equals(normalizedType)
            && !RESULT_TYPE_ABNORMAL.equals(normalizedType))
        {
            throw new ServiceException("盘点结果筛选类型不合法");
        }
        return normalizedType;
    }

    /**
     * 校验异常盘点的后续动作。
     *
     * @param inventoryResult 盘点结果
     * @param followUpAction 后续动作
     */
    private void validateFollowUpRule(String inventoryResult, String followUpAction)
    {
        if (ABNORMAL_RESULTS.contains(inventoryResult) && FOLLOW_UP_NONE.equals(followUpAction))
        {
            throw new ServiceException("盘点异常结果必须指定后续动作");
        }
    }

    /**
     * 解析盘点后目标状态。
     *
     * @param inventoryResult 盘点结果
     * @param confirmedUse 是否在用
     * @return 目标状态
     */
    private AssetStatus resolveTargetStatus(String inventoryResult, Boolean confirmedUse)
    {
        if (ABNORMAL_RESULTS.contains(inventoryResult))
        {
            return AssetStatus.PENDING_DISPOSAL;
        }
        return Boolean.FALSE.equals(confirmedUse) ? AssetStatus.IDLE : AssetStatus.IN_USE;
    }

    /**
     * 构建盘点明细实体。
     *
     * @param bo 盘点结果参数
     * @param operator 操作人
     * @param checkedTime 盘点时间
     * @param inventoryResult 盘点结果
     * @param followUpAction 后续动作
     * @return 盘点明细实体
     */
    private AssetInventoryItem buildInventoryItem(AssetInventoryResultBo bo, String operator, Date checkedTime,
        String inventoryResult, String followUpAction)
    {
        AssetInventoryItem item = new AssetInventoryItem();
        item.setTaskId(bo.getTaskId());
        item.setAssetId(bo.getAssetId());
        item.setInventoryResult(inventoryResult);
        item.setActualLocationName(StringUtils.trimToEmpty(bo.getActualLocationName()));
        item.setActualUseDeptId(bo.getActualUseDeptId());
        item.setActualResponsibleUserId(bo.getActualResponsibleUserId());
        item.setFollowUpAction(followUpAction);
        item.setProcessStatus(resolveProcessStatus(inventoryResult, followUpAction));
        if (PROCESS_STATUS_PROCESSED.equals(item.getProcessStatus()))
        {
            item.setProcessTime(checkedTime);
        }
        item.setCheckedBy(operator);
        item.setCheckedTime(checkedTime);
        item.setResultDesc(bo.getResultDesc());
        item.setRemark(bo.getRemark());
        item.setCreateBy(operator);
        return item;
    }

    /**
     * 解析盘点结果处理状态。
     *
     * @param inventoryResult 盘点结果
     * @param followUpAction 后续动作
     * @return 处理状态
     */
    private String resolveProcessStatus(String inventoryResult, String followUpAction)
    {
        if (ABNORMAL_RESULTS.contains(inventoryResult) && FOLLOW_UP_CREATE_DISPOSAL.equals(followUpAction))
        {
            return PROCESS_STATUS_PENDING;
        }
        return PROCESS_STATUS_PROCESSED;
    }

    /**
     * 构建盘点回写台账对象。
     *
     * @param bo 盘点结果参数
     * @param operator 操作人
     * @param checkedTime 盘点时间
     * @param targetStatus 目标状态
     * @param inventoryResult 盘点结果
     * @return 台账更新对象
     */
    private AssetLedger buildInventoryLedgerUpdate(AssetInventoryResultBo bo, String operator, Date checkedTime,
        AssetStatus targetStatus, String inventoryResult)
    {
        AssetLedger updateEntity = new AssetLedger();
        updateEntity.setAssetId(bo.getAssetId());
        updateEntity.setLastInventoryDate(checkedTime);
        updateEntity.setUseDeptId(bo.getActualUseDeptId());
        updateEntity.setResponsibleUserId(bo.getActualResponsibleUserId());
        updateEntity.setLocationName(StringUtils.trimToEmpty(bo.getActualLocationName()));
        updateEntity.setUpdateBy(operator);
        // 异常盘点已通过 updateStatus 回写状态，此处仅回写盘点时间与修正字段。
        if (!ABNORMAL_RESULTS.contains(inventoryResult))
        {
            updateEntity.setAssetStatus(targetStatus.getCode());
        }
        return updateEntity;
    }

    /**
     * 根据任务范围和值回写任务状态。
     *
     * @param task 盘点任务
     * @param operator 操作人
     * @param checkedTime 盘点时间
     */
    private void updateTaskStatus(AssetInventoryTask task, String operator, Date checkedTime)
    {
        int expectedCount = parseExpectedAssetCount(task.getScopeValue());
        if (expectedCount <= 0)
        {
            return;
        }

        Long actualCount = assetInventoryMapper.countInventoryItemByTaskId(task.getTaskId());
        if (actualCount != null && actualCount.longValue() >= expectedCount)
        {
            assetInventoryMapper.updateTaskStatus(task.getTaskId(), TASK_STATUS_COMPLETED, checkedTime, operator);
            return;
        }
        assetInventoryMapper.updateTaskStatus(task.getTaskId(), TASK_STATUS_IN_PROGRESS, null, operator);
    }

    /**
     * 解析范围值中的资产数量。
     *
     * @param scopeValue 范围值
     * @return 资产数量
     */
    private int parseExpectedAssetCount(String scopeValue)
    {
        if (StringUtils.isBlank(scopeValue))
        {
            return 0;
        }

        String[] segments = StringUtils.split(scopeValue, ',');
        if (segments == null || segments.length == 0)
        {
            return 0;
        }

        Set<String> uniqueIds = new LinkedHashSet<String>();
        for (String segment : segments)
        {
            if (StringUtils.isNotBlank(segment))
            {
                uniqueIds.add(StringUtils.trim(segment));
            }
        }
        return uniqueIds.size();
    }

    /**
     * 构建盘点变更描述。
     *
     * @param taskNo 任务编号
     * @param inventoryResult 盘点结果
     * @param followUpAction 后续动作
     * @return 变更描述
     */
    private String buildInventoryChangeDesc(String taskNo, String inventoryResult, String followUpAction)
    {
        return "盘点结果：" + inventoryResult + "（任务号：" + StringUtils.defaultString(taskNo) + "，后续动作："
            + followUpAction + "）";
    }

    /**
     * 查询资产，不存在则抛错。
     *
     * @param assetId 资产ID
     * @return 资产主档
     */
    private AssetLedger requireAsset(Long assetId)
    {
        AssetLedger asset = assetLedgerMapper.selectAssetById(assetId);
        if (asset == null)
        {
            throw new ServiceException("资产不存在，资产ID：" + assetId);
        }
        return asset;
    }

    /**
     * 解析资产状态编码。
     *
     * @param assetStatus 资产状态编码
     * @return 资产状态枚举
     */
    private AssetStatus parseAssetStatus(String assetStatus)
    {
        String normalizedStatus = StringUtils.upperCase(StringUtils.defaultIfBlank(assetStatus, AssetStatus.DRAFT.getCode()));
        for (AssetStatus status : AssetStatus.values())
        {
            if (status.getCode().equals(normalizedStatus))
            {
                return status;
            }
        }
        throw new ServiceException("资产状态非法：" + assetStatus);
    }

    /**
     * 生成下一条盘点任务号。
     *
     * @return 盘点任务号
     */
    private String generateNextTaskNo()
    {
        String currentYear = String.valueOf(Year.now().getValue());
        String taskNoPrefix = INVENTORY_TASK_NO_PREFIX + "-" + currentYear + "-";
        String currentMaxNo = assetInventoryMapper.selectMaxTaskNoByPrefix(taskNoPrefix);
        int nextSerial = parseNextSerial(currentMaxNo, taskNoPrefix);
        return taskNoPrefix + String.format("%0" + INVENTORY_TASK_NO_SERIAL_LENGTH + "d", nextSerial);
    }

    /**
     * 解析下一条流水号。
     *
     * @param currentMaxNo 当前最大编号
     * @param noPrefix 编号前缀
     * @return 流水号
     */
    private int parseNextSerial(String currentMaxNo, String noPrefix)
    {
        if (StringUtils.isBlank(currentMaxNo) || !currentMaxNo.startsWith(noPrefix))
        {
            return 1;
        }
        String serialPart = currentMaxNo.substring(noPrefix.length());
        if (!StringUtils.isNumeric(serialPart))
        {
            return 1;
        }
        return Integer.parseInt(serialPart) + 1;
    }
}
