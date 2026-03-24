package com.ruoyi.asset.service.impl;

import java.time.Year;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.asset.domain.AssetApprovalRecord;
import com.ruoyi.asset.domain.AssetChangeLog;
import com.ruoyi.asset.domain.AssetDisposal;
import com.ruoyi.asset.domain.AssetLedger;
import com.ruoyi.asset.domain.bo.AssetApprovalActionBo;
import com.ruoyi.asset.domain.bo.AssetDisposalBo;
import com.ruoyi.asset.domain.vo.AssetDisposalVo;
import com.ruoyi.asset.enums.AssetApprovalStatus;
import com.ruoyi.asset.enums.AssetApprovalType;
import com.ruoyi.asset.enums.AssetStatus;
import com.ruoyi.asset.mapper.AssetApprovalMapper;
import com.ruoyi.asset.mapper.AssetChangeLogMapper;
import com.ruoyi.asset.mapper.AssetDisposalMapper;
import com.ruoyi.asset.mapper.AssetLedgerMapper;
import com.ruoyi.asset.service.AssetApprovalStatusMatrix;
import com.ruoyi.asset.service.AssetStatusMachine;
import com.ruoyi.asset.service.IAssetDisposalService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 资产处置服务实现。
 *
 * @author Codex
 */
@Service
public class AssetDisposalServiceImpl implements IAssetDisposalService
{
    private static final String DISPOSAL_NO_PREFIX = "DP";

    private static final int DISPOSAL_NO_SERIAL_LENGTH = 4;

    private static final String DISPOSAL_STATUS_SUBMITTED = "SUBMITTED";

    private static final String DISPOSAL_STATUS_APPROVED = "APPROVED";

    private static final String DISPOSAL_STATUS_REJECTED = "REJECTED";

    @Autowired
    private AssetDisposalMapper assetDisposalMapper;

    @Autowired
    private AssetLedgerMapper assetLedgerMapper;

    @Autowired
    private AssetChangeLogMapper assetChangeLogMapper;

    @Autowired
    private AssetStatusMachine assetStatusMachine;

    @Autowired
    private AssetApprovalMapper assetApprovalMapper;

    @Autowired
    private AssetApprovalStatusMatrix assetApprovalStatusMatrix;

    /**
     * 查询处置列表。
     *
     * @param bo 查询参数
     * @return 处置列表
     */
    @Override
    public List<AssetDisposalVo> selectAssetDisposalList(AssetDisposalBo bo)
    {
        return assetDisposalMapper.selectAssetDisposalList(bo);
    }

    /**
     * 查询处置详情。
     *
     * @param disposalId 处置ID
     * @return 处置详情
     */
    @Override
    public AssetDisposalVo selectAssetDisposalById(Long disposalId)
    {
        if (disposalId == null)
        {
            throw new ServiceException("处置ID不能为空");
        }
        return assetDisposalMapper.selectAssetDisposalById(disposalId);
    }

