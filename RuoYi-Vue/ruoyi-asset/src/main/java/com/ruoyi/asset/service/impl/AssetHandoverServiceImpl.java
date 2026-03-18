package com.ruoyi.asset.service.impl;

import java.time.Year;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.asset.domain.AssetChangeLog;
import com.ruoyi.asset.domain.AssetHandover;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.bo.AssetHandoverBo;
import com.ruoyi.asset.domain.vo.AssetHandoverVo;
import com.ruoyi.asset.enums.AssetBizType;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetHandoverMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.asset.service.IAssetHandoverService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;

/**
 * 资产交接服务实现。
 *
 * <p>
 * 固定资产一期将领用、调拨、退还统一收敛到交接模型，确保：
 * 1. 不允许绕过业务单据直接修改台账使用关系。
 * 2. 每次交接都写入交接记录与变更日志，满足资产管理追责要求。
 * 3. 交接完成后立即回写台账“使用部门/责任人/位置/状态”当前态。
 * </p>
 *
 * @author Codex
 */
@Service
public class AssetHandoverServiceImpl implements IAssetHandoverService
{
    private static final String HANDOVER_NO_PREFIX = "HD";

    private static final int HANDOVER_NO_SERIAL_LENGTH = 4;

    private static final String HANDOVER_CONFIRMED = "CONFIRMED";

    @Autowired
    private AssetHandoverMapper assetHandoverMapper;

    @Autowired
    private AssetLedgerMapper assetLedgerMapper;

    @Autowired
    private AssetChangeLogMapper assetChangeLogMapper;

    /**
     * 查询交接记录列表。
     *
     * @param bo 查询条件
     * @return 交接列表
     */
    @Override
    public List<AssetHandoverVo> selectAssetHandoverList(AssetHandoverBo bo)
    {
        return assetHandoverMapper.selectAssetHandoverList(bo);
    }

    /**
     * 新增交接记录并完成台账回写。
     *
     * @param bo 交接参数
     * @param operator 操作人
     * @return 交接ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createHandover(AssetHandoverBo bo, String operator)
    {
        validateRequiredParams(bo);

        AssetLedger currentAsset = assetLedgerMapper.selectAssetById(bo.getAssetId());
        if (currentAsset == null)
        {
            throw new ServiceException("资产台账不存在，无法发起交接");
        }

        AssetBizType bizType = parseHandoverBizType(bo.getHandoverType());
        AssetStatus beforeStatus = parseAssetStatus(currentAsset.getAssetStatus());
        validateBusinessRule(currentAsset, beforeStatus, bizType, bo);

        Date now = DateUtils.getNowDate();
        AssetHandover assetHandover = buildHandoverEntity(bo, currentAsset, bizType, operator, now);
        int insertRows = assetHandoverMapper.insertAssetHandover(assetHandover);
        if (insertRows <= 0)
        {
            throw new ServiceException("新增资产交接记录失败");
        }

        AssetLedger usageUpdate = buildUsageUpdateEntity(currentAsset, bo, bizType, operator);
        int updateRows = assetLedgerMapper.updateAssetUsageInfo(usageUpdate);
        if (updateRows <= 0)
        {
            throw new ServiceException("回写资产使用信息失败");
        }

        assetChangeLogMapper.insertAssetChangeLog(AssetChangeLog.ofHandover(
            currentAsset.getAssetId(),
            bizType.getCode(),
            assetHandover.getHandoverId(),
            beforeStatus.getCode(),
            usageUpdate.getAssetStatus(),
            operator,
            buildChangeDesc(bizType, assetHandover.getHandoverNo())));

        return assetHandover.getHandoverId();
    }

    /**
     * 校验交接必须参数。
     *
     * @param bo 交接入参
     */
    private void validateRequiredParams(AssetHandoverBo bo)
    {
        if (bo == null)
        {
            throw new ServiceException("交接参数不能为空");
        }
        if (bo.getAssetId() == null)
        {
            throw new ServiceException("资产ID不能为空");
        }
        if (StringUtils.isBlank(bo.getHandoverType()))
        {
            throw new ServiceException("交接类型不能为空");
        }
    }

