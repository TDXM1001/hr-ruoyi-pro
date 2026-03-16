package com.ruoyi.asset.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.ruoyi.asset.domain.vo.AssetReportSummaryVo;
import com.ruoyi.asset.domain.vo.AssetWarningVo;
import com.ruoyi.asset.mapper.AssetReportMapper;
import com.ruoyi.asset.service.IAssetReportService;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 资产报表与预警服务实现。
 */
@Service
public class AssetReportServiceImpl implements IAssetReportService {
    private static final String ASSET_TYPE_FIXED = "1";
    private static final String ASSET_TYPE_REAL_ESTATE = "2";
    private static final int DEFAULT_LAND_TERM_WITHIN_DAYS = 90;

    @Autowired
    private AssetReportMapper assetReportMapper;

    @Override
    public AssetReportSummaryVo getSummary() {
        AssetReportSummaryVo summary = new AssetReportSummaryVo();
        List<AssetReportSummaryVo.AssetTypeSummaryItem> typeSummaries = safeList(assetReportMapper.selectAssetTypeSummary());
        summary.setTypeSummaries(typeSummaries);
        summary.setStatusSummaries(safeList(assetReportMapper.selectAssetStatusSummary()));
        summary.setDeptSummaries(safeList(assetReportMapper.selectDeptSummary()));

        long totalAssetCount = 0L;
        BigDecimal totalOriginalValue = BigDecimal.ZERO;
        for (AssetReportSummaryVo.AssetTypeSummaryItem item : typeSummaries) {
            long assetCount = defaultLong(item.getAssetCount());
            BigDecimal originalValueTotal = defaultZero(item.getOriginalValueTotal());
            totalAssetCount += assetCount;
            totalOriginalValue = totalOriginalValue.add(originalValueTotal);
            if (ASSET_TYPE_FIXED.equals(item.getAssetType())) {
                summary.setFixedAssetCount(assetCount);
                summary.setFixedAssetOriginalValue(originalValueTotal);
            }
            if (ASSET_TYPE_REAL_ESTATE.equals(item.getAssetType())) {
                summary.setRealEstateCount(assetCount);
                summary.setRealEstateOriginalValue(originalValueTotal);
            }
        }
        summary.setTotalAssetCount(totalAssetCount);
        summary.setTotalOriginalValue(totalOriginalValue);
        return summary;
    }

    @Override
    public AssetWarningVo getWarnings(Integer landTermWithinDays) {
        AssetWarningVo warningVo = new AssetWarningVo();
        warningVo.setIdleAssets(safeList(assetReportMapper.selectIdleAssetWarnings()));
        warningVo.setMaintenanceAssets(safeList(assetReportMapper.selectMaintenanceWarningItems()));
        warningVo.setPendingApprovalItems(safeList(assetReportMapper.selectPendingApprovalWarningItems()));

        Date thresholdDate = DateUtils.addDays(DateUtils.getNowDate(), normalizeLandTermWithinDays(landTermWithinDays));
        warningVo.setLandTermExpiringAssets(safeList(assetReportMapper.selectLandTermExpiringWarningItems(thresholdDate)));
        return warningVo;
    }

    /**
     * 页面未显式传入天数时，默认按 90 天到期预警处理。
     */
    private int normalizeLandTermWithinDays(Integer landTermWithinDays) {
        if (landTermWithinDays == null || landTermWithinDays <= 0) {
            return DEFAULT_LAND_TERM_WITHIN_DAYS;
        }
        return landTermWithinDays;
    }

    private <T> List<T> safeList(List<T> items) {
        return items == null ? new ArrayList<>() : items;
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}