    /**
     * 提交资产处置审批。
     *
     * @param bo 处置参数
     * @param operator 操作人
     * @return 处置ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitDisposalApproval(AssetDisposalBo bo, String operator)
    {
        validateConfirmParams(bo);

        AssetLedger asset = assetLedgerMapper.selectAssetById(bo.getAssetId());
        if (asset == null)
        {
            throw new ServiceException("资产不存在");
        }

        AssetStatus beforeStatus = parseAssetStatus(asset.getAssetStatus());
        if (!AssetStatus.PENDING_DISPOSAL.equals(beforeStatus))
        {
            throw new ServiceException("只有待处置资产才能提交处置审批");
        }

        Date now = DateUtils.getNowDate();
        AssetDisposal disposal = buildDisposalEntity(bo, operator);
        int disposalRows = assetDisposalMapper.insertAssetDisposal(disposal);
        if (disposalRows <= 0)
        {
            throw new ServiceException("新增处置单失败");
        }

        insertApprovalRecord(bo.getAssetId(), disposal.getDisposalId(), AssetApprovalStatus.SUBMITTED, operator,
            "提交处置审批");

        assetChangeLogMapper.insertAssetChangeLog(AssetChangeLog.ofDisposalApply(bo.getAssetId(),
            disposal.getDisposalId(), beforeStatus.getCode(), beforeStatus.getCode(), operator));

        return disposal.getDisposalId();
    }

    /**
     * 审批通过处置单。
     *
     * @param disposalId 处置单ID
     * @param bo 审批动作
     * @param operator 审批人
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveDisposal(Long disposalId, AssetApprovalActionBo bo, String operator)
    {
        AssetDisposalVo disposal = getSubmittedDisposal(disposalId);
        AssetLedger asset = assetLedgerMapper.selectAssetById(disposal.getAssetId());
        if (asset == null)
        {
            throw new ServiceException("资产不存在");
        }

        AssetStatus beforeStatus = parseAssetStatus(asset.getAssetStatus());
        AssetStatus afterStatus = assetApprovalStatusMatrix.mapToAssetStatus(AssetApprovalType.DISPOSAL,
            AssetApprovalStatus.APPROVED);
        if (!AssetStatus.PENDING_DISPOSAL.equals(beforeStatus))
        {
            throw new ServiceException("只有待处置资产才能审批通过");
        }
        if (!assetStatusMachine.canTransit(beforeStatus, afterStatus))
        {
            throw new ServiceException("资产状态不允许审批通过处置");
        }

        Date now = DateUtils.getNowDate();
        int disposalRows = assetDisposalMapper.updateDisposalApprovalResult(disposalId, DISPOSAL_STATUS_APPROVED,
            operator, now);
        if (disposalRows <= 0)
        {
            throw new ServiceException("更新处置审批结果失败");
        }

        int updateRows = assetLedgerMapper.updateStatus(disposal.getAssetId(), afterStatus.getCode());
        if (updateRows <= 0)
        {
            throw new ServiceException("回写资产处置状态失败");
        }

        insertApprovalRecord(disposal.getAssetId(), disposalId, AssetApprovalStatus.APPROVED, operator,
            StringUtils.trim(bo.getOpinion()));
        assetChangeLogMapper.insertAssetChangeLog(AssetChangeLog.ofDisposalConfirm(disposal.getAssetId(), disposalId,
            beforeStatus.getCode(), afterStatus.getCode(), operator));
    }

    /**
     * 驳回处置单。
     *
     * @param disposalId 处置单ID
     * @param bo 审批动作
     * @param operator 审批人
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectDisposal(Long disposalId, AssetApprovalActionBo bo, String operator)
    {
        AssetDisposalVo disposal = getSubmittedDisposal(disposalId);
        AssetLedger asset = assetLedgerMapper.selectAssetById(disposal.getAssetId());
        if (asset == null)
        {
            throw new ServiceException("资产不存在");
        }

        AssetStatus beforeStatus = parseAssetStatus(asset.getAssetStatus());
        Date now = DateUtils.getNowDate();
        int disposalRows = assetDisposalMapper.updateDisposalApprovalResult(disposalId, DISPOSAL_STATUS_REJECTED,
            operator, now);
        if (disposalRows <= 0)
        {
            throw new ServiceException("更新处置审批结果失败");
        }

        insertApprovalRecord(disposal.getAssetId(), disposalId, AssetApprovalStatus.REJECTED, operator,
            StringUtils.trim(bo.getOpinion()));
        assetChangeLogMapper.insertAssetChangeLog(AssetChangeLog.ofDisposalReject(disposal.getAssetId(), disposalId,
            beforeStatus.getCode(), beforeStatus.getCode(), operator));
    }

    /**
     * 查询处置审批轨迹。
     *
     * @param disposalId 处置单ID
     * @return 审批轨迹
     */
    @Override
    public List<AssetApprovalRecord> selectDisposalApprovalRecords(Long disposalId)
    {
        if (disposalId == null)
        {
            throw new ServiceException("处置单ID不能为空");
        }
        return assetApprovalMapper.selectAssetApprovalRecords(AssetApprovalType.DISPOSAL.getCode(), disposalId);
    }

    /**
     * 校验确认处置入参。
     *
     * @param bo 处置参数
     */
    private void validateConfirmParams(AssetDisposalBo bo)
    {
        if (bo == null)
        {
            throw new ServiceException("处置参数不能为空");
        }
        if (bo.getAssetId() == null)
        {
            throw new ServiceException("资产ID不能为空");
        }
        if (StringUtils.isBlank(bo.getDisposalType()))
        {
            throw new ServiceException("处置类型不能为空");
        }
        if (StringUtils.isBlank(bo.getDisposalReason()))
        {
            throw new ServiceException("处置原因不能为空");
        }
        if (bo.getDisposalDate() == null)
        {
            throw new ServiceException("处置日期不能为空");
        }
    }

