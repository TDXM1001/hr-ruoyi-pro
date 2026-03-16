package com.ruoyi.asset.service.impl;

import java.util.Collections;
import com.ruoyi.asset.domain.AssetCategoryAttr;
import com.ruoyi.asset.mapper.AssetCategoryAttrMapper;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 资产分类属性模板服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AssetCategoryAttrServiceImplTest {
    @Mock
    private AssetCategoryAttrMapper assetCategoryAttrMapper;

    @InjectMocks
    private AssetCategoryAttrServiceImpl assetCategoryAttrService;

    @Test
    void shouldBackfillAttrTypeFromDataTypeWhenInsertCategoryAttr() {
        AssetCategoryAttr assetCategoryAttr = new AssetCategoryAttr();
        assetCategoryAttr.setCategoryId(10L);
        assetCategoryAttr.setAttrCode("Manufacturer");
        assetCategoryAttr.setAttrName("厂商");
        assetCategoryAttr.setDataType("text");
        assetCategoryAttr.setOptionSourceType("manual");

        when(assetCategoryAttrMapper.selectAssetCategoryAttrList(any(AssetCategoryAttr.class)))
            .thenReturn(Collections.emptyList());
        when(assetCategoryAttrMapper.insertAssetCategoryAttr(any(AssetCategoryAttr.class))).thenReturn(1);

        int rows = assetCategoryAttrService.insertAssetCategoryAttr(assetCategoryAttr);

        assertEquals(1, rows);

        ArgumentCaptor<AssetCategoryAttr> captor = ArgumentCaptor.forClass(AssetCategoryAttr.class);
        verify(assetCategoryAttrMapper).insertAssetCategoryAttr(captor.capture());
        assertEquals("manufacturer", captor.getValue().getAttrCode());
        assertEquals("text", captor.getValue().getAttrType());
        assertEquals("0", captor.getValue().getIsRequired());
        assertEquals("0", captor.getValue().getIsUnique());
        assertEquals("0", captor.getValue().getIsListDisplay());
        assertEquals("0", captor.getValue().getIsQueryCondition());
        assertEquals("1", captor.getValue().getOptionSourceType());
        assertNotNull(captor.getValue().getCreateTime());
        assertNotNull(captor.getValue().getUpdateTime());
    }

    @Test
    void shouldRejectReservedAttrCodeAfterSnakeCaseNormalization() {
        AssetCategoryAttr assetCategoryAttr = new AssetCategoryAttr();
        assetCategoryAttr.setCategoryId(10L);
        assetCategoryAttr.setAttrCode("Asset-No");
        assetCategoryAttr.setAttrName("资产编号");
        assetCategoryAttr.setDataType("text");

        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> assetCategoryAttrService.insertAssetCategoryAttr(assetCategoryAttr)
        );

        assertEquals("字段编码[asset_no]为系统保留字段，不允许使用", exception.getMessage());
        verify(assetCategoryAttrMapper, never()).insertAssetCategoryAttr(any(AssetCategoryAttr.class));
    }
}