    /**
     * 校验交接业务规则。
     *
     * @param currentAsset 当前资产
     * @param beforeStatus 交接前状态
     * @param bizType 交接业务类型
     * @param bo 交接参数
     */
    private void validateBusinessRule(AssetLedger currentAsset, AssetStatus beforeStatus, AssetBizType bizType,
        AssetHandoverBo bo)
    {
        if (AssetStatus.DISPOSED.equals(beforeStatus))
        {
            throw new ServiceException("已处置资产不允许发起交接");
        }
        if (AssetStatus.PENDING_DISPOSAL.equals(beforeStatus))
        {
            throw new ServiceException("待处置资产不允许发起交接");
        }

        if (AssetBizType.ASSIGN.equals(bizType))
        {
            if (!(AssetStatus.IN_LEDGER.equals(beforeStatus) || AssetStatus.IDLE.equals(beforeStatus)))
            {
                throw new ServiceException("资产当前状态不允许领用，领用仅支持在册或闲置状态");
            }
            validateTargetAssignee(bo);
            return;
        }

        if (AssetBizType.TRANSFER.equals(bizType))
        {
            if (!AssetStatus.IN_USE.equals(beforeStatus))
            {
                throw new ServiceException("资产调拨仅支持使用中状态");
            }
            validateTargetAssignee(bo);
            if (isSameAssignment(currentAsset, bo))
            {
                throw new ServiceException("调拨目标与当前使用关系一致，无需重复调拨");
            }
            return;
        }

        if (AssetBizType.RETURN.equals(bizType))
        {
            if (!AssetStatus.IN_USE.equals(beforeStatus))
            {
                throw new ServiceException("资产退还仅支持使用中状态");
            }
            if (resolveTargetDeptId(currentAsset, bo, bizType) == null)
            {
                throw new ServiceException("退还时无法确定目标部门，请指定目标部门或维护权属部门");
            }
        }
    }

    /**
     * 校验领用/调拨的目标责任信息。
     *
     * @param bo 交接参数
     */
    private void validateTargetAssignee(AssetHandoverBo bo)
    {
        if (bo.getToDeptId() == null)
        {
            throw new ServiceException("目标部门不能为空");
        }
        if (bo.getToUserId() == null)
        {
            throw new ServiceException("目标责任人不能为空");
        }
    }

    /**
     * 判断调拨目标是否与当前使用关系一致。
     *
     * @param currentAsset 当前资产
     * @param bo 交接参数
     * @return true 一致
     */
    private boolean isSameAssignment(AssetLedger currentAsset, AssetHandoverBo bo)
    {
        boolean sameDept = currentAsset.getUseDeptId() != null && currentAsset.getUseDeptId().equals(bo.getToDeptId());
        boolean sameUser = currentAsset.getResponsibleUserId() != null
            && currentAsset.getResponsibleUserId().equals(bo.getToUserId());
        boolean sameLocation = StringUtils.equals(StringUtils.trimToEmpty(currentAsset.getLocationName()),
            StringUtils.trimToEmpty(bo.getLocationName()));
        return sameDept && sameUser && (StringUtils.isBlank(bo.getLocationName()) || sameLocation);
    }

    /**
     * 构建交接记录实体。
     *
     * @param bo 交接参数
     * @param currentAsset 当前资产
     * @param bizType 业务类型
     * @param operator 操作人
     * @param now 当前时间
     * @return 交接实体
     */
    private AssetHandover buildHandoverEntity(AssetHandoverBo bo, AssetLedger currentAsset, AssetBizType bizType,
        String operator, Date now)
    {
        AssetHandover assetHandover = new AssetHandover();
        BeanUtils.copyBeanProp(assetHandover, bo);
        assetHandover.setHandoverNo(generateNextHandoverNo());
        assetHandover.setHandoverType(bizType.getCode());
        assetHandover.setFromDeptId(currentAsset.getUseDeptId());
        assetHandover.setFromUserId(currentAsset.getResponsibleUserId());
        assetHandover.setFromLocationName(currentAsset.getLocationName());
        assetHandover.setToDeptId(resolveTargetDeptId(currentAsset, bo, bizType));
        assetHandover.setToUserId(resolveTargetUserId(bo, bizType));
        assetHandover.setLocationName(resolveTargetLocation(currentAsset, bo));
        assetHandover.setHandoverStatus(HANDOVER_CONFIRMED);
        assetHandover.setHandoverDate(bo.getHandoverDate() == null ? now : bo.getHandoverDate());
        assetHandover.setConfirmBy(operator);
        assetHandover.setConfirmTime(now);
        assetHandover.setCreateBy(operator);
        return assetHandover;
    }

    /**
     * 构建台账回写实体。
     *
     * @param currentAsset 当前资产
     * @param bo 交接参数
     * @param bizType 交接类型
     * @param operator 操作人
     * @return 台账更新实体
     */
    private AssetLedger buildUsageUpdateEntity(AssetLedger currentAsset, AssetHandoverBo bo, AssetBizType bizType,
        String operator)
    {
        AssetLedger updateEntity = new AssetLedger();
        updateEntity.setAssetId(currentAsset.getAssetId());
        updateEntity.setUseDeptId(resolveTargetDeptId(currentAsset, bo, bizType));
        updateEntity.setResponsibleUserId(resolveTargetUserId(bo, bizType));
        updateEntity.setLocationName(resolveTargetLocation(currentAsset, bo));
        updateEntity.setAssetStatus(resolveNextStatus(bizType).getCode());
        updateEntity.setUpdateBy(operator);
        return updateEntity;
    }

