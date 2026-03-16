package com.ruoyi.asset.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import com.ruoyi.asset.domain.AssetAttachment;
import com.ruoyi.asset.domain.AssetAttrValue;
import com.ruoyi.asset.domain.AssetDepreciationLog;
import com.ruoyi.asset.domain.AssetFinance;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetRealEstate;
import com.ruoyi.asset.domain.dto.AssetCreateReq;
import com.ruoyi.asset.domain.dto.AssetUpdateReq;
import com.ruoyi.asset.domain.vo.AssetDetailVo;
import com.ruoyi.asset.domain.vo.AssetListVo;
import com.ruoyi.asset.domain.vo.AssetTimelineVo;
import com.ruoyi.asset.mapper.AssetAttachmentMapper;
import com.ruoyi.asset.mapper.AssetAttrValueMapper;
import com.ruoyi.asset.mapper.AssetDepreciationLogMapper;
import com.ruoyi.asset.mapper.AssetFinanceMapper;
import com.ruoyi.asset.mapper.AssetRealEstateMapper;
import com.ruoyi.asset.mapper.AssetTimelineMapper;
import com.ruoyi.asset.service.IAssetAggregateService;
import com.ruoyi.asset.service.IAssetFinanceService;
import com.ruoyi.asset.service.IAssetInfoService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 资产聚合编排服务。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
@Service
public class AssetAggregateServiceImpl implements IAssetAggregateService {
    private static final String ASSET_TYPE_REAL_ESTATE = "2";
    private static final int RECENT_DEPRECIATION_LOG_LIMIT = 12;
    private static final int RECENT_TIMELINE_LIMIT = 12;

    @Autowired
    private IAssetInfoService assetInfoService;

    @Autowired
    private AssetFinanceMapper assetFinanceMapper;

    @Autowired
    private AssetRealEstateMapper assetRealEstateMapper;

    @Autowired
    private AssetAttrValueMapper assetAttrValueMapper;

    @Autowired
    private AssetAttachmentMapper assetAttachmentMapper;

    @Autowired
    private AssetDepreciationLogMapper assetDepreciationLogMapper;

    @Autowired
    private AssetTimelineMapper assetTimelineMapper;

    @Autowired
    private IAssetFinanceService assetFinanceService;

    /**
     * 查询资产列表。
     */
    @Override
    public List<AssetListVo> selectAssetList(AssetInfo assetInfo) {
        List<AssetInfo> assetInfoList = assetInfoService.selectAssetInfoList(assetInfo);
        List<AssetListVo> result = new ArrayList<>(assetInfoList.size());
        for (AssetInfo info : assetInfoList) {
            result.add(buildAssetListVo(info));
        }
        return result;
    }

    /**
     * 查询资产聚合详情。
     */
    @Override
    public AssetDetailVo selectAssetDetailByAssetId(Long assetId) {
        AssetInfo basicInfo = assetInfoService.selectAssetInfoByAssetId(assetId);
        if (basicInfo == null) {
            return null;
        }
        AssetDetailVo detailVo = new AssetDetailVo();
        detailVo.setBasicInfo(basicInfo);
        detailVo.setFinanceInfo(assetFinanceMapper.selectAssetFinanceByAssetId(assetId));
        detailVo.setRealEstateInfo(assetRealEstateMapper.selectAssetRealEstateByAssetId(assetId));
        detailVo.setDynamicAttrs(assetAttrValueMapper.selectAssetAttrValueByAssetId(assetId));
        detailVo.setAttachments(assetAttachmentMapper.selectAssetAttachmentByAssetId(assetId));
        detailVo.setDepreciationLogs(loadRecentDepreciationLogs(assetId));
        detailVo.setTimeline(loadRecentTimeline(assetId));
        return detailVo;
    }

