package com.ruoyi.asset.service.impl;

import java.util.Collections;
import com.ruoyi.asset.domain.AssetCategoryAttr;
import com.ruoyi.asset.mapper.AssetCategoryAttrMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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

        when(assetCategoryAttrMapper.selectAssetCategoryAttrList(any(AssetCategoryAttr.class)))
            .thenReturn(Collections.emptyList());
        when(assetCategoryAttrMapper.insertAssetCategoryAttr(any(AssetCategoryAttr.class))).thenReturn(1);

        int rows = assetCategoryAttrService.insertAssetCategoryAttr(assetCategoryAttr);

        assertEquals(1, rows);

        ArgumentCaptor<AssetCategoryAttr> captor = ArgumentCaptor.forClass(AssetCategoryAttr.class);
        verify(assetCategoryAttrMapper).insertAssetCategoryAttr(captor.capture());
        assertEquals("manufacturer", captor.getValue().getAttrCode());
        assertEquals("text", captor.getValue().getAttrType());
        assertNotNull(captor.getValue().getCreateTime());
        assertNotNull(captor.getValue().getUpdateTime());
    }
}
