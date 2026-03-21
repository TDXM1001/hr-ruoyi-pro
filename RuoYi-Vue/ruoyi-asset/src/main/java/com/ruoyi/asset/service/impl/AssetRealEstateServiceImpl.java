package com.ruoyi.asset.service.impl;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.asset.domain.AssetChangeLog;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.AssetRealEstateProfile;
import com.ruoyi.asset.domain.bo.AssetRealEstateBo;
import com.ruoyi.asset.domain.vo.AssetLedgerLifecycleVo;
import com.ruoyi.asset.domain.vo.AssetLedgerVo;
import com.ruoyi.asset.domain.vo.AssetRealEstateVo;
import com.ruoyi.asset.enums.AssetBizType;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetDisposalMapper;
import com.ruoyi.asset.mapper.AssetHandoverItemMapper;
import com.ruoyi.asset.mapper.AssetInventoryMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.asset.mapper.AssetRectificationMapper;
import com.ruoyi.asset.mapper.AssetRealEstateMapper;
import com.ruoyi.asset.service.IAssetRealEstateService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;

/**
 * 不动产档案服务实现。
 *
 * <p>不动产建档不是只写扩展档案，而是同步写入统一台账和权属扩展档案，
 * 以保证列表、详情、后续审批回写与生命周期追踪使用同一套资产主键。</p>
 *
 * @author Codex
 */
@Service
public class AssetRealEstateServiceImpl implements IAssetRealEstateService
{
    private static final String ASSET_TYPE_REAL_ESTATE = "REAL_ESTATE";

    private static final String ASSET_CODE_PREFIX = "RE";

    private static final int ASSET_CODE_SERIAL_LENGTH = 4;

    @Autowired
    private AssetRealEstateMapper assetRealEstateMapper;

    @Autowired
    private AssetLedgerMapper assetLedgerMapper;

    @Autowired
    private AssetChangeLogMapper assetChangeLogMapper;

    @Autowired
    private AssetHandoverItemMapper assetHandoverItemMapper;

    @Autowired
    private AssetInventoryMapper assetInventoryMapper;

    @Autowired
    private AssetDisposalMapper assetDisposalMapper;

    @Autowired
    private AssetRectificationMapper assetRectificationMapper;

    /**
     * 查询不动产档案列表。
     *
     * @param query 查询条件
     * @return 档案列表
     */
    @Override
    public List<AssetRealEstateVo> selectList(AssetRealEstateBo query)
    {
        return assetRealEstateMapper.selectList(query);
    }

    /**
     * 查询不动产档案详情。
     *
     * @param assetId 资产ID
     * @return 档案详情
     */
    @Override
    public AssetRealEstateVo selectDetailByAssetId(Long assetId)
    {
        if (assetId == null)
        {
            throw new ServiceException("资产ID不能为空");
        }

        AssetRealEstateVo detail = assetRealEstateMapper.selectDetailByAssetId(assetId);
        if (detail == null)
        {
            throw new ServiceException("不动产档案不存在");
        }
        return detail;
    }

    /**
     * 按资产ID查询权属扩展档案。
     *
     * @param assetId 资产ID
     * @return 权属档案
     */
    @Override
    public AssetRealEstateProfile selectByAssetId(Long assetId)
    {
        if (assetId == null)
        {
            throw new ServiceException("资产ID不能为空");
        }
        return assetRealEstateMapper.selectByAssetId(assetId);
    }

    /**
     * 查询不动产生命周期详情。
     *
     * @param assetId 资产ID
     * @return 生命周期详情
     */
    @Override
    public AssetLedgerLifecycleVo selectLifecycleByAssetId(Long assetId)
    {
        AssetRealEstateVo detail = selectDetailByAssetId(assetId);
        AssetLedgerVo ledger = assetLedgerMapper.selectAssetLedgerById(detail.getAssetId());
        if (ledger == null)
        {
            throw new ServiceException("不动产台账不存在");
        }

        AssetLedgerLifecycleVo lifecycle = new AssetLedgerLifecycleVo();
        lifecycle.setLedger(ledger);
        lifecycle.setHandoverRecords(defaultList(assetHandoverItemMapper.selectAssetHandoverItemsByAssetId(assetId)));
        lifecycle.setInventoryRecords(defaultList(assetInventoryMapper.selectAssetInventoryRecordsByAssetId(assetId)));
        lifecycle.setRectificationOrders(defaultList(assetRectificationMapper.selectAssetRectificationListByAssetId(assetId)));
        lifecycle.setDisposalRecords(defaultList(assetDisposalMapper.selectAssetDisposalsByAssetId(assetId)));
        lifecycle.setChangeLogs(defaultList(assetChangeLogMapper.selectAssetChangeLogListByAssetId(assetId)));
        return lifecycle;
    }

    /**
     * 获取下一条建议资产编码。
     *
     * @return 建议资产编码
     */
    @Override
    public String getNextAssetCode()
    {
        return generateNextAssetCode();
    }

