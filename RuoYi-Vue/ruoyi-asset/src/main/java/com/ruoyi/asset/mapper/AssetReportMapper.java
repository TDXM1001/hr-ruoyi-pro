package com.ruoyi.asset.mapper;

import java.util.Date;
import java.util.List;
import com.ruoyi.asset.domain.vo.AssetReportSummaryVo;
import com.ruoyi.asset.domain.vo.AssetWarningVo;

/**
 * 资产报表与预警 Mapper。
 */
public interface AssetReportMapper {
    /** 查询资产类型汇总。 */
    public List<AssetReportSummaryVo.AssetTypeSummaryItem> selectAssetTypeSummary();

    /** 查询资产状态汇总。 */
    public List<AssetReportSummaryVo.AssetStatusSummaryItem> selectAssetStatusSummary();

    /** 查询在用资产部门汇总。 */
    public List<AssetReportSummaryVo.AssetDeptSummaryItem> selectDeptSummary();

    /** 查询闲置资产预警。 */
    public List<AssetWarningVo.AssetWarningItem> selectIdleAssetWarnings();

    /** 查询维修中未关闭资产预警。 */
    public List<AssetWarningVo.AssetWarningItem> selectMaintenanceWarningItems();

    /** 查询待审批关键动作预警。 */
    public List<AssetWarningVo.AssetWarningItem> selectPendingApprovalWarningItems();

    /** 查询土地期限临近到期预警。 */
    public List<AssetWarningVo.AssetWarningItem> selectLandTermExpiringWarningItems(Date thresholdDate);
}