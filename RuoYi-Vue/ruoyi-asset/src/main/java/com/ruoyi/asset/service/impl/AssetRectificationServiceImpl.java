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
 * 璧勪骇鏁存敼鏈嶅姟瀹炵幇銆? *
 * <p>褰撳墠闃舵灏嗘暣鏀圭櫥璁颁笌鏁存敼瀹屾垚鎷嗘垚涓や釜鏄庣‘鍔ㄤ綔锛? * 鏅€氱紪杈戝彧缁存姢璐ｄ换銆佹湡闄愩€侀棶棰樻弿杩扮瓑鍩虹淇℃伅锛? * 鈥滃畬鎴愭暣鏀光€濋€氳繃鐙珛鍛戒护鍗曠嫭鏀跺彛锛岄伩鍏嶇姸鎬佹祦杞拰鍩虹缁存姢娣峰湪鍚屼竴涓〃鍗曢噷銆?/p>
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
            throw new ServiceException("\u8d44\u4ea7ID\u4e0d\u80fd\u4e3a\u7a7a");
        }
        return assetRectificationMapper.selectAssetRectificationListByAssetId(assetId);
    }

    @Override
    public AssetRectificationVo selectAssetRectificationById(Long rectificationId)
    {
        if (rectificationId == null)
        {
            throw new ServiceException("\u6574\u6539\u5355ID\u4e0d\u80fd\u4e3a\u7a7a");
        }
        AssetRectificationVo detail = assetRectificationMapper.selectAssetRectificationById(rectificationId);
        if (detail == null)
        {
            throw new ServiceException("\u6574\u6539\u5355\u4e0d\u5b58\u5728");
        }
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAssetRectification(AssetRectificationBo bo, String operator)
    {
        validateBo(bo);
        ensureRegisterStageStatus(bo.getRectificationStatus());
        AssetInventoryItem inventoryItem = requireInventoryItem(bo.getInventoryItemId());
        validateAssetOwnership(bo.getAssetId(), bo.getTaskId(), inventoryItem);
        if (assetRectificationMapper.selectByInventoryItemId(bo.getInventoryItemId()) != null)
        {
            throw new ServiceException("\u5f53\u524d\u5de1\u68c0\u7ed3\u679c\u5df2\u53d1\u8d77\u6574\u6539\u5355");
        }
        requireAsset(bo.getAssetId());
        AssetRectificationOrder order = buildOrderEntity(bo, null, RECTIFICATION_STATUS_PENDING);
        order.setRectificationNo(generateNextRectificationNo());
        order.setCreateBy(operator);
        int rows = assetRectificationMapper.insertAssetRectification(order);
        if (rows <= 0)
        {
            throw new ServiceException("\u65b0\u589e\u6574\u6539\u5355\u5931\u8d25");
        }
        syncInventoryFollowUp(order.getInventoryItemId(), order.getRectificationStatus(), order.getRectificationId());
        assetChangeLogMapper.insertAssetChangeLog(buildChangeLog(bo.getAssetId(), operator,
            "\u53d1\u8d77\u6574\u6539\u5355\uff1a" + order.getRectificationNo() + "\uff0c\u95ee\u9898\u7c7b\u578b\uff1a"
                + order.getIssueType()));
        return order.getRectificationId();
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAssetRectification(AssetRectificationBo bo, String operator)
    {
        validateBo(bo);
        if (bo.getRectificationId() == null)
        {
            throw new ServiceException("\u6574\u6539\u5355ID\u4e0d\u80fd\u4e3a\u7a7a");
        }
        AssetRectificationVo current = selectAssetRectificationById(bo.getRectificationId());
        if (!bo.getAssetId().equals(current.getAssetId()))
        {
            throw new ServiceException("\u6574\u6539\u5355\u4e0e\u8d44\u4ea7\u4e0d\u5339\u914d");
        }
        if (isCompletedRectification(current.getRectificationStatus()))
        {
            throw new ServiceException("\u6574\u6539\u5355\u5df2\u5b8c\u6210\uff0c\u4e0d\u5141\u8bb8\u518d\u6b21\u7f16\u8f91");
        }
        ensureRegisterStageStatus(bo.getRectificationStatus());
        AssetInventoryItem inventoryItem = requireInventoryItem(bo.getInventoryItemId());
        validateAssetOwnership(bo.getAssetId(), bo.getTaskId(), inventoryItem);
        AssetRectificationOrder order = buildOrderEntity(bo, current, RECTIFICATION_STATUS_PENDING);
        order.setRectificationId(bo.getRectificationId());
        order.setRectificationNo(current.getRectificationNo());
        order.setUpdateBy(operator);
        int rows = assetRectificationMapper.updateAssetRectification(order);
        if (rows <= 0)
        {
            throw new ServiceException("\u66f4\u65b0\u6574\u6539\u5355\u5931\u8d25");
        }
        syncInventoryFollowUp(order.getInventoryItemId(), order.getRectificationStatus(), order.getRectificationId());
        assetChangeLogMapper.insertAssetChangeLog(buildChangeLog(bo.getAssetId(), operator,
            "\u66f4\u65b0\u6574\u6539\u5355\uff1a" + current.getRectificationNo() + "\uff0c\u72b6\u6001\uff1a"
                + order.getRectificationStatus()));
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
            throw new ServiceException("\u6574\u6539\u5355\u4e0e\u8d44\u4ea7\u4e0d\u5339\u914d");
        }
        if (RECTIFICATION_STATUS_COMPLETED.equals(StringUtils.upperCase(current.getRectificationStatus())))
        {
            throw new ServiceException("\u6574\u6539\u5355\u5df2\u5b8c\u6210\uff0c\u8bf7\u52ff\u91cd\u590d\u63d0\u4ea4");
        }

        AssetRectificationOrder order = buildCompletedOrder(current, bo, operator);
        int rows = assetRectificationMapper.updateAssetRectification(order);
        if (rows <= 0)
        {
            throw new ServiceException("\u5b8c\u6210\u6574\u6539\u5931\u8d25");
        }

        syncInventoryFollowUp(order.getInventoryItemId(), order.getRectificationStatus(), order.getRectificationId());
        assetChangeLogMapper.insertAssetChangeLog(buildChangeLog(assetId, operator,
            "\u5b8c\u6210\u6574\u6539\u5355\uff1a" + current.getRectificationNo() + "\uff0c\u5b8c\u6210\u8bf4\u660e\uff1a"
                + order.getCompletionDesc()));
        return rows;
    }

    private void validateBo(AssetRectificationBo bo)
    {
        if (bo == null)
        {
            throw new ServiceException("\u6574\u6539\u53c2\u6570\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (bo.getAssetId() == null)
        {
            throw new ServiceException("\u8d44\u4ea7ID\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (bo.getTaskId() == null)
        {
            throw new ServiceException("\u5de1\u68c0\u4efb\u52a1ID\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (bo.getInventoryItemId() == null)
        {
            throw new ServiceException("\u5de1\u68c0\u7ed3\u679c\u660e\u7ec6ID\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (StringUtils.isBlank(bo.getIssueType()))
        {
            throw new ServiceException("\u95ee\u9898\u7c7b\u578b\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (StringUtils.isBlank(bo.getIssueDesc()))
        {
            throw new ServiceException("\u95ee\u9898\u63cf\u8ff0\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (bo.getResponsibleDeptId() == null)
        {
            throw new ServiceException("\u8d23\u4efb\u90e8\u95e8\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (bo.getResponsibleUserId() == null)
        {
            throw new ServiceException("\u8d23\u4efb\u4eba\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (bo.getDeadlineDate() == null)
        {
            throw new ServiceException("\u6574\u6539\u671f\u9650\u4e0d\u80fd\u4e3a\u7a7a");
        }
    }

    private void validateCompleteBo(Long assetId, Long rectificationId, AssetRectificationCompleteBo bo)
    {
        if (assetId == null)
        {
            throw new ServiceException("\u8d44\u4ea7ID\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (rectificationId == null)
        {
            throw new ServiceException("\u6574\u6539\u5355ID\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (bo == null)
        {
            throw new ServiceException("\u6574\u6539\u5b8c\u6210\u53c2\u6570\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (StringUtils.isBlank(bo.getCompletionDesc()))
        {
            throw new ServiceException("\u5b8c\u6210\u8bf4\u660e\u4e0d\u80fd\u4e3a\u7a7a");
        }
    }

    private AssetInventoryItem requireInventoryItem(Long inventoryItemId)
    {
        AssetInventoryItem item = assetInventoryMapper.selectAssetInventoryItemById(inventoryItemId);
        if (item == null)
        {
            throw new ServiceException("\u5de1\u68c0\u7ed3\u679c\u660e\u7ec6\u4e0d\u5b58\u5728");
        }
        return item;
    }

    private void validateAssetOwnership(Long assetId, Long taskId, AssetInventoryItem inventoryItem)
    {
        if (!assetId.equals(inventoryItem.getAssetId()))
        {
            throw new ServiceException("\u6574\u6539\u8d44\u4ea7\u4e0e\u5de1\u68c0\u7ed3\u679c\u4e0d\u5339\u914d");
        }
        if (!taskId.equals(inventoryItem.getTaskId()))
        {
            throw new ServiceException("\u6574\u6539\u4efb\u52a1\u4e0e\u5de1\u68c0\u7ed3\u679c\u4e0d\u5339\u914d");
        }
    }

    private void requireAsset(Long assetId)
    {
        AssetLedger asset = assetLedgerMapper.selectAssetById(assetId);
        if (asset == null)
        {
            throw new ServiceException("\u8d44\u4ea7\u4e0d\u5b58\u5728");
        }
    }

    private AssetRectificationOrder buildOrderEntity(AssetRectificationBo bo, AssetRectificationVo current,
        String rectificationStatus)
    {
        AssetRectificationOrder order = new AssetRectificationOrder();
        order.setAssetId(bo.getAssetId());
        order.setTaskId(bo.getTaskId());
        order.setInventoryItemId(bo.getInventoryItemId());
        order.setRectificationStatus(rectificationStatus);
        order.setIssueType(StringUtils.trim(bo.getIssueType()));
        order.setIssueDesc(StringUtils.trim(bo.getIssueDesc()));
        order.setResponsibleDeptId(bo.getResponsibleDeptId());
        order.setResponsibleUserId(bo.getResponsibleUserId());
        order.setDeadlineDate(bo.getDeadlineDate());
        // 中文注释：整改登记阶段只维护基础责任信息，完成事实只能由独立完成动作写入。
        order.setCompletedTime(current == null ? null : current.getCompletedTime());
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

    private void ensureRegisterStageStatus(String sourceStatus)
    {
        String status = StringUtils.upperCase(StringUtils.defaultIfBlank(StringUtils.trim(sourceStatus), RECTIFICATION_STATUS_PENDING));
        if (!RECTIFICATION_STATUS_PENDING.equals(status))
        {
            throw new ServiceException(
                "\u6574\u6539\u767b\u8bb0\u9636\u6bb5\u4e0d\u5141\u8bb8\u76f4\u63a5\u7f6e\u4e3a\u5df2\u5b8c\u6210\uff0c\u8bf7\u4f7f\u7528\u6574\u6539\u5b8c\u6210\u52a8\u4f5c");
        }
    }

    private boolean isCompletedRectification(String rectificationStatus)
    {
        return RECTIFICATION_STATUS_COMPLETED.equals(StringUtils.upperCase(StringUtils.trim(rectificationStatus)));
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
            throw new ServiceException("\u56de\u5199\u5de1\u68c0\u7ed3\u679c\u6574\u6539\u72b6\u6001\u5931\u8d25");
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

