package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetAttachment;

/**
 * 资产附件Mapper接口
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public interface AssetAttachmentMapper {
    public AssetAttachment selectAssetAttachmentByAttachmentId(Long attachmentId);

    public List<AssetAttachment> selectAssetAttachmentList(AssetAttachment assetAttachment);

    public List<AssetAttachment> selectAssetAttachmentByAssetId(Long assetId);

    public int insertAssetAttachment(AssetAttachment assetAttachment);

    public int updateAssetAttachment(AssetAttachment assetAttachment);

    public int deleteAssetAttachmentByAssetId(Long assetId);

    public int deleteAssetAttachmentByAttachmentId(Long attachmentId);

    public int deleteAssetAttachmentByAttachmentIds(Long[] attachmentIds);
}
