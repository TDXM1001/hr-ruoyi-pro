package com.ruoyi.asset.service.impl;

import java.util.List;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetDisposal;
import com.ruoyi.asset.mapper.AssetDisposalMapper;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.service.IAssetDisposalService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.workflow.service.IApprovalEngine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 资产报废服务实现
 */
@Service
public class AssetDisposalServiceImpl implements IAssetDisposalService {
    private static final String ASSET_STATUS_SCRAPPED = "5";
    private static final String ASSET_STATUS_DISPOSED = "6";
    
    @Autowired
    private AssetDisposalMapper assetDisposalMapper;

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Autowired
    private IApprovalEngine approvalEngine;

    @Override
    public AssetDisposal selectAssetDisposalByDisposalNo(String disposalNo) {
        return assetDisposalMapper.selectAssetDisposalByDisposalNo(disposalNo);
    }

    @Override
    public List<AssetDisposal> selectAssetDisposalList(AssetDisposal assetDisposal) {
        return assetDisposalMapper.selectAssetDisposalList(assetDisposal);
    }

    @Transactional
    @Override
    public int insertAssetDisposal(AssetDisposal assetDisposal) {
        assetDisposal.setCreateTime(DateUtils.getNowDate());
        assetDisposal.setStatus(0); // 审批中

        // 处置流程内部统一绑定 assetId，同时保留 assetNo 供页面展示和查询。
        AssetInfo assetInfo = resolveAsset(assetDisposal.getAssetId(), assetDisposal.getAssetNo());
        validateAssetForDisposal(assetInfo);
        assetDisposal.setAssetId(assetInfo.getAssetId());
        assetDisposal.setAssetNo(assetInfo.getAssetNo());
        
        int rows = assetDisposalMapper.insertAssetDisposal(assetDisposal);

        // 状态更新也切换为使用 assetId，避免和业务编号耦合。
        updateAssetStatus(assetInfo.getAssetId(), ASSET_STATUS_SCRAPPED);

        // 发起审批流程
        approvalEngine.startProcess(assetDisposal.getDisposalNo(), "asset_disposal");
        
        return rows;
    }

    private AssetInfo resolveAsset(Long assetId, String assetNo) {
        AssetInfo assetInfo;
        if (assetId != null) {
            assetInfo = assetInfoMapper.selectAssetInfoByAssetId(assetId);
        } else if (StringUtils.isNotBlank(assetNo)) {
            assetInfo = assetInfoMapper.selectAssetInfoByAssetNo(assetNo);
        } else {
            throw new ServiceException("资产ID或资产编号不能为空");
        }
        if (assetInfo == null) {
            throw new ServiceException("关联资产不存在");
        }
        return assetInfo;
    }

    /**
     * 已完成报废或处置的资产不允许重复发起处置流程。
     */
    private void validateAssetForDisposal(AssetInfo assetInfo) {
        if (ASSET_STATUS_DISPOSED.equals(assetInfo.getAssetStatus())) {
            throw new ServiceException("已处置资产不能重复处置");
        }
        if (ASSET_STATUS_SCRAPPED.equals(assetInfo.getAssetStatus())) {
            throw new ServiceException("已报废资产不能重复处置");
        }
    }

    private void updateAssetStatus(Long assetId, String assetStatus) {
        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetId(assetId);
        assetInfo.setAssetStatus(assetStatus);
        assetInfoMapper.updateAssetInfo(assetInfo);
    }
}
