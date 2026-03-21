package com.ruoyi.asset.service.impl;

import java.time.Year;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.asset.domain.AssetChangeLog;
import com.ruoyi.asset.domain.AssetInventoryItem;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.AssetRectificationOrder;
import com.ruoyi.asset.domain.bo.AssetRectificationBo;
import com.ruoyi.asset.domain.bo.AssetRectificationCompleteBo;
import com.ruoyi.asset.domain.vo.AssetRectificationVo;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetInventoryMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.asset.mapper.AssetRectificationMapper;
import com.ruoyi.asset.service.IAssetRectificationService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 资产整改服务实现。
 *
 * <p>当前阶段将整改登记与整改完成拆成两个明确动作：
 * 普通编辑只维护责任、期限、问题描述等基础信息；
 * “完成整改”通过独立命令单独收口，避免状态流转和基础维护混在同一个表单里。</p>
 *
 * @author Codex
 */
@Service
public class AssetRectificationServiceImpl implements IAssetRectificationService
{
    private static final String RECTIFICATION_NO_PREFIX = "RC";

    private static final int RECTIFICATION_NO_SERIAL_LENGTH = 4;

    private static final String RECTIFICATION_STATUS_PENDING = "PENDING";

    private static final String RECTIFICATION_STATUS_COMPLETED = "COMPLETED";

    private static final String PROCESS_STATUS_PENDING = "PENDING";

    private static final String PROCESS_STATUS_PROCESSED = "PROCESSED";

    @Autowired
    private AssetRectificationMapper assetRectificationMapper;

    @Autowired
    private AssetInventoryMapper assetInventoryMapper;

    @Autowired
    private AssetLedgerMapper assetLedgerMapper;

    @Autowired
    private AssetChangeLogMapper assetChangeLogMapper;

    @Override
    public List<AssetRectificationVo> selectAssetRectificationListByAssetId(Long assetId)
    {
        if (assetId == null)
        {
            throw new ServiceException("资产ID不能为空");
        }
        return assetRectificationMapper.selectAssetRectificationListByAssetId(assetId);
    }

