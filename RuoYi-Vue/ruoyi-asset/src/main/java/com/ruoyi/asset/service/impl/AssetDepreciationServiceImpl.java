package com.ruoyi.asset.service.impl;

import java.util.List;
import com.ruoyi.asset.domain.AssetDepreciationLog;
import com.ruoyi.asset.domain.AssetFinance;
import com.ruoyi.asset.mapper.AssetDepreciationLogMapper;
import com.ruoyi.asset.mapper.AssetFinanceMapper;
import com.ruoyi.asset.service.IAssetDepreciationService;
import com.ruoyi.asset.service.IAssetFinanceService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 资产折旧服务实现。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
@Service
public class AssetDepreciationServiceImpl implements IAssetDepreciationService {
    @Autowired
    private AssetFinanceMapper assetFinanceMapper;

    @Autowired
    private AssetDepreciationLogMapper assetDepreciationLogMapper;

    @Autowired
    private IAssetFinanceService assetFinanceService;

    @Override
    public List<AssetDepreciationLog> selectAssetDepreciationLogByAssetId(Long assetId) {
        return assetDepreciationLogMapper.selectAssetDepreciationLogByAssetId(assetId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public AssetDepreciationLog accrueDepreciation(Long assetId, String period) {
        if (assetId == null) {
            throw new ServiceException("资产ID不能为空");
        }
        if (StringUtils.isBlank(period)) {
            throw new ServiceException("折旧期间不能为空");
        }
        AssetFinance assetFinance = assetFinanceMapper.selectAssetFinanceByAssetId(assetId);
        if (assetFinance == null) {
            throw new ServiceException("资产财务信息不存在");
        }
        if (assetDepreciationLogMapper.selectAssetDepreciationLogByAssetIdAndPeriod(assetId, period) != null) {
            throw new ServiceException("该期间已执行过折旧计提");
        }
        List<AssetDepreciationLog> depreciationLogs = assetDepreciationLogMapper.selectAssetDepreciationLogByAssetId(assetId);
        int nextPeriod = depreciationLogs.size() + 1;
        if (assetFinance.getUsefulLifeMonth() != null && nextPeriod > assetFinance.getUsefulLifeMonth()) {
            throw new ServiceException("资产已完成全部折旧期间");
        }

        AssetFinance calculatedFinance = assetFinanceService.calculateDepreciatedFinance(assetFinance, nextPeriod);
        AssetDepreciationLog depreciationLog = new AssetDepreciationLog();
        depreciationLog.setAssetId(assetId);
        depreciationLog.setPeriod(period);
        depreciationLog.setDepreciationAmount(calculatedFinance.getMonthlyDepreciationAmount());
        depreciationLog.setAccumulatedDepreciation(calculatedFinance.getAccumulatedDepreciation());
        depreciationLog.setNetBookValue(calculatedFinance.getNetBookValue());
        depreciationLog.setBookValue(calculatedFinance.getBookValue());
        depreciationLog.setCalcTime(DateUtils.getNowDate());
        depreciationLog.setCalcType("MONTHLY");
        depreciationLog.setCreateTime(DateUtils.getNowDate());
        assetDepreciationLogMapper.insertAssetDepreciationLog(depreciationLog);

        calculatedFinance.setLastDepreciationPeriod(period);
        calculatedFinance.setUpdateTime(DateUtils.getNowDate());
        assetFinanceMapper.updateAssetFinance(calculatedFinance);
        return depreciationLog;
    }
}