    /**
     * 新增不动产档案。
     *
     * @param bo 建档入参
     * @param operator 操作人
     * @return 新增后的资产ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAsset(AssetRealEstateBo bo, String operator)
    {
        assignAssetCodeForCreate(bo);
        validateRealEstateBo(bo);
        checkAssetCodeUnique(bo.getAssetCode(), null);

        AssetLedger assetLedger = buildAssetLedgerEntity(bo);
        assetLedger.setAssetType(ASSET_TYPE_REAL_ESTATE);
        assetLedger.setAssetStatus(resolveCreateStatus(bo));
        assetLedger.setCreateBy(operator);

        int ledgerRows = assetLedgerMapper.insertAsset(assetLedger);
        if (ledgerRows <= 0)
        {
            throw new ServiceException("新增不动产台账失败");
        }

        AssetRealEstateProfile profile = buildProfileEntity(bo);
        profile.setAssetId(assetLedger.getAssetId());
        profile.setCreateBy(operator);
        int profileRows = assetRealEstateMapper.insertProfile(profile);
        if (profileRows <= 0)
        {
            throw new ServiceException("新增不动产权属档案失败");
        }

        assetChangeLogMapper.insertAssetChangeLog(buildChangeLog(assetLedger.getAssetId(), AssetBizType.LEDGER_CREATE.getCode(),
            null, assetLedger.getAssetStatus(), operator, "新增不动产档案"));
        return assetLedger.getAssetId();
    }

    /**
     * 修改不动产档案。
     *
     * @param bo 编辑入参
     * @param operator 操作人
     * @return 修改结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAsset(AssetRealEstateBo bo, String operator)
    {
        if (bo == null || bo.getAssetId() == null)
        {
            throw new ServiceException("资产ID不能为空");
        }

        AssetLedger current = assetLedgerMapper.selectAssetById(bo.getAssetId());
        if (current == null)
        {
            throw new ServiceException("不动产台账不存在");
        }
        if (!ASSET_TYPE_REAL_ESTATE.equals(StringUtils.defaultIfBlank(current.getAssetType(), ASSET_TYPE_REAL_ESTATE)))
        {
            throw new ServiceException("当前资产不是不动产档案");
        }

        bo.setAssetCode(resolveAssetCodeForUpdate(current));
        validateRealEstateBo(bo);
        checkAssetCodeUnique(bo.getAssetCode(), bo.getAssetId());

        AssetLedger assetLedger = buildAssetLedgerEntity(bo);
        assetLedger.setAssetId(current.getAssetId());
        assetLedger.setAssetType(ASSET_TYPE_REAL_ESTATE);
        assetLedger.setAssetStatus(resolveUpdateStatus(current));
        assetLedger.setUpdateBy(operator);

        int ledgerRows = assetLedgerMapper.updateAsset(assetLedger);
        if (ledgerRows <= 0)
        {
            return 0;
        }

        AssetRealEstateProfile currentProfile = assetRealEstateMapper.selectByAssetId(current.getAssetId());
        AssetRealEstateProfile profile = buildProfileEntity(bo);
        profile.setAssetId(current.getAssetId());
        profile.setUpdateBy(operator);

        int profileRows;
        if (currentProfile == null)
        {
            profile.setCreateBy(operator);
            profileRows = assetRealEstateMapper.insertProfile(profile);
        }
        else
        {
            profile.setProfileId(currentProfile.getProfileId());
            profileRows = assetRealEstateMapper.updateProfile(profile);
        }

        if (profileRows <= 0)
        {
            throw new ServiceException("修改不动产权属档案失败");
        }

        assetChangeLogMapper.insertAssetChangeLog(buildChangeLog(assetLedger.getAssetId(), AssetBizType.LEDGER_UPDATE.getCode(),
            current.getAssetStatus(), assetLedger.getAssetStatus(), operator, "修改不动产档案"));
        return ledgerRows;
    }

    /**
     * 校验不动产建档核心字段。
     *
     * @param bo 建档入参
     */
    private void validateRealEstateBo(AssetRealEstateBo bo)
    {
        if (bo == null)
        {
            throw new ServiceException("不动产档案参数不能为空");
        }
        if (StringUtils.isBlank(bo.getAssetCode()))
        {
            throw new ServiceException("资产编码不能为空");
        }
        if (StringUtils.isBlank(bo.getAssetName()))
        {
            throw new ServiceException("资产名称不能为空");
        }
        if (bo.getCategoryId() == null)
        {
            throw new ServiceException("资产分类不能为空");
        }
        if (bo.getOwnerDeptId() == null)
        {
            throw new ServiceException("权属部门不能为空");
        }
        if (StringUtils.isBlank(bo.getSourceType()))
        {
            throw new ServiceException("录入来源不能为空");
        }
        if (StringUtils.isBlank(bo.getOwnershipCertNo()))
        {
            throw new ServiceException("权属证号不能为空");
        }
        if (StringUtils.isBlank(bo.getLandUseType()))
        {
            throw new ServiceException("土地用途不能为空");
        }
        if (bo.getBuildingArea() == null)
        {
            throw new ServiceException("建筑面积不能为空");
        }
    }

