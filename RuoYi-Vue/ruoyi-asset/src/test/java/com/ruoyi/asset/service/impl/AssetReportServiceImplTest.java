package com.ruoyi.asset.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.ruoyi.asset.domain.vo.AssetReportSummaryVo;
import com.ruoyi.asset.domain.vo.AssetWarningVo;
import com.ruoyi.asset.mapper.AssetReportMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 资产报表与预警服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AssetReportServiceImplTest {

    @Mock
    private AssetReportMapper assetReportMapper;

    @InjectMocks
    private AssetReportServiceImpl assetReportService;

    @Test
    void shouldAggregateSummaryAndWarningsForSmeDashboard() {
        when(assetReportMapper.selectAssetTypeSummary()).thenReturn(List.of(
            buildTypeSummary("1", 12L, "120000.00"),
            buildTypeSummary("2", 4L, "800000.00")
        ));
        when(assetReportMapper.selectAssetStatusSummary()).thenReturn(List.of(
            buildStatusSummary("1", 9L, "500000.00"),
            buildStatusSummary("7", 3L, "100000.00"),
            buildStatusSummary("3", 2L, "70000.00")
        ));
        when(assetReportMapper.selectDeptSummary()).thenReturn(List.of(
            buildDeptSummary(101L, "行政部", 5L, "300000.00"),
            buildDeptSummary(102L, "信息部", 4L, "200000.00")
        ));
        when(assetReportMapper.selectIdleAssetWarnings()).thenReturn(List.of(
            buildWarningItem("FA-2026-0001", "闲置资产"),
            buildWarningItem("FA-2026-0002", "闲置资产"),
            buildWarningItem("FA-2026-0003", "闲置资产")
        ));
        when(assetReportMapper.selectMaintenanceWarningItems()).thenReturn(List.of(
            buildWarningItem("FA-2026-0008", "维修中未关闭")
        ));
        when(assetReportMapper.selectPendingApprovalWarningItems()).thenReturn(List.of(
            buildWarningItem("DIS-2026-0001", "待审批关键动作")
        ));
        when(assetReportMapper.selectLandTermExpiringWarningItems(any(Date.class))).thenReturn(List.of(
            buildWarningItem("RE-2026-0001", "土地期限即将到期")
        ));

        AssetReportSummaryVo summary = assetReportService.getSummary();
        AssetWarningVo warnings = assetReportService.getWarnings(30);

        assertAll(
            () -> assertEquals(12L, summary.getFixedAssetCount()),
            () -> assertEquals(new BigDecimal("120000.00"), summary.getFixedAssetOriginalValue()),
            () -> assertEquals(4L, summary.getRealEstateCount()),
            () -> assertEquals(2, summary.getDeptSummaries().size()),
            () -> assertEquals(3, warnings.getIdleAssets().size()),
            () -> assertEquals(1, warnings.getMaintenanceAssets().size()),
            () -> assertEquals(1, warnings.getPendingApprovalItems().size()),
            () -> assertEquals(1, warnings.getLandTermExpiringAssets().size())
        );

        verify(assetReportMapper).selectLandTermExpiringWarningItems(any(Date.class));
    }

    private AssetReportSummaryVo.AssetTypeSummaryItem buildTypeSummary(
        String assetType,
        Long assetCount,
        String originalValueTotal
    ) {
        AssetReportSummaryVo.AssetTypeSummaryItem item = new AssetReportSummaryVo.AssetTypeSummaryItem();
        item.setAssetType(assetType);
        item.setAssetCount(assetCount);
        item.setOriginalValueTotal(new BigDecimal(originalValueTotal));
        return item;
    }

    private AssetReportSummaryVo.AssetStatusSummaryItem buildStatusSummary(
        String assetStatus,
        Long assetCount,
        String originalValueTotal
    ) {
        AssetReportSummaryVo.AssetStatusSummaryItem item = new AssetReportSummaryVo.AssetStatusSummaryItem();
        item.setAssetStatus(assetStatus);
        item.setAssetCount(assetCount);
        item.setOriginalValueTotal(new BigDecimal(originalValueTotal));
        return item;
    }

    private AssetReportSummaryVo.AssetDeptSummaryItem buildDeptSummary(
        Long deptId,
        String deptName,
        Long assetCount,
        String originalValueTotal
    ) {
        AssetReportSummaryVo.AssetDeptSummaryItem item = new AssetReportSummaryVo.AssetDeptSummaryItem();
        item.setDeptId(deptId);
        item.setDeptName(deptName);
        item.setAssetCount(assetCount);
        item.setOriginalValueTotal(new BigDecimal(originalValueTotal));
        return item;
    }

    private AssetWarningVo.AssetWarningItem buildWarningItem(String businessNo, String warningLabel) {
        AssetWarningVo.AssetWarningItem item = new AssetWarningVo.AssetWarningItem();
        item.setBusinessNo(businessNo);
        item.setWarningLabel(warningLabel);
        return item;
    }
}
