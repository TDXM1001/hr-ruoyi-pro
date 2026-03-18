package com.ruoyi.asset.service.impl;

import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.asset.domain.AssetChangeLog;
import com.ruoyi.asset.domain.AssetHandoverItem;
import com.ruoyi.asset.domain.AssetHandoverOrder;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.bo.AssetHandoverCreateBo;
import com.ruoyi.asset.domain.bo.AssetHandoverOrderBo;
import com.ruoyi.asset.domain.vo.AssetHandoverItemVo;
import com.ruoyi.asset.domain.vo.AssetHandoverOrderVo;
import com.ruoyi.asset.enums.AssetBizType;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetHandoverItemMapper;
import com.ruoyi.asset.mapper.AssetHandoverOrderMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.asset.service.IAssetHandoverService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 资产交接服务实现。
 *
 * <p>一期交接采用“主单 + 明细”模型。
 * 该实现负责一次性校验整批资产后，再落主单、写明细、回写台账和记录变更日志，
 * 以保证领用、调拨、退还流程的业务闭环与可追溯性。</p>
 *
 * @author Codex
 */
@Service
public class AssetHandoverServiceImpl implements IAssetHandoverService
{
    private static final String HANDOVER_NO_PREFIX = "HD";

    private static final int HANDOVER_NO_SERIAL_LENGTH = 4;

    private static final String HANDOVER_CONFIRMED = "CONFIRMED";

    private static final String FIXED_ASSET_TYPE = "FIXED";

    @Autowired
    private AssetHandoverOrderMapper assetHandoverOrderMapper;

    @Autowired
    private AssetHandoverItemMapper assetHandoverItemMapper;

    @Autowired
    private AssetLedgerMapper assetLedgerMapper;

    @Autowired
    private AssetChangeLogMapper assetChangeLogMapper;

    /**
     * 查询交接主单列表。
     *
     * @param bo 查询参数
     * @return 主单列表
     */
    @Override
    public List<AssetHandoverOrderVo> selectAssetHandoverOrderList(AssetHandoverOrderBo bo)
    {
        return assetHandoverOrderMapper.selectAssetHandoverOrderList(bo);
    }

    /**
     * 查询主单下的交接明细。
     *
     * @param handoverOrderId 主单ID
     * @return 明细列表
     */
    @Override
    public List<AssetHandoverItemVo> selectAssetHandoverItemsByOrderId(Long handoverOrderId)
    {
        if (handoverOrderId == null)
        {
            throw new ServiceException("交接主单ID不能为空");
        }
        return assetHandoverItemMapper.selectAssetHandoverItemsByOrderId(handoverOrderId);
    }

