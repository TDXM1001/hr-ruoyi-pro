package com.ruoyi.asset.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ruoyi.asset.mapper.AssetRequisitionMapper;
import com.ruoyi.asset.mapper.AssetInfoMapper;
import com.ruoyi.asset.domain.AssetRequisition;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.service.IAssetRequisitionService;
import com.ruoyi.workflow.service.IApprovalEngine;
import com.ruoyi.common.utils.DateUtils;

/**
 * 资产领用服务实现
 */
@Service
public class AssetRequisitionServiceImpl implements IAssetRequisitionService {
    
    @Autowired
    private AssetRequisitionMapper assetRequisitionMapper;

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Autowired
    private IApprovalEngine approvalEngine;

    @Override
    public AssetRequisition selectAssetRequisitionByRequisitionNo(String requisitionNo) {
        return assetRequisitionMapper.selectAssetRequisitionByRequisitionNo(requisitionNo);
    }

    @Override
    public List<AssetRequisition> selectAssetRequisitionList(AssetRequisition assetRequisition) {
        return assetRequisitionMapper.selectAssetRequisitionList(assetRequisition);
    }

    @Transactional
    @Override
    public int insertAssetRequisition(AssetRequisition assetRequisition) {
        // 设置默认值
        assetRequisition.setCreateTime(DateUtils.getNowDate());
        assetRequisition.setStatus(0); // 0=审批中
        
        // 保存领用单
        int rows = assetRequisitionMapper.insertAssetRequisition(assetRequisition);

        // 修改资产状态为"领用中"
        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setAssetNo(assetRequisition.getAssetNo());
        assetInfo.setStatus("2"); // 2=领用中
        assetInfoMapper.updateAssetInfo(assetInfo);

        // 发起审批流程
        approvalEngine.startProcess(assetRequisition.getRequisitionNo(), "asset_requisition");
        
        return rows;
    }
}
