package com.ruoyi.asset.service.finance;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import com.ruoyi.asset.domain.AssetFinance;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * 年数总和法折旧测试。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
class SumOfYearsDepreciationTest {
    @Test
    void shouldCalculateMonthlyDepreciationForSumOfYears() {
        assertDoesNotThrow(
            () -> Class.forName("com.ruoyi.asset.service.finance.SumOfYearsDepreciation"),
            "尚未实现年数总和法策略类"
        );

        AssetFinance finance = buildFinance("13000.00", "0.10", 12, "年数总和法");
        AssetFinance result = invokeFinanceCalculation(finance, 2);

        assertAll(
            () -> assertEquals(new BigDecimal("1300.00"), result.getSalvageValue()),
            () -> assertEquals(new BigDecimal("11700.00"), result.getDepreciableValue()),
            () -> assertEquals(new BigDecimal("1650.00"), result.getMonthlyDepreciationAmount()),
            () -> assertEquals(new BigDecimal("3450.00"), result.getAccumulatedDepreciation()),
            () -> assertEquals(new BigDecimal("9550.00"), result.getNetBookValue()),
            () -> assertEquals(new BigDecimal("9550.00"), result.getBookValue())
        );
    }

    @Test
    void shouldRoundBookValueForSumOfYears() {
        AssetFinance finance = buildFinance("13000.00", "0.10", 12, "年数总和法");
        finance.setImpairmentAmount(new BigDecimal("99.995"));

        AssetFinance result = invokeFinanceCalculation(finance, 12);

        assertAll(
            () -> assertEquals(new BigDecimal("150.00"), result.getMonthlyDepreciationAmount()),
            () -> assertEquals(new BigDecimal("11700.00"), result.getAccumulatedDepreciation()),
            () -> assertEquals(new BigDecimal("1300.00"), result.getNetBookValue()),
            () -> assertEquals(new BigDecimal("1200.01"), result.getBookValue())
        );
    }

    /**
     * 通过反射调用待实现的财务服务，先让测试红起来。
     */
    private AssetFinance invokeFinanceCalculation(AssetFinance finance, int periodIndex) {
        try {
            Class<?> serviceClass = Class.forName("com.ruoyi.asset.service.impl.AssetFinanceServiceImpl");
            Object service = serviceClass.getDeclaredConstructor().newInstance();
            Method method = serviceClass.getMethod("calculateDepreciatedFinance", AssetFinance.class, int.class);
            return (AssetFinance) method.invoke(service, finance, periodIndex);
        } catch (ClassNotFoundException ex) {
            fail("尚未实现 AssetFinanceServiceImpl");
        } catch (NoSuchMethodException ex) {
            fail("尚未实现 calculateDepreciatedFinance 方法");
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getTargetException();
            fail("财务计算执行失败: " + cause.getMessage(), cause);
        } catch (ReflectiveOperationException ex) {
            fail("反射调用财务计算失败: " + ex.getMessage(), ex);
        }
        return null;
    }

    private AssetFinance buildFinance(String originalValue, String salvageRate, int usefulLifeMonth, String method) {
        AssetFinance finance = new AssetFinance();
        finance.setOriginalValue(new BigDecimal(originalValue));
        finance.setSalvageRate(new BigDecimal(salvageRate));
        finance.setUsefulLifeMonth(usefulLifeMonth);
        finance.setDepreciationMethod(method);
        return finance;
    }
}
