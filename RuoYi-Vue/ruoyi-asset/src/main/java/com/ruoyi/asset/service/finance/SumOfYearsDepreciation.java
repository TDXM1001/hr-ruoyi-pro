package com.ruoyi.asset.service.finance;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 年数总和法折旧策略。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public class SumOfYearsDepreciation implements DepreciationStrategy {
    @Override
    public String getMethodCode() {
        return "SUM_OF_YEARS";
    }

    @Override
    public BigDecimal calculateCurrentPeriodDepreciation(
        BigDecimal originalValue,
        BigDecimal salvageValue,
        int usefulLifeMonth,
        int currentPeriod
    ) {
        if (currentPeriod < 1 || currentPeriod > usefulLifeMonth || usefulLifeMonth <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal depreciableValue = originalValue.subtract(salvageValue);
        BigDecimal denominator = BigDecimal.valueOf((long) usefulLifeMonth * (usefulLifeMonth + 1) / 2);
        BigDecimal numerator = BigDecimal.valueOf(usefulLifeMonth - currentPeriod + 1L);
        return depreciableValue.multiply(numerator).divide(denominator, 2, RoundingMode.HALF_UP);
    }
}