    /**
     * 构建处置实体。
     *
     * @param bo 处置参数
     * @param operator 操作人
     * @param now 当前时间
     * @return 处置实体
     */
    private AssetDisposal buildDisposalEntity(AssetDisposalBo bo, String operator)
    {
        AssetDisposal disposal = new AssetDisposal();
        disposal.setDisposalNo(generateNextDisposalNo());
        disposal.setAssetId(bo.getAssetId());
        disposal.setDisposalType(StringUtils.upperCase(StringUtils.trim(bo.getDisposalType())));
        disposal.setDisposalStatus(DISPOSAL_STATUS_SUBMITTED);
        disposal.setDisposalReason(StringUtils.trim(bo.getDisposalReason()));
        disposal.setDisposalDate(bo.getDisposalDate());
        disposal.setDisposalAmount(bo.getDisposalAmount());
        disposal.setFinanceConfirmFlag(resolveFinanceConfirmFlag(bo.getFinanceConfirmFlag()));
        disposal.setCreateBy(operator);
        disposal.setRemark(bo.getRemark());
        return disposal;
    }

    private AssetDisposalVo getSubmittedDisposal(Long disposalId)
    {
        if (disposalId == null)
        {
            throw new ServiceException("处置单ID不能为空");
        }

        AssetDisposalVo disposal = assetDisposalMapper.selectAssetDisposalById(disposalId);
        if (disposal == null)
        {
            throw new ServiceException("处置单不存在");
        }
        if (!DISPOSAL_STATUS_SUBMITTED.equalsIgnoreCase(StringUtils.defaultString(disposal.getDisposalStatus())))
        {
            throw new ServiceException("只有待审批处置单才能执行审批动作");
        }
        return disposal;
    }

    private void insertApprovalRecord(Long assetId, Long disposalId, AssetApprovalStatus approvalStatus,
        String operator, String opinion)
    {
        AssetApprovalRecord approvalRecord = new AssetApprovalRecord();
        approvalRecord.setAssetId(assetId);
        approvalRecord.setBizId(disposalId);
        approvalRecord.setApprovalType(AssetApprovalType.DISPOSAL.getCode());
        approvalRecord.setApprovalStatus(approvalStatus.getCode());
        approvalRecord.setOpinion(opinion);
        approvalRecord.setCreateBy(operator);
        assetApprovalMapper.insertAssetApprovalRecord(approvalRecord);
    }

    /**
     * 规范化财务确认标识。
     *
     * @param financeConfirmFlag 原始标识
     * @return 标准标识
     */
    private String resolveFinanceConfirmFlag(String financeConfirmFlag)
    {
        return "1".equals(StringUtils.trimToEmpty(financeConfirmFlag)) ? "1" : "0";
    }

    /**
     * 解析资产状态编码。
     *
     * @param assetStatus 状态编码
     * @return 状态枚举
     */
    private AssetStatus parseAssetStatus(String assetStatus)
    {
        String normalizedStatus = StringUtils.upperCase(StringUtils.defaultIfBlank(assetStatus, AssetStatus.DRAFT.getCode()));
        for (AssetStatus status : AssetStatus.values())
        {
            if (status.getCode().equals(normalizedStatus))
            {
                return status;
            }
        }
        throw new ServiceException("资产状态非法：" + assetStatus);
    }

    /**
     * 生成下一条处置单号。
     *
     * @return 处置单号
     */
    private String generateNextDisposalNo()
    {
        String currentYear = String.valueOf(Year.now().getValue());
        String disposalNoPrefix = DISPOSAL_NO_PREFIX + "-" + currentYear + "-";
        String currentMaxNo = assetDisposalMapper.selectMaxDisposalNoByPrefix(disposalNoPrefix);
        int nextSerial = parseNextSerial(currentMaxNo, disposalNoPrefix);
        return disposalNoPrefix + String.format("%0" + DISPOSAL_NO_SERIAL_LENGTH + "d", nextSerial);
    }

    /**
     * 解析下一条流水号。
     *
     * @param currentMaxNo 当前最大编号
     * @param noPrefix 编号前缀
     * @return 流水号
     */
    private int parseNextSerial(String currentMaxNo, String noPrefix)
    {
        if (StringUtils.isBlank(currentMaxNo) || !currentMaxNo.startsWith(noPrefix))
        {
            return 1;
        }
        String serialPart = currentMaxNo.substring(noPrefix.length());
        if (!StringUtils.isNumeric(serialPart))
        {
            return 1;
        }
        return Integer.parseInt(serialPart) + 1;
    }
}
