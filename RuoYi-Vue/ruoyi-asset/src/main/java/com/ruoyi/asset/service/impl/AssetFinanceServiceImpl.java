package com.ruoyi.asset.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import com.ruoyi.asset.domain.AssetFinance;
import com.ruoyi.asset.mapper.AssetFinanceMapper;
import com.ruoyi.asset.service.IAssetFinanceService;
import com.ruoyi.asset.service.finance.DepreciationStrategy;
import com.ruoyi.asset.service.finance.DoubleDecliningDepreciation;
import com.ruoyi.asset.service.finance.StraightLineDepreciation;
import com.ruoyi.asset.service.finance.SumOfYearsDepreciation;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 资产财务计算服务实现。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
@Service
public class AssetFinanceServiceImpl implements IAssetFinanceService {
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final Map<String, DepreciationStrategy> strategyMap = new HashMap<>();

    @Autowired
    private AssetFinanceMapper assetFinanceMapper;

    public AssetFinanceServiceImpl() {
        registerStrategy(new StraightLineDepreciation());
        registerStrategy(new DoubleDecliningDepreciation());
        registerStrategy(new SumOfYearsDepreciation());
    }

    @Override
    public AssetFinance selectAssetFinanceByAssetId(Long assetId) {
        return assetFinanceMapper.selectAssetFinanceByAssetId(assetId);
    }

    @Override
    public AssetFinance recalculateFinanceByAssetId(Long assetId) {
        AssetFinance assetFinance = assetFinanceMapper.selectAssetFinanceByAssetId(assetId);
        if (assetFinance == null) {
            throw new ServiceException("资产财务信息不存在");
        }
        AssetFinance calculatedFinance = calculateFinance(assetFinance);
        calculatedFinance.setUpdateTime(DateUtils.getNowDate());
        assetFinanceMapper.updateAssetFinance(calculatedFinance);
        return calculatedFinance;
    }

    @Override
    public AssetFinance calculateFinance(AssetFinance assetFinance) {
        return calculateDepreciatedFinance(assetFinance, 0);
    }

    @Override
    public AssetFinance calculateDepreciatedFinance(AssetFinance assetFinance, int periodIndex) {
        validateAssetFinance(assetFinance);
        String methodCode = normalizeMethodCode(assetFinance.getDepreciationMethod());
        DepreciationStrategy strategy = strategyMap.get(methodCode);
        if (strategy == null) {
            throw new ServiceException("暂不支持的折旧方法: " + assetFinance.getDepreciationMethod());
        }

        BigDecimal originalValue = scale(assetFinance.getOriginalValue());
        BigDecimal salvageValue = calculateSalvageValue(originalValue, assetFinance.getSalvageRate());
        BigDecimal depreciableValue = scale(originalValue.subtract(salvageValue));
        int usefulLifeMonth = assetFinance.getUsefulLifeMonth();
        int normalizedPeriod = Math.max(periodIndex, 0);
        if (normalizedPeriod > usefulLifeMonth) {
            normalizedPeriod = usefulLifeMonth;
        }

        BigDecimal accumulatedDepreciation = ZERO;
        BigDecimal currentPeriodDepreciation = ZERO;
        if (normalizedPeriod == 0) {
            currentPeriodDepreciation = resolveCurrentPeriodDepreciation(
                strategy,
                originalValue,
                salvageValue,
                depreciableValue,
                usefulLifeMonth,
                1,
                ZERO
            );
        } else {
            for (int currentPeriod = 1; currentPeriod <= normalizedPeriod; currentPeriod++) {
                currentPeriodDepreciation = resolveCurrentPeriodDepreciation(
                    strategy,
                    originalValue,
                    salvageValue,
                    depreciableValue,
                    usefulLifeMonth,
                    currentPeriod,
                    accumulatedDepreciation
                );
                accumulatedDepreciation = scale(accumulatedDepreciation.add(currentPeriodDepreciation));
            }
        }

        BigDecimal netBookValue = scale(originalValue.subtract(accumulatedDepreciation));
        if (netBookValue.compareTo(salvageValue) < 0) {
            netBookValue = salvageValue;
        }
        BigDecimal bookValue = calculateBookValue(
            netBookValue,
            assetFinance.getImpairmentAmount(),
            assetFinance.getDisposedValue()
        );

        assetFinance.setSalvageValue(salvageValue);
        assetFinance.setDepreciableValue(depreciableValue);
        assetFinance.setMonthlyDepreciationAmount(scale(currentPeriodDepreciation));
        assetFinance.setAccumulatedDepreciation(scale(accumulatedDepreciation));
        assetFinance.setNetBookValue(netBookValue);
        assetFinance.setBookValue(bookValue);
        return assetFinance;
    }

