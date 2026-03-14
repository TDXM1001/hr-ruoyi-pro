package com.ruoyi.asset.service.finance;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 年限平均法折旧策略。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public class StraightLineDepreciation implements DepreciationStrategy {
    @Override
    public String getMethodCode() {
        return "STRAIGHT_LINE";
    }

    @Override
    public BigDecimal calculateCurrentPeriodDepreciation(
        BigDecimal originalValue,
        BigDecimal salvageValue,
        int usefulLifeMonth,
        int currentPeriod
    ) {
        if (currentPeriod < 1 || usefulLifeMonth <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal depreciableValue = originalValue.subtract(salvageValue);
        return depreciableValue.divide(BigDecimal.valueOf(usefulLifeMonth), 2, RoundingMode.HALF_UP);
    }
}
