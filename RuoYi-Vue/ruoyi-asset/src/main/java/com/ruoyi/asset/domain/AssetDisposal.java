package com.ruoyi.asset.domain;

import java.util.Set;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.StringUtils;

/**
 * 固定资产报废/处置实体。
 */
public class AssetDisposal extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 报废。 */
    public static final String DISPOSAL_TYPE_SCRAP = "scrap";

    /** 出售。 */
    public static final String DISPOSAL_TYPE_SELL = "sell";

    /** 划转。 */
    public static final String DISPOSAL_TYPE_TRANSFER = "transfer";

    /** 捐赠。 */
    public static final String DISPOSAL_TYPE_DONATE = "donate";

    /** 固定资产处置类型白名单。 */
    private static final Set<String> SUPPORTED_DISPOSAL_TYPES = Set.of(
        DISPOSAL_TYPE_SCRAP,
        DISPOSAL_TYPE_SELL,
        DISPOSAL_TYPE_TRANSFER,
        DISPOSAL_TYPE_DONATE
    );

    /** 报废单号 */
    private String disposalNo;

    /** 资产ID */
    private Long assetId;

    /** 资产编号 */
    @Excel(name = "资产编号")
    private String assetNo;

    /** 申请人 */
    @Excel(name = "申请人")
    private Long applyUserId;

    /** 申请部门 */
    @Excel(name = "申请部门")
    private Long applyDeptId;

    /** 报废原因 */
    @Excel(name = "报废原因")
    private String reason;

    /** 处置类型：scrap=报废 sell=出售 transfer=划转 donate=捐赠 */
    @Excel(name = "处置类型")
    private String disposalType;

    /** 状态：0=审批中 1=已通过(已报废) 2=已驳回 */
    @Excel(name = "状态")
    private Integer status;

    public void setDisposalNo(String disposalNo) {
        this.disposalNo = disposalNo;
    }

    public String getDisposalNo() {
        return disposalNo;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetNo(String assetNo) {
        this.assetNo = assetNo;
    }

    public String getAssetNo() {
        return assetNo;
    }

    public void setApplyUserId(Long applyUserId) {
        this.applyUserId = applyUserId;
    }

    public Long getApplyUserId() {
        return applyUserId;
    }

    public void setApplyDeptId(Long applyDeptId) {
        this.applyDeptId = applyDeptId;
    }

    public Long getApplyDeptId() {
        return applyDeptId;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setDisposalType(String disposalType) {
        this.disposalType = disposalType;
    }

    public String getDisposalType() {
        return disposalType;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    /**
     * 统一把前后端传入的处置类型规整到数据库口径。
     */
    public static String normalizeDisposalType(String disposalType) {
        String normalized = StringUtils.trimToNull(disposalType);
        return normalized == null ? null : StringUtils.lowerCase(normalized);
    }

    /**
     * 判断是否为支持的固定资产处置类型。
     */
    public static boolean isSupportedDisposalType(String disposalType) {
        String normalized = normalizeDisposalType(disposalType);
        return normalized != null && SUPPORTED_DISPOSAL_TYPES.contains(normalized);
    }

    /**
     * 空值兼容历史“统一按报废收口”的老数据。
     */
    public static boolean isScrapType(String disposalType) {
        String normalized = normalizeDisposalType(disposalType);
        return normalized == null || DISPOSAL_TYPE_SCRAP.equals(normalized);
    }
}