    /**
     * 创建交接主单并统一回写台账使用信息。
     *
     * @param bo 建单参数
     * @param operator 操作人
     * @return 主单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createHandoverOrder(AssetHandoverCreateBo bo, String operator)
    {
        validateCreateParams(bo);

        AssetBizType bizType = parseHandoverBizType(bo.getHandoverType());
        List<AssetLedger> assets = loadAssets(bo.getAssetIds());
        validateAssetTypeScope(assets);
        validateBatchBusinessRule(assets, bizType, bo);

        Date now = DateUtils.getNowDate();
        String handoverNo = generateNextHandoverNo();
        AssetHandoverOrder order = buildOrderEntity(bo, assets, handoverNo, operator, now);
        int orderRows = assetHandoverOrderMapper.insertAssetHandoverOrder(order);
        if (orderRows <= 0)
        {
            throw new ServiceException("新增资产交接主单失败");
        }

        List<AssetHandoverItem> items = buildItemEntities(order.getHandoverOrderId(), bo, assets, bizType, operator);
        int itemRows = assetHandoverItemMapper.batchInsertAssetHandoverItems(items);
        if (itemRows != items.size())
        {
            throw new ServiceException("新增资产交接明细失败");
        }

        for (int i = 0; i < assets.size(); i++)
        {
            AssetLedger currentAsset = assets.get(i);
            AssetHandoverItem item = items.get(i);

            AssetLedger usageUpdate = buildUsageUpdateEntity(item, operator);
            int updateRows = assetLedgerMapper.updateAssetUsageInfo(usageUpdate);
            if (updateRows <= 0)
            {
                throw new ServiceException("回写资产使用信息失败");
            }

            assetChangeLogMapper.insertAssetChangeLog(AssetChangeLog.ofHandover(
                currentAsset.getAssetId(),
                bizType.getCode(),
                order.getHandoverOrderId(),
                item.getBeforeStatus(),
                item.getAfterStatus(),
                operator,
                buildChangeDesc(bizType, order.getHandoverNo(), currentAsset.getAssetCode())));
        }

        return order.getHandoverOrderId();
    }

    /**
     * 校验建单参数。
     *
     * @param bo 建单参数
     */
    private void validateCreateParams(AssetHandoverCreateBo bo)
    {
        if (bo == null)
        {
            throw new ServiceException("交接参数不能为空");
        }
        if (StringUtils.isBlank(bo.getHandoverType()))
        {
            throw new ServiceException("交接类型不能为空");
        }
        if (bo.getHandoverDate() == null)
        {
            throw new ServiceException("交接日期不能为空");
        }
        if (bo.getAssetIds() == null || bo.getAssetIds().isEmpty())
        {
            throw new ServiceException("交接资产不能为空");
        }

        Set<Long> uniqueAssetIds = new LinkedHashSet<>();
        for (Long assetId : bo.getAssetIds())
        {
            if (assetId == null)
            {
                throw new ServiceException("交接资产ID不能为空");
            }
            if (!uniqueAssetIds.add(assetId))
            {
                throw new ServiceException("同一交接单不允许重复选择同一资产");
            }
        }
    }

    /**
     * 按前端选择顺序加载资产。
     *
     * @param assetIds 资产ID列表
     * @return 资产列表
     */
    private List<AssetLedger> loadAssets(List<Long> assetIds)
    {
        List<AssetLedger> assets = new ArrayList<>(assetIds.size());
        for (Long assetId : assetIds)
        {
            AssetLedger asset = assetLedgerMapper.selectAssetById(assetId);
            if (asset == null)
            {
                throw new ServiceException("交接资产不存在，资产ID：" + assetId);
            }
            assets.add(asset);
        }
        return assets;
    }

    /**
     * 校验一期交接资产边界。
     *
     * @param assets 资产列表
     */
    private void validateAssetTypeScope(List<AssetLedger> assets)
    {
        Set<String> assetTypes = new LinkedHashSet<>();
        for (AssetLedger asset : assets)
        {
            assetTypes.add(resolveAssetType(asset));
        }

        if (assetTypes.size() > 1)
        {
            throw new ServiceException("同一交接单不允许混合不同资产类型");
        }
        if (!FIXED_ASSET_TYPE.equals(assetTypes.iterator().next()))
        {
            throw new ServiceException("一期交接单仅支持固定资产");
        }
    }

    /**
     * 批量校验交接业务规则。
     *
     * @param assets 资产列表
     * @param bizType 交接业务类型
     * @param bo 建单参数
     */
    private void validateBatchBusinessRule(List<AssetLedger> assets, AssetBizType bizType, AssetHandoverCreateBo bo)
    {
        for (AssetLedger asset : assets)
        {
            AssetStatus beforeStatus = parseAssetStatus(asset.getAssetStatus());
            validateBusinessRule(asset, beforeStatus, bizType, bo);
        }
    }

