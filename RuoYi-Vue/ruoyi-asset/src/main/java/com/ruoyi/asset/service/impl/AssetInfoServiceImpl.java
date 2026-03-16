package com.ruoyi.asset.service.impl;

import java.util.Date;
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
import com.ruoyi.asset.service.IAssetInfoService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 资产主档Service业务层处理
 *
 * @author ruoyi
 * @date 2026-03-14
 */
@Service
public class AssetInfoServiceImpl implements IAssetInfoService {
    private static final String ASSET_STATUS_ACTIVE = "1";
    private static final String ASSET_STATUS_IDLE = "7";
    private static final String ASSET_STATUS_SCRAPPED = "5";
    private static final String ASSET_STATUS_DISPOSED = "6";
    private static final String DOC_STATUS_PENDING = "pending";
    private static final String DEL_FLAG_ARCHIVED = "2";

    @Autowired
    private AssetInfoMapper assetInfoMapper;

    @Autowired
    private AssetRequisitionMapper assetRequisitionMapper;

    @Autowired
    private AssetMaintenanceMapper assetMaintenanceMapper;

    @Autowired
    private AssetDisposalMapper assetDisposalMapper;

    @Autowired
    private AssetRealEstateOwnershipChangeMapper assetRealEstateOwnershipChangeMapper;

    @Autowired
    private AssetRealEstateDisposalMapper assetRealEstateDisposalMapper;

    /**
     * 按资产ID查询资产主档。
     *
     * @param assetId 资产ID
     * @return 资产主档
     */
    @Override
    public AssetInfo selectAssetInfoByAssetId(Long assetId) {
        return assetInfoMapper.selectAssetInfoByAssetId(assetId);
    }

    /**
     * 按资产编号查询资产主档。
     *
     * @param assetNo 资产编号
     * @return 资产主档
     */
    @Override
    public AssetInfo selectAssetInfoByAssetNo(String assetNo) {
        return assetInfoMapper.selectAssetInfoByAssetNo(assetNo);
    }

    /**
     * 查询资产主档列表。
     *
     * @param assetInfo 查询条件
     * @return 资产主档集合
     */
    @Override
    public List<AssetInfo> selectAssetInfoList(AssetInfo assetInfo) {
        return assetInfoMapper.selectAssetInfoList(assetInfo);
    }

    /**
     * 新增资产主档。
     *
     * @param assetInfo 资产主档
     * @return 结果
     */
    @Override
    public int insertAssetInfo(AssetInfo assetInfo) {
        normalizeAssetInfo(assetInfo);
        assetInfo.setCreateTime(DateUtils.getNowDate());
        assetInfo.setUpdateTime(DateUtils.getNowDate());
        return assetInfoMapper.insertAssetInfo(assetInfo);
    }

    /**
     * 修改资产主档。
     *
     * @param assetInfo 资产主档
     * @return 结果
     */
    @Override
    public int updateAssetInfo(AssetInfo assetInfo) {
        normalizeAssetInfo(assetInfo);
        assetInfo.setUpdateTime(DateUtils.getNowDate());
        return assetInfoMapper.updateAssetInfo(assetInfo);
    }

