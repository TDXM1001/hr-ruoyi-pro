package com.ruoyi.asset.service.impl;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.asset.domain.AssetChangeLog;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.bo.AssetLedgerBo;
import com.ruoyi.asset.domain.vo.AssetLedgerLifecycleVo;
import com.ruoyi.asset.domain.vo.AssetLedgerVo;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetDisposalMapper;
import com.ruoyi.asset.mapper.AssetHandoverItemMapper;
import com.ruoyi.asset.mapper.AssetInventoryMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.asset.service.IAssetLedgerService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.bean.BeanUtils;

/**
 * 资产台账服务实现。
 *
 * <p>当前实现聚焦固定资产一期正式建账能力，
 * 资产编号由后端统一生成并在通用编辑页冻结，避免前端手填或通用编辑导致主键口径漂移。</p>
 *
 * @author Codex
 */
@Service
public class AssetLedgerServiceImpl implements IAssetLedgerService
{
    private static final String ASSET_CODE_PREFIX = "FA";

    private static final int ASSET_CODE_SERIAL_LENGTH = 4;

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

    /**
     * 查询资产台账列表。
     *
     * @param bo 查询条件
     * @return 台账列表
     */
    @Override
    public List<AssetLedgerVo> selectAssetLedgerList(AssetLedgerBo bo)
    {
        return assetLedgerMapper.selectAssetLedgerList(bo);
    }

    /**
     * 查询资产台账详情。
     *
     * @param assetId 资产ID
     * @return 台账详情
     */
    @Override
    public AssetLedgerVo selectAssetLedgerById(Long assetId)
    {
        return assetLedgerMapper.selectAssetLedgerById(assetId);
    }

    /**
     * 查询资产生命周期聚合详情。
     *
     * @param assetId 资产ID
     * @return 生命周期聚合详情
     */
    @Override
    public AssetLedgerLifecycleVo selectAssetLifecycleById(Long assetId)
    {
        if (assetId == null)
        {
            throw new ServiceException("资产ID不能为空");
        }

        AssetLedgerVo ledger = assetLedgerMapper.selectAssetLedgerById(assetId);
        if (ledger == null)
        {
            throw new ServiceException("资产台账不存在");
        }

        AssetLedgerLifecycleVo lifecycle = new AssetLedgerLifecycleVo();
        lifecycle.setLedger(ledger);
        lifecycle.setHandoverRecords(defaultList(assetHandoverItemMapper.selectAssetHandoverItemsByAssetId(assetId)));
        lifecycle.setInventoryRecords(defaultList(assetInventoryMapper.selectAssetInventoryRecordsByAssetId(assetId)));
        lifecycle.setDisposalRecords(defaultList(assetDisposalMapper.selectAssetDisposalsByAssetId(assetId)));
        lifecycle.setChangeLogs(defaultList(assetChangeLogMapper.selectAssetChangeLogListByAssetId(assetId)));
        return lifecycle;
    }

    /**
     * 获取下一条建议资产编号。
     *
     * @return 建议资产编号
     */
    @Override
    public String getNextAssetCode()
    {
        return generateNextAssetCode();
    }

    /**
     * 新增资产台账。
     *
     * @param bo 台账入参
     * @param operator 操作人
     * @return 新增后的资产ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAsset(AssetLedgerBo bo, String operator)
    {
        assignAssetCodeForCreate(bo);
        validateAssetLedgerBo(bo);
        checkAssetCodeUnique(bo.getAssetCode(), null);

        AssetLedger assetLedger = buildAssetLedgerEntity(bo);
        assetLedger.setAssetStatus(AssetStatus.IN_LEDGER.getCode());
        assetLedger.setCreateBy(operator);

        int rows = assetLedgerMapper.insertAsset(assetLedger);
        if (rows <= 0)
        {
            throw new ServiceException("新增资产台账失败");
        }

        assetChangeLogMapper.insertAssetChangeLog(AssetChangeLog.ofCreate(assetLedger.getAssetId(), operator));
        return assetLedger.getAssetId();
    }

    /**
     * 修改资产台账。
     *
     * <p>通用编辑页不允许修改资产编号，
     * 如需编号重编，应在后续单独建设“编号调整”流程，而不是复用基础编辑接口。</p>
     *
     * @param bo 台账入参
     * @param operator 操作人
     * @return 修改结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAsset(AssetLedgerBo bo, String operator)
    {
        if (bo == null || bo.getAssetId() == null)
        {
            throw new ServiceException("资产ID不能为空");
        }

        AssetLedger current = assetLedgerMapper.selectAssetById(bo.getAssetId());
        if (current == null)
        {
            throw new ServiceException("资产台账不存在");
        }

        bo.setAssetCode(resolveAssetCodeForUpdate(current));
        validateAssetLedgerBo(bo);
        checkAssetCodeUnique(bo.getAssetCode(), bo.getAssetId());

        AssetLedger assetLedger = buildAssetLedgerEntity(bo);
        assetLedger.setAssetId(current.getAssetId());
        assetLedger.setAssetType(StringUtils.defaultIfBlank(current.getAssetType(), assetLedger.getAssetType()));
        assetLedger.setAssetStatus(StringUtils.defaultIfBlank(current.getAssetStatus(), AssetStatus.IN_LEDGER.getCode()));
        // 使用部门、责任人、位置和状态必须通过交接单闭环变更，台账编辑页只允许维护主数据。
        assetLedger.setUseDeptId(current.getUseDeptId());
        assetLedger.setResponsibleUserId(current.getResponsibleUserId());
        assetLedger.setLocationName(current.getLocationName());
        assetLedger.setUpdateBy(operator);

        int rows = assetLedgerMapper.updateAsset(assetLedger);
        if (rows > 0)
        {
            assetChangeLogMapper.insertAssetChangeLog(AssetChangeLog.ofUpdate(assetLedger.getAssetId(),
                current.getAssetStatus(), assetLedger.getAssetStatus(), operator));
        }
        return rows;
    }

    /**
     * 校验台账建账核心字段。
     *
     * @param bo 台账入参
     */
    private void validateAssetLedgerBo(AssetLedgerBo bo)
    {
        if (bo == null)
        {
            throw new ServiceException("资产台账参数不能为空");
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
     * 构建台账主档对象。
     *
     * @param bo 台账入参
     * @return 台账主档
     */
    private AssetLedger buildAssetLedgerEntity(AssetLedgerBo bo)
    {
        AssetLedger assetLedger = new AssetLedger();
        BeanUtils.copyBeanProp(assetLedger, bo);
        assetLedger.setAssetType(StringUtils.defaultIfBlank(bo.getAssetType(), "FIXED"));
        assetLedger.setSourceType(StringUtils.defaultIfBlank(bo.getSourceType(), "MANUAL"));
        return assetLedger;
    }

    /**
     * 创建时统一分配资产编号。
     *
     * <p>当前新增页不再接受前端手填资产编号，
     * 无论前端是否传值，都以后端规则生成正式编号。</p>
     *
     * @param bo 台账入参
     */
    private void assignAssetCodeForCreate(AssetLedgerBo bo)
    {
        if (bo == null)
        {
            return;
        }
        bo.setAssetCode(generateNextAssetCode());
    }

    /**
     * 解析编辑场景应沿用的资产编号。
     *
     * @param current 当前台账
     * @return 最终资产编号
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
     * 生成下一条资产编号。
     *
     * @return 资产编号
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
     * @param currentMaxCode 当前最大编号
     * @param assetCodePrefix 编号前缀
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

    private <T> List<T> defaultList(List<T> records)
    {
        return records == null ? new ArrayList<T>() : records;
    }
}