    /**
     * 新增聚合资产。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertAsset(AssetCreateReq assetCreateReq) {
        validateAssetReq(assetCreateReq, false);
        AssetInfo basicInfo = assetCreateReq.getBasicInfo();
        checkAssetNoUnique(basicInfo.getAssetNo(), null);
        assetInfoService.insertAssetInfo(basicInfo);
        Long assetId = basicInfo.getAssetId();
        replaceFinanceInfo(assetId, assetCreateReq.getFinanceInfo());
        replaceRealEstateInfo(assetId, basicInfo.getAssetType(), assetCreateReq.getRealEstateInfo());
        replaceDynamicAttrs(assetId, basicInfo.getCategoryId(), assetCreateReq.getDynamicAttrs());
        replaceAttachments(assetId, assetCreateReq.getAttachments());
        return 1;
    }

    /**
     * 修改聚合资产。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateAsset(AssetUpdateReq assetUpdateReq) {
        validateAssetReq(assetUpdateReq, true);
        AssetInfo basicInfo = assetUpdateReq.getBasicInfo();
        AssetInfo currentInfo = assetInfoService.selectAssetInfoByAssetId(basicInfo.getAssetId());
        if (currentInfo == null) {
            throw new ServiceException("待修改的资产不存在");
        }
        checkAssetNoUnique(basicInfo.getAssetNo(), basicInfo.getAssetId());
        AssetFinance financeToPersist = prepareFinanceInfoForUpdate(
            assetFinanceMapper.selectAssetFinanceByAssetId(basicInfo.getAssetId()),
            assetUpdateReq.getFinanceInfo()
        );
        assetInfoService.updateAssetInfo(basicInfo);
        if (financeToPersist != null) {
            replaceFinanceInfo(basicInfo.getAssetId(), financeToPersist);
        }
        replaceRealEstateInfo(basicInfo.getAssetId(), basicInfo.getAssetType(), assetUpdateReq.getRealEstateInfo());
        replaceDynamicAttrs(basicInfo.getAssetId(), basicInfo.getCategoryId(), assetUpdateReq.getDynamicAttrs());
        replaceAttachments(basicInfo.getAssetId(), assetUpdateReq.getAttachments());
        return 1;
    }

    /**
     * 构建列表视图，避免对外直接暴露领域对象。
     */
    private AssetListVo buildAssetListVo(AssetInfo assetInfo) {
        AssetListVo listVo = new AssetListVo();
        listVo.setAssetId(assetInfo.getAssetId());
        listVo.setAssetNo(assetInfo.getAssetNo());
        listVo.setAssetName(assetInfo.getAssetName());
        listVo.setCategoryId(assetInfo.getCategoryId());
        listVo.setAssetType(assetInfo.getAssetType());
        listVo.setSpecModel(assetInfo.getSpecModel());
        listVo.setUseDeptId(assetInfo.getUseDeptId());
        listVo.setLocationText(assetInfo.getLocationText());
        listVo.setAssetStatus(assetInfo.getAssetStatus());
        listVo.setPurchaseDate(assetInfo.getPurchaseDate());
        listVo.setCapitalizationDate(assetInfo.getCapitalizationDate());
        return listVo;
    }

    /**
     * 校验聚合请求的必要信息。
     */
    private void validateAssetReq(AssetCreateReq assetReq, boolean update) {
        if (assetReq == null) {
            throw new ServiceException("资产请求体不能为空");
        }
        AssetInfo basicInfo = assetReq.getBasicInfo();
        if (basicInfo == null) {
            throw new ServiceException("资产基础信息不能为空");
        }
        if (update && basicInfo.getAssetId() == null) {
            throw new ServiceException("修改资产时资产ID不能为空");
        }
        if (StringUtils.isBlank(basicInfo.getAssetNo())) {
            throw new ServiceException("资产编号不能为空");
        }
        if (StringUtils.isBlank(basicInfo.getAssetName())) {
            throw new ServiceException("资产名称不能为空");
        }
        if (basicInfo.getCategoryId() == null) {
            throw new ServiceException("资产分类不能为空");
        }
        if (StringUtils.isBlank(basicInfo.getAssetType())) {
            throw new ServiceException("资产类型不能为空");
        }
        if (assetReq.getFinanceInfo() == null) {
            throw new ServiceException("资产财务信息不能为空");
        }
        if (ASSET_TYPE_REAL_ESTATE.equals(basicInfo.getAssetType()) && assetReq.getRealEstateInfo() == null) {
            throw new ServiceException("不动产必须填写不动产信息");
        }
    }