    /**
     * 计算单期折旧额，并在最后一期补足剩余可折旧金额。
     */
    private BigDecimal resolveCurrentPeriodDepreciation(
        DepreciationStrategy strategy,
        BigDecimal originalValue,
        BigDecimal salvageValue,
        BigDecimal depreciableValue,
        int usefulLifeMonth,
        int currentPeriod,
        BigDecimal accumulatedDepreciation
    ) {
        BigDecimal remainingAmount = scale(depreciableValue.subtract(accumulatedDepreciation));
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return ZERO;
        }
        if (currentPeriod >= usefulLifeMonth) {
            return remainingAmount;
        }
        return limitByRemaining(
            strategy.calculateCurrentPeriodDepreciation(originalValue, salvageValue, usefulLifeMonth, currentPeriod),
            depreciableValue,
            accumulatedDepreciation
        );
    }

    /**
     * 注册折旧策略，便于后续继续扩展。
     */
    private void registerStrategy(DepreciationStrategy strategy) {
        strategyMap.put(strategy.getMethodCode(), strategy);
    }

    /**
     * 校验财务主数据是否满足计算前置条件。
     */
    private void validateAssetFinance(AssetFinance assetFinance) {
        if (assetFinance == null) {
            throw new ServiceException("资产财务信息不能为空");
        }
        if (assetFinance.getOriginalValue() == null) {
            throw new ServiceException("资产原值不能为空");
        }
        if (assetFinance.getUsefulLifeMonth() == null || assetFinance.getUsefulLifeMonth() <= 0) {
            throw new ServiceException("使用月数必须大于0");
        }
        if (StringUtils.isBlank(assetFinance.getDepreciationMethod())) {
            throw new ServiceException("折旧方法不能为空");
        }
    }

    /**
     * 兼容中文名称、英文编码和数字字典值。
     */
    private String normalizeMethodCode(String depreciationMethod) {
        String method = StringUtils.trimToEmpty(depreciationMethod);
        if (StringUtils.equalsAnyIgnoreCase(method, "1", "STRAIGHT_LINE", "年限平均法", "直线法")) {
            return "STRAIGHT_LINE";
        }
        if (StringUtils.equalsAnyIgnoreCase(method, "2", "DOUBLE_DECLINING", "双倍余额递减法")) {
            return "DOUBLE_DECLINING";
        }
        if (StringUtils.equalsAnyIgnoreCase(method, "3", "SUM_OF_YEARS", "年数总和法")) {
            return "SUM_OF_YEARS";
        }
        return method.toUpperCase();
    }

    /**
     * 将残值率兼容为0-1之间的小数。
     */
    private BigDecimal normalizeRate(BigDecimal salvageRate) {
        if (salvageRate == null) {
            return BigDecimal.ZERO;
        }
        if (salvageRate.compareTo(BigDecimal.ONE) > 0) {
            return salvageRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        }
        return salvageRate;
    }

    /**
     * 计算残值。
     */
    private BigDecimal calculateSalvageValue(BigDecimal originalValue, BigDecimal salvageRate) {
        BigDecimal normalizedRate = normalizeRate(salvageRate);
        return scale(originalValue.multiply(normalizedRate));
    }

    /**
     * 对当前期折旧额做封顶，保证累计折旧不超过可折旧金额。
     */
    private BigDecimal limitByRemaining(BigDecimal depreciationAmount, BigDecimal depreciableValue, BigDecimal accumulatedDepreciation) {
        BigDecimal remainingAmount = scale(depreciableValue.subtract(accumulatedDepreciation));
        BigDecimal currentAmount = scale(depreciationAmount);
        if (currentAmount.compareTo(remainingAmount) > 0) {
            currentAmount = remainingAmount;
        }
        if (currentAmount.compareTo(BigDecimal.ZERO) < 0) {
            return ZERO;
        }
        return scale(currentAmount);
    }

    /**
     * 计算账面价值，并统一保留两位小数。
     */
    private BigDecimal calculateBookValue(BigDecimal netBookValue, BigDecimal impairmentAmount, BigDecimal disposedValue) {
        BigDecimal bookValue = netBookValue
            .subtract(defaultZero(impairmentAmount))
            .subtract(defaultZero(disposedValue));
        if (bookValue.compareTo(BigDecimal.ZERO) < 0) {
            return ZERO;
        }
        return scale(bookValue);
    }

    /**
     * 将空金额按0处理，但不提前裁剪原始精度。
     */
    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 统一金额精度处理。
     */
    private BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
