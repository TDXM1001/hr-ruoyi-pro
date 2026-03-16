package com.ruoyi.asset.domain.dto;

import java.util.ArrayList;
import java.util.List;
import com.ruoyi.asset.domain.AssetAttachment;
import com.ruoyi.asset.domain.AssetAttrValue;
import com.ruoyi.asset.domain.AssetFinance;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.AssetRealEstate;

/**
 * 资产聚合新增请求。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public class AssetCreateReq {
    /** 主档基础信息 */
    private AssetInfo basicInfo;

    /** 财务信息 */
    private AssetFinance financeInfo;

    /** 不动产信息 */
    private AssetRealEstate realEstateInfo;

    /** 动态扩展属性 */
    private List<AssetAttrValue> dynamicAttrs = new ArrayList<>();

    /** 附件信息 */
    private List<AssetAttachment> attachments = new ArrayList<>();

    public AssetInfo getBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(AssetInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    public AssetFinance getFinanceInfo() {
        return financeInfo;
    }

    public void setFinanceInfo(AssetFinance financeInfo) {
        this.financeInfo = financeInfo;
    }

    public AssetRealEstate getRealEstateInfo() {
        return realEstateInfo;
    }

    public void setRealEstateInfo(AssetRealEstate realEstateInfo) {
        this.realEstateInfo = realEstateInfo;
    }

    public List<AssetAttrValue> getDynamicAttrs() {
        return dynamicAttrs;
    }

    public void setDynamicAttrs(List<AssetAttrValue> dynamicAttrs) {
        this.dynamicAttrs = dynamicAttrs == null ? new ArrayList<>() : dynamicAttrs;
    }

    public List<AssetAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AssetAttachment> attachments) {
        this.attachments = attachments == null ? new ArrayList<>() : attachments;
    }

    /**
     * 统一暴露聚合主档主键，便于控制器、服务和测试按同一口径读取。
     */
    public Long getAssetId() {
        return basicInfo == null ? null : basicInfo.getAssetId();
    }

    /**
     * 统一暴露聚合主档资产编号，避免调用方重复钻取 basicInfo。
     */
    public String getAssetNo() {
        return basicInfo == null ? null : basicInfo.getAssetNo();
    }
}
