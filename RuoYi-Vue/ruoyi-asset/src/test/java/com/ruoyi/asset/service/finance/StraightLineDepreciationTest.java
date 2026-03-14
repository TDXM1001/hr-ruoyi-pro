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
 * 年限平均法折旧测试。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
class StraightLineDepreciationTest {
    @Test
    void shouldCalculateSalvageAndMonthlyDepreciationForStraightLine() {
        assertDoesNotThrow(
            () -> Class.forName("com.ruoyi.asset.service.finance.StraightLineDepreciation"),
            "尚未实现年限平均法策略类"
        );

        AssetFinance finance = buildFinance("10000.00", "0.035", 36, "年限平均法");
        AssetFinance result = invokeFinanceCalculation(finance, 1);

        assertAll(
            () -> assertEquals(new BigDecimal("350.00"), result.getSalvageValue()),
            () -> assertEquals(new BigDecimal("9650.00"), result.getDepreciableValue()),
            () -> assertEquals(new BigDecimal("268.06"), result.getMonthlyDepreciationAmount()),
            () -> assertEquals(new BigDecimal("268.06"), result.getAccumulatedDepreciation()),
            () -> assertEquals(new BigDecimal("9731.94"), result.getNetBookValue()),
            () -> assertEquals(new BigDecimal("9731.94"), result.getBookValue())
        );
    }

    @Test
    void shouldCapAccumulatedDepreciationAndRoundBookValueForStraightLine() {
        AssetFinance finance = buildFinance("10000.00", "0.035", 36, "年限平均法");
        finance.setImpairmentAmount(new BigDecimal("123.456"));
        finance.setDisposedValue(new BigDecimal("10.123"));

        AssetFinance result = invokeFinanceCalculation(finance, 36);

        assertAll(
            () -> assertEquals(new BigDecimal("267.90"), result.getMonthlyDepreciationAmount()),
            () -> assertEquals(new BigDecimal("9650.00"), result.getAccumulatedDepreciation()),
            () -> assertEquals(new BigDecimal("350.00"), result.getNetBookValue()),
            () -> assertEquals(new BigDecimal("216.42"), result.getBookValue())
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