    /**
     * 校验资产编号唯一性，继续保留一物一码规则。
     */
    private void checkAssetNoUnique(String assetNo, Long currentAssetId) {
        AssetInfo assetInfo = assetInfoService.selectAssetInfoByAssetNo(assetNo);
        if (assetInfo != null && !Objects.equals(assetInfo.getAssetId(), currentAssetId)) {
            throw new ServiceException("资产编号已存在，请调整后一物一码编码");
        }
    }

    /**
     * 财务信息按资产维度整体替换，保证主子表更新保持一致。
     */
    private void replaceFinanceInfo(Long assetId, AssetFinance financeInfo) {
        Date now = DateUtils.getNowDate();
        assetFinanceMapper.deleteAssetFinanceByAssetId(assetId);
        financeInfo = assetFinanceService.calculateFinance(financeInfo);
        financeInfo.setFinanceId(null);
        financeInfo.setAssetId(assetId);
        financeInfo.setCreateTime(now);
        financeInfo.setUpdateTime(now);
        if (StringUtils.isBlank(financeInfo.getFinanceStatus())) {
            financeInfo.setFinanceStatus("0");
        }
        if (financeInfo.getVersionNo() == null) {
            financeInfo.setVersionNo(1);
        }
        assetFinanceMapper.insertAssetFinance(financeInfo);
    }

    /**
     * 不动产信息仅对不动产资产生效，其余类型会清理历史记录。
     */
    private void replaceRealEstateInfo(Long assetId, String assetType, AssetRealEstate realEstateInfo) {
        assetRealEstateMapper.deleteAssetRealEstateByAssetId(assetId);
        if (!ASSET_TYPE_REAL_ESTATE.equals(assetType) || realEstateInfo == null) {
            return;
        }
        Date now = DateUtils.getNowDate();
        realEstateInfo.setRealEstateId(null);
        realEstateInfo.setAssetId(assetId);
        realEstateInfo.setCreateTime(now);
        realEstateInfo.setUpdateTime(now);
        assetRealEstateMapper.insertAssetRealEstate(realEstateInfo);
    }

    /**
     * 动态属性在更新时按资产整体覆盖，避免出现脏数据。
     */
    private void replaceDynamicAttrs(Long assetId, Long categoryId, List<AssetAttrValue> dynamicAttrs) {
        assetAttrValueMapper.deleteAssetAttrValueByAssetId(assetId);
        if (dynamicAttrs == null || dynamicAttrs.isEmpty()) {
            return;
        }
        Date now = DateUtils.getNowDate();
        for (AssetAttrValue attrValue : dynamicAttrs) {
            if (attrValue == null) {
                continue;
            }
            attrValue.setValueId(null);
            attrValue.setAssetId(assetId);
            if (attrValue.getCategoryId() == null) {
                attrValue.setCategoryId(categoryId);
            }
            attrValue.setCreateTime(now);
            attrValue.setUpdateTime(now);
            assetAttrValueMapper.insertAssetAttrValue(attrValue);
        }
    }

    /**
     * 附件按资产整体覆盖，确保详情和存量记录一致。
     */
    private void replaceAttachments(Long assetId, List<AssetAttachment> attachments) {
        assetAttachmentMapper.deleteAssetAttachmentByAssetId(assetId);
        if (attachments == null || attachments.isEmpty()) {
            return;
        }
        Date now = DateUtils.getNowDate();
        for (AssetAttachment attachment : attachments) {
            if (attachment == null) {
                continue;
            }
            attachment.setAttachmentId(null);
            attachment.setAssetId(assetId);
            attachment.setCreateTime(now);
            attachment.setUpdateTime(now);
            assetAttachmentMapper.insertAssetAttachment(attachment);
        }
    }