    /**
     * 校验单资产业务规则。
     *
     * @param currentAsset 当前资产
     * @param beforeStatus 交接前状态
     * @param bizType 交接业务类型
     * @param bo 建单参数
     */
    private void validateBusinessRule(AssetLedger currentAsset, AssetStatus beforeStatus, AssetBizType bizType,
        AssetHandoverCreateBo bo)
    {
        if (AssetStatus.DISPOSED.equals(beforeStatus))
        {
            throw new ServiceException("已处置资产不允许继续交接");
        }
        if (AssetStatus.PENDING_DISPOSAL.equals(beforeStatus))
        {
            throw new ServiceException("待处置资产不允许继续交接");
        }

        if (AssetBizType.ASSIGN.equals(bizType))
        {
            if (!(AssetStatus.IN_LEDGER.equals(beforeStatus) || AssetStatus.IDLE.equals(beforeStatus)))
            {
                throw new ServiceException("当前资产状态不允许执行领用");
            }
            validateTargetAssignee(bo);
            return;
        }

        if (AssetBizType.TRANSFER.equals(bizType))
        {
            if (!AssetStatus.IN_USE.equals(beforeStatus))
            {
                throw new ServiceException("当前资产不在使用中，不能执行调拨");
            }
            validateTargetAssignee(bo);
            if (isSameAssignment(currentAsset, bo))
            {
                throw new ServiceException("调拨目标不能与当前使用信息一致");
            }
            return;
        }

        if (AssetBizType.RETURN.equals(bizType))
        {
            if (!AssetStatus.IN_USE.equals(beforeStatus))
            {
                throw new ServiceException("当前资产不在使用中，不能执行退还");
            }
            if (resolveTargetDeptId(currentAsset, bo, bizType) == null)
            {
                throw new ServiceException("退还场景必须能确定目标部门");
            }
        }
    }

    /**
     * 校验领用或调拨的目标信息。
     *
     * @param bo 建单参数
     */
    private void validateTargetAssignee(AssetHandoverCreateBo bo)
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
     * 判断调拨目标是否与当前使用信息一致。
     *
     * @param currentAsset 当前资产
     * @param bo 建单参数
     * @return true 表示一致
     */
    private boolean isSameAssignment(AssetLedger currentAsset, AssetHandoverCreateBo bo)
    {
        boolean sameDept = currentAsset.getUseDeptId() != null && currentAsset.getUseDeptId().equals(bo.getToDeptId());
        boolean sameUser = currentAsset.getResponsibleUserId() != null
            && currentAsset.getResponsibleUserId().equals(bo.getToUserId());
        boolean sameLocation = StringUtils.equals(StringUtils.trimToEmpty(currentAsset.getLocationName()),
            StringUtils.trimToEmpty(bo.getLocationName()));
        return sameDept && sameUser && (StringUtils.isBlank(bo.getLocationName()) || sameLocation);
    }

    /**
     * 构建交接主单实体。
     *
     * @param bo 建单参数
     * @param assets 资产列表
     * @param handoverNo 交接单号
     * @param operator 操作人
     * @param now 当前时间
     * @return 主单实体
     */
    private AssetHandoverOrder buildOrderEntity(AssetHandoverCreateBo bo, List<AssetLedger> assets, String handoverNo,
        String operator, Date now)
    {
        AssetHandoverOrder order = new AssetHandoverOrder();
        order.setHandoverNo(handoverNo);
        order.setAssetType(resolveAssetType(assets.get(0)));
        order.setHandoverType(StringUtils.upperCase(StringUtils.trim(bo.getHandoverType())));
        order.setHandoverStatus(HANDOVER_CONFIRMED);
        order.setHandoverDate(bo.getHandoverDate());
        order.setAssetCount(assets.size());
        order.setToDeptId(resolveTargetDeptId(assets.get(0), bo, parseHandoverBizType(bo.getHandoverType())));
        order.setToUserId(resolveTargetUserId(bo));
        order.setLocationName(resolveTargetLocation(assets.get(0), bo));
        order.setConfirmBy(operator);
        order.setConfirmTime(now);
        order.setCreateBy(operator);
        order.setRemark(bo.getRemark());
        return order;
    }

