package com.ruoyi.asset.service;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import com.ruoyi.asset.enums.AssetStatus;

/**
 * 资产状态机。
 * <p>
 * 当前状态机只负责判断状态流转是否合法，不直接替代业务前置条件校验。
 * 例如“待处置 -> 已处置”虽然状态上允许，但仍需在处置服务中校验处置单是否完成。
 * </p>
 *
 * @author Codex
 */
@Component
public class AssetStatusMachine
{
    /** 状态流转映射 */
    private final Map<AssetStatus, EnumSet<AssetStatus>> transitionMap = new EnumMap<>(AssetStatus.class);

    public AssetStatusMachine()
    {
        register(AssetStatus.DRAFT, AssetStatus.IN_LEDGER);
        register(AssetStatus.IN_LEDGER, AssetStatus.IN_USE, AssetStatus.IDLE, AssetStatus.INVENTORYING, AssetStatus.PENDING_DISPOSAL);
        register(AssetStatus.IN_USE, AssetStatus.IDLE, AssetStatus.INVENTORYING, AssetStatus.PENDING_DISPOSAL);
        register(AssetStatus.IDLE, AssetStatus.IN_USE, AssetStatus.INVENTORYING, AssetStatus.PENDING_DISPOSAL);
        register(AssetStatus.INVENTORYING, AssetStatus.IN_USE, AssetStatus.IDLE, AssetStatus.PENDING_DISPOSAL);
        register(AssetStatus.PENDING_DISPOSAL, AssetStatus.DISPOSED);
        register(AssetStatus.DISPOSED);
    }

    /**
     * 判断状态是否允许流转。
     *
     * @param from 当前状态
     * @param to 目标状态
     * @return true 允许流转，false 不允许流转
     */
    public boolean canTransit(AssetStatus from, AssetStatus to)
    {
        if (from == null || to == null)
        {
            return false;
        }
        return transitionMap.getOrDefault(from, EnumSet.noneOf(AssetStatus.class)).contains(to);
    }

    /**
     * 获取某个状态允许流转到的目标状态集合。
     *
     * @param from 当前状态
     * @return 可流转状态集合
     */
    public Set<AssetStatus> getAllowedTransitions(AssetStatus from)
    {
        if (from == null)
        {
            return Collections.emptySet();
        }
        EnumSet<AssetStatus> targets = transitionMap.get(from);
        if (targets == null || targets.isEmpty())
        {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(targets));
    }

    /**
     * 注册状态流转规则。
     *
     * @param from 当前状态
     * @param targets 允许的目标状态
     */
    private void register(AssetStatus from, AssetStatus... targets)
    {
        EnumSet<AssetStatus> statusSet = EnumSet.noneOf(AssetStatus.class);
        if (targets != null)
        {
            for (AssetStatus target : targets)
            {
                statusSet.add(target);
            }
        }
        transitionMap.put(from, statusSet);
    }
}