    /**
     * 校验资产编码唯一性。
     *
     * @param assetCode 资产编码
     * @param assetId 当前资产ID
     */
    private void checkAssetCodeUnique(String assetCode, Long assetId)
    {
        AssetLedger existed = assetLedgerMapper.selectByAssetCode(assetCode);
        Long currentAssetId = StringUtils.nvl(assetId, -1L);
        if (existed != null && !currentAssetId.equals(existed.getAssetId()))
        {
            throw new ServiceException("资产编码已存在");
        }
    }

    /**
     * 构建统一台账实体。
     *
     * @param bo 建档入参
     * @return 台账实体
     */
    private AssetLedger buildAssetLedgerEntity(AssetRealEstateBo bo)
    {
        AssetLedger assetLedger = new AssetLedger();
        BeanUtils.copyBeanProp(assetLedger, bo);
        assetLedger.setAssetType(ASSET_TYPE_REAL_ESTATE);
        assetLedger.setSourceType(StringUtils.defaultIfBlank(bo.getSourceType(), "MANUAL"));
        return assetLedger;
    }

    /**
     * 构建权属扩展档案实体。
     *
     * @param bo 建档入参
     * @return 权属档案实体
     */
    private AssetRealEstateProfile buildProfileEntity(AssetRealEstateBo bo)
    {
        AssetRealEstateProfile profile = new AssetRealEstateProfile();
        BeanUtils.copyBeanProp(profile, bo);
        return profile;
    }

    /**
     * 创建动作统一由后端分配编码，避免前端手填。
     *
     * @param bo 建档入参
     */
    private void assignAssetCodeForCreate(AssetRealEstateBo bo)
    {
        if (bo == null)
        {
            return;
        }
        bo.setAssetCode(generateNextAssetCode());
    }

    /**
     * 解析编辑场景应沿用的资产编码。
     *
     * @param current 当前台账
     * @return 最终资产编码
     */
    private String resolveAssetCodeForUpdate(AssetLedger current)
    {
        if (current == null || StringUtils.isBlank(current.getAssetCode()))
        {
            return generateNextAssetCode();
        }
        return StringUtils.trim(current.getAssetCode());
    }

    /**
     * 解析新建时的资产状态。
     *
     * @param bo 建档入参
     * @return 资产状态
     */
    private String resolveCreateStatus(AssetRealEstateBo bo)
    {
        return StringUtils.defaultIfBlank(bo.getAssetStatus(), AssetStatus.IN_USE.getCode());
    }

    /**
     * 编辑场景沿用当前状态，避免档案维护直接篡改资产流转口径。
     *
     * @param current 当前台账
     * @return 资产状态
     */
    private String resolveUpdateStatus(AssetLedger current)
    {
        return StringUtils.defaultIfBlank(current.getAssetStatus(), AssetStatus.IN_USE.getCode());
    }

    /**
     * 生成下一条不动产编码。
     *
     * @return 资产编码
     */
    private String generateNextAssetCode()
    {
        String currentYear = String.valueOf(Year.now().getValue());
        String assetCodePrefix = ASSET_CODE_PREFIX + "-" + currentYear + "-";
        String currentMaxCode = assetLedgerMapper.selectMaxAssetCodeByPrefix(assetCodePrefix);
        int nextSerial = parseNextSerial(currentMaxCode, assetCodePrefix);
        return assetCodePrefix + String.format("%0" + ASSET_CODE_SERIAL_LENGTH + "d", nextSerial);
    }

    /**
     * 解析下一条流水号。
     *
     * @param currentMaxCode 当前最大编码
     * @param assetCodePrefix 编码前缀
     * @return 下一条流水号
     */
    private int parseNextSerial(String currentMaxCode, String assetCodePrefix)
    {
        if (StringUtils.isBlank(currentMaxCode) || !currentMaxCode.startsWith(assetCodePrefix))
        {
            return 1;
        }

        String serialPart = currentMaxCode.substring(assetCodePrefix.length());
        if (!StringUtils.isNumeric(serialPart))
        {
            return 1;
        }
        return Integer.parseInt(serialPart) + 1;
    }

    /**
     * 构建不动产档案变更日志。
     *
     * @param assetId 资产ID
     * @param bizType 业务类型
     * @param beforeStatus 变更前状态
     * @param afterStatus 变更后状态
     * @param operator 操作人
     * @param changeDesc 变更说明
     * @return 变更日志
     */
    private AssetChangeLog buildChangeLog(Long assetId, String bizType, String beforeStatus,
        String afterStatus, String operator, String changeDesc)
    {
        AssetChangeLog changeLog = new AssetChangeLog();
        changeLog.setAssetId(assetId);
        changeLog.setBizType(bizType);
        changeLog.setBeforeStatus(beforeStatus);
        changeLog.setAfterStatus(afterStatus);
        changeLog.setOperateBy(operator);
        changeLog.setOperateTime(DateUtils.getNowDate());
        changeLog.setChangeDesc(changeDesc);
        return changeLog;
    }

    private <T> List<T> defaultList(List<T> records)
    {
        return records == null ? new ArrayList<T>() : records;
    }
}