    @Override
    public AssetRectificationVo selectAssetRectificationById(Long rectificationId)
    {
        if (rectificationId == null)
        {
            throw new ServiceException("整改单ID不能为空");
        }
        AssetRectificationVo detail = assetRectificationMapper.selectAssetRectificationById(rectificationId);
        if (detail == null)
        {
            throw new ServiceException("整改单不存在");
        }
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAssetRectification(AssetRectificationBo bo, String operator)
    {
        validateBo(bo);
        AssetInventoryItem inventoryItem = requireInventoryItem(bo.getInventoryItemId());
        validateAssetOwnership(bo.getAssetId(), bo.getTaskId(), inventoryItem);
        if (assetRectificationMapper.selectByInventoryItemId(bo.getInventoryItemId()) != null)
        {
            throw new ServiceException("当前巡检结果已发起整改单");
        }
        requireAsset(bo.getAssetId());

        AssetRectificationOrder order = buildOrderEntity(bo, null);
        order.setRectificationNo(generateNextRectificationNo());
        order.setRectificationStatus(resolveRectificationStatus(bo.getRectificationStatus()));
        order.setCreateBy(operator);

        int rows = assetRectificationMapper.insertAssetRectification(order);
        if (rows <= 0)
        {
            throw new ServiceException("新增整改单失败");
        }

        syncInventoryFollowUp(order.getInventoryItemId(), order.getRectificationStatus(), order.getRectificationId());
        assetChangeLogMapper.insertAssetChangeLog(buildChangeLog(bo.getAssetId(), operator,
            "发起整改单：" + order.getRectificationNo() + "，问题类型：" + order.getIssueType()));
        return order.getRectificationId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAssetRectification(AssetRectificationBo bo, String operator)
    {
        validateBo(bo);
        if (bo.getRectificationId() == null)
        {
            throw new ServiceException("整改单ID不能为空");
        }

        AssetRectificationVo current = selectAssetRectificationById(bo.getRectificationId());
        if (!bo.getAssetId().equals(current.getAssetId()))
        {
            throw new ServiceException("整改单与资产不匹配");
        }

        AssetInventoryItem inventoryItem = requireInventoryItem(bo.getInventoryItemId());
        validateAssetOwnership(bo.getAssetId(), bo.getTaskId(), inventoryItem);

        AssetRectificationOrder order = buildOrderEntity(bo, current);
        order.setRectificationId(bo.getRectificationId());
        order.setRectificationNo(current.getRectificationNo());
        order.setUpdateBy(operator);

        int rows = assetRectificationMapper.updateAssetRectification(order);
        if (rows <= 0)
        {
            throw new ServiceException("更新整改单失败");
        }

        syncInventoryFollowUp(order.getInventoryItemId(), order.getRectificationStatus(), order.getRectificationId());
        assetChangeLogMapper.insertAssetChangeLog(buildChangeLog(bo.getAssetId(), operator,
            "更新整改单：" + current.getRectificationNo() + "，状态：" + order.getRectificationStatus()));
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int completeAssetRectification(Long assetId, Long rectificationId, AssetRectificationCompleteBo bo,
        String operator)
    {
        validateCompleteBo(assetId, rectificationId, bo);
        AssetRectificationVo current = selectAssetRectificationById(rectificationId);
        if (!assetId.equals(current.getAssetId()))
        {
            throw new ServiceException("整改单与资产不匹配");
        }
        if (RECTIFICATION_STATUS_COMPLETED.equals(StringUtils.upperCase(current.getRectificationStatus())))
        {
            throw new ServiceException("整改单已完成，请勿重复提交");
        }

        AssetRectificationOrder order = buildCompletedOrder(current, bo, operator);
        int rows = assetRectificationMapper.updateAssetRectification(order);
        if (rows <= 0)
        {
            throw new ServiceException("完成整改失败");
        }

        syncInventoryFollowUp(order.getInventoryItemId(), order.getRectificationStatus(), order.getRectificationId());
        assetChangeLogMapper.insertAssetChangeLog(buildChangeLog(assetId, operator,
            "完成整改单：" + current.getRectificationNo() + "，完成说明：" + order.getCompletionDesc()));
        return rows;
    }

    private void validateBo(AssetRectificationBo bo)
    {
        if (bo == null)
        {
            throw new ServiceException("整改参数不能为空");
        }
        if (bo.getAssetId() == null)
        {
            throw new ServiceException("资产ID不能为空");
        }
        if (bo.getTaskId() == null)
        {
            throw new ServiceException("巡检任务ID不能为空");
        }
        if (bo.getInventoryItemId() == null)
        {
            throw new ServiceException("巡检结果明细ID不能为空");
        }
        if (StringUtils.isBlank(bo.getIssueType()))
        {
            throw new ServiceException("问题类型不能为空");
        }
        if (StringUtils.isBlank(bo.getIssueDesc()))
        {
            throw new ServiceException("问题描述不能为空");
        }
        if (bo.getResponsibleDeptId() == null)
        {
            throw new ServiceException("责任部门不能为空");
        }
        if (bo.getResponsibleUserId() == null)
        {
            throw new ServiceException("责任人不能为空");
        }
        if (bo.getDeadlineDate() == null)
        {
            throw new ServiceException("整改期限不能为空");
        }
    }

    private void validateCompleteBo(Long assetId, Long rectificationId, AssetRectificationCompleteBo bo)
    {
        if (assetId == null)
        {
            throw new ServiceException("资产ID不能为空");
        }
        if (rectificationId == null)
        {
            throw new ServiceException("整改单ID不能为空");
        }
        if (bo == null)
        {
            throw new ServiceException("整改完成参数不能为空");
        }
        if (StringUtils.isBlank(bo.getCompletionDesc()))
        {
            throw new ServiceException("完成说明不能为空");
        }
    }

    private AssetInventoryItem requireInventoryItem(Long inventoryItemId)
    {
        AssetInventoryItem item = assetInventoryMapper.selectAssetInventoryItemById(inventoryItemId);
        if (item == null)
        {
            throw new ServiceException("巡检结果明细不存在");
        }
        return item;
    }

    private void validateAssetOwnership(Long assetId, Long taskId, AssetInventoryItem inventoryItem)
    {
        if (!assetId.equals(inventoryItem.getAssetId()))
        {
            throw new ServiceException("整改资产与巡检结果不匹配");
        }
        if (!taskId.equals(inventoryItem.getTaskId()))
        {
            throw new ServiceException("整改任务与巡检结果不匹配");
        }
    }

    private void requireAsset(Long assetId)
    {
        AssetLedger asset = assetLedgerMapper.selectAssetById(assetId);
        if (asset == null)
        {
            throw new ServiceException("资产不存在");
        }
    }

    private AssetRectificationOrder buildOrderEntity(AssetRectificationBo bo, AssetRectificationVo current)
    {
        String status = resolveRectificationStatus(bo.getRectificationStatus());
        Date now = DateUtils.getNowDate();

        AssetRectificationOrder order = new AssetRectificationOrder();
        order.setAssetId(bo.getAssetId());
        order.setTaskId(bo.getTaskId());
        order.setInventoryItemId(bo.getInventoryItemId());
        order.setRectificationStatus(status);
        order.setIssueType(StringUtils.trim(bo.getIssueType()));
        order.setIssueDesc(StringUtils.trim(bo.getIssueDesc()));
        order.setResponsibleDeptId(bo.getResponsibleDeptId());
        order.setResponsibleUserId(bo.getResponsibleUserId());
        order.setDeadlineDate(bo.getDeadlineDate());
        order.setCompletedTime(RECTIFICATION_STATUS_COMPLETED.equals(status)
            ? now
            : current == null ? null : current.getCompletedTime());
        order.setCompletionDesc(current == null ? null : current.getCompletionDesc());
        order.setAcceptanceRemark(current == null ? null : current.getAcceptanceRemark());
        order.setRemark(bo.getRemark());
        return order;
    }

    private AssetRectificationOrder buildCompletedOrder(AssetRectificationVo current, AssetRectificationCompleteBo bo,
        String operator)
    {
        AssetRectificationOrder order = new AssetRectificationOrder();
        order.setRectificationId(current.getRectificationId());
        order.setRectificationNo(current.getRectificationNo());
        order.setAssetId(current.getAssetId());
        order.setTaskId(current.getTaskId());
        order.setInventoryItemId(current.getInventoryItemId());
        order.setRectificationStatus(RECTIFICATION_STATUS_COMPLETED);
        order.setIssueType(current.getIssueType());
        order.setIssueDesc(current.getIssueDesc());
        order.setResponsibleDeptId(current.getResponsibleDeptId());
        order.setResponsibleUserId(current.getResponsibleUserId());
        order.setDeadlineDate(current.getDeadlineDate());
        order.setCompletedTime(DateUtils.getNowDate());
        order.setCompletionDesc(StringUtils.trim(bo.getCompletionDesc()));
        order.setAcceptanceRemark(normalizeOptionalText(bo.getAcceptanceRemark()));
        order.setRemark(current.getRemark());
        order.setUpdateBy(operator);
        return order;
    }

    private String resolveRectificationStatus(String sourceStatus)
    {
        String status = StringUtils.upperCase(StringUtils.defaultIfBlank(StringUtils.trim(sourceStatus), RECTIFICATION_STATUS_PENDING));
        if (!RECTIFICATION_STATUS_PENDING.equals(status) && !RECTIFICATION_STATUS_COMPLETED.equals(status))
        {
            throw new ServiceException("整改状态不合法");
        }
        return status;
    }

    private void syncInventoryFollowUp(Long inventoryItemId, String rectificationStatus, Long rectificationId)
    {
        String processStatus = RECTIFICATION_STATUS_COMPLETED.equals(rectificationStatus)
            ? PROCESS_STATUS_PROCESSED
            : PROCESS_STATUS_PENDING;
        Date processTime = RECTIFICATION_STATUS_COMPLETED.equals(rectificationStatus) ? DateUtils.getNowDate() : null;
        int rows = assetInventoryMapper.updateInventoryItemFollowUp(inventoryItemId, processStatus, processTime,
            rectificationId);
        if (rows <= 0)
        {
            throw new ServiceException("回写巡检结果整改状态失败");
        }
    }

    private AssetChangeLog buildChangeLog(Long assetId, String operator, String changeDesc)
    {
        AssetChangeLog changeLog = new AssetChangeLog();
        changeLog.setAssetId(assetId);
        changeLog.setBizType("LEDGER_UPDATE");
        changeLog.setOperateBy(operator);
        changeLog.setOperateTime(DateUtils.getNowDate());
        changeLog.setChangeDesc(changeDesc);
        return changeLog;
    }

    private String normalizeOptionalText(String source)
    {
        String value = StringUtils.trim(source);
        return StringUtils.isBlank(value) ? null : value;
    }

    private String generateNextRectificationNo()
    {
        String currentYear = String.valueOf(Year.now().getValue());
        String prefix = RECTIFICATION_NO_PREFIX + "-" + currentYear + "-";
        String currentMaxNo = assetRectificationMapper.selectMaxRectificationNoByPrefix(prefix);
        int nextSerial = parseNextSerial(currentMaxNo, prefix);
        return prefix + String.format("%0" + RECTIFICATION_NO_SERIAL_LENGTH + "d", nextSerial);
    }

    private int parseNextSerial(String currentMaxNo, String prefix)
    {
        if (StringUtils.isBlank(currentMaxNo) || !currentMaxNo.startsWith(prefix))
        {
            return 1;
        }
        String serialPart = currentMaxNo.substring(prefix.length());
        if (!StringUtils.isNumeric(serialPart))
        {
            return 1;
        }
        return Integer.parseInt(serialPart) + 1;
    }
}
