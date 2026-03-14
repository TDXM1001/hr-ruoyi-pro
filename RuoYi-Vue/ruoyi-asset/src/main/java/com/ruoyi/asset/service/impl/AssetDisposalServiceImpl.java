package com.ruoyi.asset.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.asset.mapper.AssetDisposalMapper;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.domain.AssetDisposal;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.service.IAssetDisposalService;
import com.ruoyi.workflow.service.IApprovalEngine;
import com.ruoyi.common.utils.DateUtils;

/**
 * 资产报废服务实现
 */
@Service
public class AssetDisposalServiceImpl implements IAssetDisposalService {
    
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
        
        int rows = assetDisposalMapper.insertAssetDisposal(assetDisposal);

        // 修改资产状态为"已报废"审批中（在状态这里，为了测试直接置为5已报废或者4盘点中，因为计划说状态变为“维修中/盘点中/已报废”，这里选5）
        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetNo(assetDisposal.getAssetNo());
        assetInfo.setStatus("5"); // 5=已报废
        assetInfoMapper.updateAssetInfo(assetInfo);

        // 发起审批流程
        approvalEngine.startProcess(assetDisposal.getDisposalNo(), "asset_disposal");
        
        return rows;
    }
}
