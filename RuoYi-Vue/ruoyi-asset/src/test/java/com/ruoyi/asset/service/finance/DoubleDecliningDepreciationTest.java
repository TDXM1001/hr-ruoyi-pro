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
 * 双倍余额递减法折旧测试。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
class DoubleDecliningDepreciationTest {
    @Test
    void shouldCalculateMonthlyDepreciationForDoubleDeclining() {
        assertDoesNotThrow(
            () -> Class.forName("com.ruoyi.asset.service.finance.DoubleDecliningDepreciation"),
            "尚未实现双倍余额递减法策略类"
        );

        AssetFinance finance = buildFinance("12000.00", "0.05", 60, "双倍余额递减法");
        AssetFinance result = invokeFinanceCalculation(finance, 2);

        assertAll(
            () -> assertEquals(new BigDecimal("600.00"), result.getSalvageValue()),
            () -> assertEquals(new BigDecimal("11400.00"), result.getDepreciableValue()),
            () -> assertEquals(new BigDecimal("386.67"), result.getMonthlyDepreciationAmount()),
            () -> assertEquals(new BigDecimal("786.67"), result.getAccumulatedDepreciation()),
            () -> assertEquals(new BigDecimal("11213.33"), result.getNetBookValue()),
            () -> assertEquals(new BigDecimal("11213.33"), result.getBookValue())
        );
    }

    @Test
    void shouldCapAccumulatedDepreciationForDoubleDeclining() {
        AssetFinance finance = buildFinance("12000.00", "0.05", 60, "双倍余额递减法");
        finance.setImpairmentAmount(new BigDecimal("50.555"));

        AssetFinance result = invokeFinanceCalculation(finance, 60);

        assertAll(
            () -> assertEquals(new BigDecimal("11400.00"), result.getAccumulatedDepreciation()),
            () -> assertEquals(new BigDecimal("600.00"), result.getNetBookValue()),
            () -> assertEquals(new BigDecimal("549.45"), result.getBookValue())
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