    /**
     * 批量归档资产主档。
     *
     * @param assetIds 资产ID集合
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAssetInfoByAssetIds(Long[] assetIds) {
        if (assetIds == null || assetIds.length == 0) {
            return 0;
        }

        for (Long assetId : assetIds) {
            AssetInfo assetInfo = assetInfoMapper.selectAssetInfoByAssetId(assetId);
            if (assetInfo == null) {
                throw new ServiceException("待归档的资产不存在");
            }
            validateArchiveAllowed(assetInfo);
        }

        int rows = 0;
        Date now = DateUtils.getNowDate();
        for (Long assetId : assetIds) {
            AssetInfo archivePayload = new AssetInfo();
            archivePayload.setAssetId(assetId);
            archivePayload.setDelFlag(DEL_FLAG_ARCHIVED);
            archivePayload.setUpdateTime(now);
            rows += assetInfoMapper.updateAssetInfo(archivePayload);
        }
        return rows;
    }

    /**
     * 归档单个资产主档。
     *
     * @param assetId 资产ID
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAssetInfoByAssetId(Long assetId) {
        return deleteAssetInfoByAssetIds(new Long[]{assetId});
    }

    /**
     * 归档前统一收口生命周期限制，避免“删除”破坏业务审计链路。
     */
    private void validateArchiveAllowed(AssetInfo assetInfo) {
        if (DEL_FLAG_ARCHIVED.equals(assetInfo.getDelFlag())) {
            throw new ServiceException("资产已归档，无需重复归档");
        }
        if (ASSET_STATUS_SCRAPPED.equals(assetInfo.getAssetStatus())) {
            throw new ServiceException("已报废资产不能归档，请保留生命周期审计记录");
        }
        if (ASSET_STATUS_DISPOSED.equals(assetInfo.getAssetStatus())) {
            throw new ServiceException("已处置资产不能归档，请保留生命周期审计记录");
        }
        if (StringUtils.isNotBlank(assetInfo.getAssetStatus())
            && !StringUtils.equalsAny(assetInfo.getAssetStatus(), ASSET_STATUS_ACTIVE, ASSET_STATUS_IDLE)) {
            throw new ServiceException("当前资产状态不允许归档，请先结束领用、维修或盘点流程");
        }
        if (hasBlockingBusinessDocuments(assetInfo.getAssetId())) {
            throw new ServiceException("资产存在未完成的领用、维修、处置或不动产动作单据，不能归档");
        }
    }

    /**
     * 只要还有未闭环单据，就不允许把主档移出日常台账。
     */
    private boolean hasBlockingBusinessDocuments(Long assetId) {
        return hasBlockingRequisition(assetId)
            || hasBlockingMaintenance(assetId)
            || hasBlockingDisposal(assetId)
            || hasBlockingRealEstateAction(assetId);
    }

    private boolean hasBlockingRequisition(Long assetId) {
        AssetRequisition query = new AssetRequisition();
        query.setAssetId(assetId);
        return assetRequisitionMapper.selectAssetRequisitionList(query).stream()
            .anyMatch(item -> item != null && item.getStatus() != null && item.getStatus() != 2 && item.getStatus() != 3);
    }

    private boolean hasBlockingMaintenance(Long assetId) {
        AssetMaintenance query = new AssetMaintenance();
        query.setAssetId(assetId);
        return assetMaintenanceMapper.selectAssetMaintenanceList(query).stream()
            .anyMatch(item -> item != null && item.getStatus() != null && item.getStatus() != 2 && item.getStatus() != 3);
    }

    private boolean hasBlockingDisposal(Long assetId) {
        AssetDisposal query = new AssetDisposal();
        query.setAssetId(assetId);
        return assetDisposalMapper.selectAssetDisposalList(query).stream()
            .anyMatch(item -> item != null && item.getStatus() != null && item.getStatus() == 0);
    }

    private boolean hasBlockingRealEstateAction(Long assetId) {
        AssetRealEstateOwnershipChange ownershipChange = new AssetRealEstateOwnershipChange();
        ownershipChange.setAssetId(assetId);
        ownershipChange.setStatus(DOC_STATUS_PENDING);

        AssetRealEstateDisposal disposal = new AssetRealEstateDisposal();
        disposal.setAssetId(assetId);
        disposal.setStatus(DOC_STATUS_PENDING);

        return !assetRealEstateOwnershipChangeMapper.selectOwnershipChangeList(ownershipChange).isEmpty()
            || !assetRealEstateDisposalMapper.selectDisposalList(disposal).isEmpty();
    }

    /**
     * 统一整理资产主档字段，减少脏值写入。
     */
    private void normalizeAssetInfo(AssetInfo assetInfo) {
        if (assetInfo == null) {
            return;
        }
        assetInfo.setAssetNo(StringUtils.trimToNull(assetInfo.getAssetNo()));
        assetInfo.setAssetName(StringUtils.trimToNull(assetInfo.getAssetName()));
        assetInfo.setSpecModel(StringUtils.trimToNull(assetInfo.getSpecModel()));
        assetInfo.setUnit(StringUtils.trimToNull(assetInfo.getUnit()));
        assetInfo.setLocationText(StringUtils.trimToNull(assetInfo.getLocationText()));
        assetInfo.setAcquireMethod(StringUtils.trimToNull(assetInfo.getAcquireMethod()));
        if (StringUtils.isBlank(assetInfo.getAssetStatus())) {
            assetInfo.setAssetStatus("1");
        }
    }
}
