package com.ruoyi.asset.service.finance;

import java.math.BigDecimal;

/**
 * 折旧策略接口。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public interface DepreciationStrategy {
    /**
     * 返回策略方法编码。
     *
     * @return 方法编码
     */
    public String getMethodCode();

    /**
     * 计算当前折旧期的折旧额。
     *
     * @param originalValue 原值
     * @param salvageValue 残值
     * @param usefulLifeMonth 使用月数
     * @param currentPeriod 当前期次，从1开始
     * @return 当前期折旧额
     */
    public BigDecimal calculateCurrentPeriodDepreciation(
        BigDecimal originalValue,
        BigDecimal salvageValue,
        int usefulLifeMonth,
        int currentPeriod
    );
}
