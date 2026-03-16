package com.ruoyi.asset.service.impl;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import com.ruoyi.asset.domain.AssetDisposal;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetMaintenance;
import com.ruoyi.asset.domain.AssetRealEstateDisposal;
import com.ruoyi.asset.domain.AssetRealEstateOwnershipChange;
import com.ruoyi.asset.domain.AssetRequisition;
import com.ruoyi.asset.mapper.AssetDisposalMapper;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.mapper.AssetMaintenanceMapper;
import com.ruoyi.asset.mapper.AssetRealEstateDisposalMapper;
import com.ruoyi.asset.mapper.AssetRealEstateOwnershipChangeMapper;
import com.ruoyi.asset.mapper.AssetRequisitionMapper;
import com.ruoyi.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 资产主档归档语义测试。
 */
@ExtendWith(MockitoExtension.class)
class AssetInfoServiceImplTest {
    @Mock
    private AssetInfoMapper assetInfoMapper;

    @Mock
    private AssetRequisitionMapper assetRequisitionMapper;

    @Mock
    private AssetMaintenanceMapper assetMaintenanceMapper;

    @Mock
    private AssetDisposalMapper assetDisposalMapper;

    @Mock
    private AssetRealEstateOwnershipChangeMapper assetRealEstateOwnershipChangeMapper;

    @Mock
    private AssetRealEstateDisposalMapper assetRealEstateDisposalMapper;

    @InjectMocks
    private AssetInfoServiceImpl service;

    @Test
    void shouldArchiveInsteadOfPhysicalDelete() {
        when(assetInfoMapper.selectAssetInfoByAssetId(1001L)).thenReturn(buildAssetInfo(1001L, "FA-1001", "1"));
        mockNoBlockingDocuments();

        service.deleteAssetInfoByAssetIds(new Long[]{1001L});

        verify(assetInfoMapper).updateAssetInfo(argThat(assetInfo ->
            Long.valueOf(1001L).equals(assetInfo.getAssetId())
                && "2".equals(readField(assetInfo, "delFlag"))
        ));
        verify(assetInfoMapper, never()).deleteAssetInfoByAssetIds(any());
    }

    @Test
    void shouldRejectScrappedAssetArchive() {
        when(assetInfoMapper.selectAssetInfoByAssetId(1002L)).thenReturn(buildAssetInfo(1002L, "FA-1002", "5"));

        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> service.deleteAssetInfoByAssetIds(new Long[]{1002L})
        );

        assertEquals("已报废资产不能归档，请保留生命周期审计记录", exception.getMessage());
        verify(assetInfoMapper, never()).updateAssetInfo(any(AssetInfo.class));
        verify(assetInfoMapper, never()).deleteAssetInfoByAssetIds(any());
    }

    @Test
    void shouldRejectDisposedAssetArchive() {
        when(assetInfoMapper.selectAssetInfoByAssetId(1003L)).thenReturn(buildAssetInfo(1003L, "FA-1003", "6"));

        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> service.deleteAssetInfoByAssetIds(new Long[]{1003L})
        );

        assertEquals("已处置资产不能归档，请保留生命周期审计记录", exception.getMessage());
        verify(assetInfoMapper, never()).updateAssetInfo(any(AssetInfo.class));
        verify(assetInfoMapper, never()).deleteAssetInfoByAssetIds(any());
    }

    @Test
    void shouldRejectArchiveWhenBlockingBusinessDocumentsExist() {
        when(assetInfoMapper.selectAssetInfoByAssetId(1004L)).thenReturn(buildAssetInfo(1004L, "FA-1004", "1"));
        AssetRequisition requisition = new AssetRequisition();
        requisition.setStatus(0);
        when(assetRequisitionMapper.selectAssetRequisitionList(any(AssetRequisition.class))).thenReturn(List.of(requisition));

        ServiceException exception = assertThrows(
            ServiceException.class,
            () -> service.deleteAssetInfoByAssetIds(new Long[]{1004L})
        );

        assertEquals("资产存在未完成的领用、维修、处置或不动产动作单据，不能归档", exception.getMessage());
        verify(assetInfoMapper, never()).updateAssetInfo(any(AssetInfo.class));
        verify(assetInfoMapper, never()).deleteAssetInfoByAssetIds(any());
    }

    /**
     * 统一构造最小资产主档，避免测试夹杂无关字段。
     */
    private AssetInfo buildAssetInfo(Long assetId, String assetNo, String assetStatus) {
        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetId(assetId);
        assetInfo.setAssetNo(assetNo);
        assetInfo.setAssetName("测试资产");
        assetInfo.setAssetStatus(assetStatus);
        return assetInfo;
    }

    /**
     * 当前批次只关心“无阻塞单据”场景，其他单据列表统一置空即可。
     */
    private void mockNoBlockingDocuments() {
        when(assetRequisitionMapper.selectAssetRequisitionList(any(AssetRequisition.class))).thenReturn(Collections.emptyList());
        when(assetMaintenanceMapper.selectAssetMaintenanceList(any(AssetMaintenance.class))).thenReturn(Collections.emptyList());
        when(assetDisposalMapper.selectAssetDisposalList(any(AssetDisposal.class))).thenReturn(Collections.emptyList());
        when(assetRealEstateOwnershipChangeMapper.selectOwnershipChangeList(any(AssetRealEstateOwnershipChange.class)))
            .thenReturn(Collections.emptyList());
        when(assetRealEstateDisposalMapper.selectDisposalList(any(AssetRealEstateDisposal.class)))
            .thenReturn(Collections.emptyList());
    }

    /**
     * 用反射读取待新增字段，保证红灯阶段测试也能正常编译。
     */
    private String readField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Object value = field.get(target);
            return value == null ? null : String.valueOf(value);
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }
}
