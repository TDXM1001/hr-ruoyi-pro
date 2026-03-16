package com.ruoyi.asset.service;

import com.ruoyi.asset.domain.vo.AssetReportSummaryVo;
import com.ruoyi.asset.domain.vo.AssetWarningVo;

/**
 * 资产报表服务接口。
 */
public interface IAssetReportService {
    /** 查询报表汇总。 */
    public AssetReportSummaryVo getSummary();

    /** 查询预警视图。 */
    public AssetWarningVo getWarnings(Integer landTermWithinDays);
}