    /**
     * 解析交接业务类型。
     *
     * @param handoverType 交接类型编码
     * @return 业务类型
     */
    private AssetBizType parseHandoverBizType(String handoverType)
    {
        String normalizedType = StringUtils.upperCase(StringUtils.trimToEmpty(handoverType));
        if (AssetBizType.ASSIGN.getCode().equals(normalizedType))
        {
            return AssetBizType.ASSIGN;
        }
        if (AssetBizType.TRANSFER.getCode().equals(normalizedType))
        {
            return AssetBizType.TRANSFER;
        }
        if (AssetBizType.RETURN.getCode().equals(normalizedType))
        {
            return AssetBizType.RETURN;
        }
        throw new ServiceException("不支持的交接类型：" + handoverType);
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
     * 计算交接后的目标状态。
     *
     * @param bizType 交接业务类型
     * @return 目标状态
     */
    private AssetStatus resolveNextStatus(AssetBizType bizType)
    {
        if (AssetBizType.RETURN.equals(bizType))
        {
            return AssetStatus.IDLE;
        }
        return AssetStatus.IN_USE;
    }

    /**
     * 解析目标部门。
     *
     * @param currentAsset 当前资产
     * @param bo 交接参数
     * @param bizType 业务类型
     * @return 目标部门ID
     */
    private Long resolveTargetDeptId(AssetLedger currentAsset, AssetHandoverBo bo, AssetBizType bizType)
    {
        if (AssetBizType.RETURN.equals(bizType))
        {
            if (bo.getToDeptId() != null)
            {
                return bo.getToDeptId();
            }
            return currentAsset.getOwnerDeptId();
        }
        return bo.getToDeptId();
    }

    /**
     * 解析目标责任人。
     *
     * @param bo 交接参数
     * @param bizType 业务类型
     * @return 目标责任人ID
     */
    private Long resolveTargetUserId(AssetHandoverBo bo, AssetBizType bizType)
    {
        if (AssetBizType.RETURN.equals(bizType))
        {
            return bo.getToUserId();
        }
        return bo.getToUserId();
    }

    /**
     * 解析目标位置。
     *
     * @param currentAsset 当前资产
     * @param bo 交接参数
     * @return 目标位置
     */
    private String resolveTargetLocation(AssetLedger currentAsset, AssetHandoverBo bo)
    {
        if (StringUtils.isNotBlank(bo.getLocationName()))
        {
            return StringUtils.trim(bo.getLocationName());
        }
        return StringUtils.trimToEmpty(currentAsset.getLocationName());
    }

    /**
     * 生成下一条交接单号。
     *
     * @return 交接单号
     */
    private String generateNextHandoverNo()
    {
        String currentYear = String.valueOf(Year.now().getValue());
        String handoverNoPrefix = HANDOVER_NO_PREFIX + "-" + currentYear + "-";
        String currentMaxNo = assetHandoverMapper.selectMaxHandoverNoByPrefix(handoverNoPrefix);
        int nextSerial = parseNextSerial(currentMaxNo, handoverNoPrefix);
        return handoverNoPrefix + String.format("%0" + HANDOVER_NO_SERIAL_LENGTH + "d", nextSerial);
    }

    /**
     * 解析下一条交接流水号。
     *
     * @param currentMaxNo 当前最大单号
     * @param handoverNoPrefix 单号前缀
     * @return 下一条流水号
     */
    private int parseNextSerial(String currentMaxNo, String handoverNoPrefix)
    {
        if (StringUtils.isBlank(currentMaxNo) || !currentMaxNo.startsWith(handoverNoPrefix))
        {
            return 1;
        }
        String serialPart = currentMaxNo.substring(handoverNoPrefix.length());
        if (!StringUtils.isNumeric(serialPart))
        {
            return 1;
        }
        return Integer.parseInt(serialPart) + 1;
    }

    /**
     * 构建变更日志说明。
     *
     * @param bizType 业务类型
     * @param handoverNo 交接单号
     * @return 日志说明
     */
    private String buildChangeDesc(AssetBizType bizType, String handoverNo)
    {
        return bizType.getDescription() + "（单号：" + handoverNo + "）";
    }
}