    /**
     * 详情页仅返回最近折旧日志，避免一次加载全部历史记录造成响应过重。
     */
    private List<AssetDepreciationLog> loadRecentDepreciationLogs(Long assetId) {
        List<AssetDepreciationLog> depreciationLogs = assetDepreciationLogMapper.selectAssetDepreciationLogByAssetId(assetId);
        if (depreciationLogs == null || depreciationLogs.size() <= RECENT_DEPRECIATION_LOG_LIMIT) {
            return depreciationLogs;
        }
        return new ArrayList<>(depreciationLogs.subList(0, RECENT_DEPRECIATION_LOG_LIMIT));
    }

    /**
     * 详情页只返回最近动作时间线，既保留关键留痕，也避免把完整历史一次性推给前端。
     */
    private List<AssetTimelineVo> loadRecentTimeline(Long assetId) {
        List<AssetTimelineVo> timeline = assetTimelineMapper.selectAssetTimelineByAssetId(assetId);
        if (timeline == null || timeline.size() <= RECENT_TIMELINE_LIMIT) {
            return timeline;
        }
        return new ArrayList<>(timeline.subList(0, RECENT_TIMELINE_LIMIT));
    }

    /**
     * 已开始折旧的资产不允许通过聚合修改静默改写财务基础数据。
     */
    private AssetFinance prepareFinanceInfoForUpdate(AssetFinance currentFinance, AssetFinance incomingFinance) {
        if (currentFinance == null) {
            return incomingFinance;
        }
        if (!hasDepreciationStarted(currentFinance)) {
            return incomingFinance;
        }
        validateLockedFinanceFields(currentFinance, incomingFinance);
        return null;
    }

    /**
     * 一旦进入折旧周期，只允许保留现有财务快照，避免累计折旧被意外重置。
     */
    private void validateLockedFinanceFields(AssetFinance currentFinance, AssetFinance incomingFinance) {
        if (incomingFinance == null) {
            throw new ServiceException("资产财务信息不能为空");
        }
        if (isBigDecimalChanged(currentFinance.getOriginalValue(), incomingFinance.getOriginalValue())
            || isStringChanged(currentFinance.getDepreciationMethod(), incomingFinance.getDepreciationMethod())
            || !Objects.equals(currentFinance.getUsefulLifeMonth(), incomingFinance.getUsefulLifeMonth())
            || isBigDecimalChanged(currentFinance.getSalvageRate(), incomingFinance.getSalvageRate())
            || !Objects.equals(currentFinance.getDepreciationStartDate(), incomingFinance.getDepreciationStartDate())) {
            throw new ServiceException("资产已开始折旧，不能直接修改财务基础数据");
        }
    }

    /**
     * 通过最近折旧期间、累计折旧和财务状态综合判断是否已经开始折旧。
     */
    private boolean hasDepreciationStarted(AssetFinance assetFinance) {
        return StringUtils.isNotBlank(assetFinance.getLastDepreciationPeriod())
            || defaultZero(assetFinance.getAccumulatedDepreciation()).compareTo(BigDecimal.ZERO) > 0
            || StringUtils.equalsAny(assetFinance.getFinanceStatus(), "1", "2");
    }

    private boolean isBigDecimalChanged(BigDecimal currentValue, BigDecimal incomingValue) {
        return defaultZero(currentValue).compareTo(defaultZero(incomingValue)) != 0;
    }

    private boolean isStringChanged(String currentValue, String incomingValue) {
        return !StringUtils.equals(StringUtils.trimToEmpty(currentValue), StringUtils.trimToEmpty(incomingValue));
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
