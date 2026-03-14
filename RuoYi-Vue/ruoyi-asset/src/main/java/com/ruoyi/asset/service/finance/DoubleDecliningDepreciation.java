package com.ruoyi.asset.service.finance;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 双倍余额递减法折旧策略。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public class DoubleDecliningDepreciation implements DepreciationStrategy {
    @Override
    public String getMethodCode() {
        return "DOUBLE_DECLINING";
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
        BigDecimal rate = BigDecimal.valueOf(2L)
            .divide(BigDecimal.valueOf(usefulLifeMonth), 10, RoundingMode.HALF_UP);
        BigDecimal depreciableValue = originalValue.subtract(salvageValue);
        BigDecimal accumulatedDepreciation = BigDecimal.ZERO;
        BigDecimal currentBookValue = originalValue;
        BigDecimal currentDepreciation = BigDecimal.ZERO;
        for (int period = 1; period <= currentPeriod; period++) {
            currentDepreciation = currentBookValue.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal remainingDepreciable = depreciableValue.subtract(accumulatedDepreciation);
            if (currentDepreciation.compareTo(remainingDepreciable) > 0) {
                currentDepreciation = remainingDepreciable;
            }
            if (currentDepreciation.compareTo(BigDecimal.ZERO) < 0) {
                currentDepreciation = BigDecimal.ZERO;
            }
            accumulatedDepreciation = accumulatedDepreciation.add(currentDepreciation);
            currentBookValue = originalValue.subtract(accumulatedDepreciation);
        }
        return currentDepreciation.setScale(2, RoundingMode.HALF_UP);
    }
}