    /**
     * 构建交接明细实体列表。
     *
     * @param handoverOrderId 主单ID
     * @param bo 建单参数
     * @param assets 资产列表
     * @param bizType 交接业务类型
     * @param operator 操作人
     * @return 明细列表
     */
    private List<AssetHandoverItem> buildItemEntities(Long handoverOrderId, AssetHandoverCreateBo bo,
        List<AssetLedger> assets, AssetBizType bizType, String operator)
    {
        List<AssetHandoverItem> items = new ArrayList<>(assets.size());
        for (AssetLedger currentAsset : assets)
        {
            AssetHandoverItem item = new AssetHandoverItem();
            item.setHandoverOrderId(handoverOrderId);
            item.setAssetId(currentAsset.getAssetId());
            item.setAssetCode(StringUtils.trimToEmpty(currentAsset.getAssetCode()));
            item.setAssetName(currentAsset.getAssetName());
            item.setFromDeptId(currentAsset.getUseDeptId());
            item.setFromUserId(currentAsset.getResponsibleUserId());
            item.setFromLocationName(StringUtils.trimToEmpty(currentAsset.getLocationName()));
            item.setToDeptId(resolveTargetDeptId(currentAsset, bo, bizType));
            item.setToUserId(resolveTargetUserId(bo));
            item.setToLocationName(resolveTargetLocation(currentAsset, bo));
            item.setBeforeStatus(parseAssetStatus(currentAsset.getAssetStatus()).getCode());
            item.setAfterStatus(resolveNextStatus(bizType).getCode());
            item.setCreateBy(operator);
            item.setRemark(bo.getRemark());
            items.add(item);
        }
        return items;
    }

    /**
     * 构建台账使用信息回写实体。
     *
     * @param item 交接明细
     * @param operator 操作人
     * @return 台账更新实体
     */
    private AssetLedger buildUsageUpdateEntity(AssetHandoverItem item, String operator)
    {
        AssetLedger updateEntity = new AssetLedger();
        updateEntity.setAssetId(item.getAssetId());
        updateEntity.setUseDeptId(item.getToDeptId());
        updateEntity.setResponsibleUserId(item.getToUserId());
        updateEntity.setLocationName(item.getToLocationName());
        updateEntity.setAssetStatus(item.getAfterStatus());
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
     * @param bo 建单参数
     * @param bizType 交接业务类型
     * @return 目标部门ID
     */
    private Long resolveTargetDeptId(AssetLedger currentAsset, AssetHandoverCreateBo bo, AssetBizType bizType)
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
     * @param bo 建单参数
     * @return 目标责任人ID
     */
    private Long resolveTargetUserId(AssetHandoverCreateBo bo)
    {
        return bo.getToUserId();
    }

    /**
     * 解析目标位置。
     *
     * @param currentAsset 当前资产
     * @param bo 建单参数
     * @return 目标位置
     */
    private String resolveTargetLocation(AssetLedger currentAsset, AssetHandoverCreateBo bo)
    {
        if (StringUtils.isNotBlank(bo.getLocationName()))
        {
            return StringUtils.trim(bo.getLocationName());
        }
        return StringUtils.trimToEmpty(currentAsset.getLocationName());
    }

    /**
     * 解析资产类型，空值按固定资产处理。
     *
     * @param asset 资产实体
     * @return 标准化资产类型
     */
    private String resolveAssetType(AssetLedger asset)
    {
        return StringUtils.upperCase(StringUtils.defaultIfBlank(asset.getAssetType(), FIXED_ASSET_TYPE));
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
        String currentMaxNo = assetHandoverOrderMapper.selectMaxHandoverNoByPrefix(handoverNoPrefix);
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
     * @param assetCode 资产编码
     * @return 日志说明
     */
    private String buildChangeDesc(AssetBizType bizType, String handoverNo, String assetCode)
    {
        return bizType.getDescription() + "（单号：" + handoverNo + "，资产编码：" + StringUtils.trimToEmpty(assetCode) + "）";
    }
}
