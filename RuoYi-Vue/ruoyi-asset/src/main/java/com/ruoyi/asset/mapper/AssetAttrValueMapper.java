package com.ruoyi.asset.mapper;

import java.util.List;
import com.ruoyi.asset.domain.AssetAttrValue;

/**
 * 资产扩展字段值Mapper接口
 *
 * @author ruoyi
 * @date 2026-03-14
 */
public interface AssetAttrValueMapper {
    public AssetAttrValue selectAssetAttrValueByValueId(Long valueId);

    public List<AssetAttrValue> selectAssetAttrValueList(AssetAttrValue assetAttrValue);

    public List<AssetAttrValue> selectAssetAttrValueByAssetId(Long assetId);

    public int insertAssetAttrValue(AssetAttrValue assetAttrValue);

    public int updateAssetAttrValue(AssetAttrValue assetAttrValue);

    public int deleteAssetAttrValueByValueId(Long valueId);

    public int deleteAssetAttrValueByValueIds(Long[] valueIds);
}
