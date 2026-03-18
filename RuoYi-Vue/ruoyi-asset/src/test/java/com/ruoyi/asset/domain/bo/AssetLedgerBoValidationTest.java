package com.ruoyi.asset.domain.bo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * 资产台账业务入参校验测试。
 *
 * <p>当前一期要求资产编号由后端统一生成，因此新增与编辑通用入参
 * 不应再因为 assetCode 为空被前置校验拦截。</p>
 */
class AssetLedgerBoValidationTest
{
    private static Validator validator;

    @BeforeAll
    static void setUpValidator()
    {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldAllowBlankAssetCodeWhenOtherRequiredFieldsPresent()
    {
        AssetLedgerBo bo = new AssetLedgerBo();
        bo.setAssetCode("");
        bo.setAssetName("点测资产");
        bo.setAssetType("FIXED");
        bo.setCategoryId(1001L);
        bo.setSourceType("MANUAL");
        bo.setOwnerDeptId(103L);

        Set<String> violationFields = validator.validate(bo).stream()
            .map(ConstraintViolation::getPropertyPath)
            .map(Object::toString)
            .collect(Collectors.toSet());

        assertFalse(violationFields.contains("assetCode"));
    }
}
