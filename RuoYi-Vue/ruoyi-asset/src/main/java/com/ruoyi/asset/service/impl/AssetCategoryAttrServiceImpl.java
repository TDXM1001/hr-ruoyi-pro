package com.ruoyi.asset.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.ruoyi.asset.domain.AssetCategoryAttr;
import com.ruoyi.asset.mapper.AssetCategoryAttrMapper;
import com.ruoyi.asset.service.IAssetCategoryAttrService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 资产分类扩展字段服务实现。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
@Service
public class AssetCategoryAttrServiceImpl implements IAssetCategoryAttrService {
    /**
     * 系统主字段保留编码，避免动态字段覆盖主数据语义。
     */
    private static final Set<String> RESERVED_ATTR_CODES = new HashSet<>(Arrays.asList(
        "asset_no",
        "asset_name",
        "original_value",
        "property_cert_no"
    ));

    @Autowired
    private AssetCategoryAttrMapper assetCategoryAttrMapper;

    @Override
    public AssetCategoryAttr selectAssetCategoryAttrByAttrId(Long attrId) {
        return assetCategoryAttrMapper.selectAssetCategoryAttrByAttrId(attrId);
    }

    @Override
    public List<AssetCategoryAttr> selectAssetCategoryAttrList(AssetCategoryAttr assetCategoryAttr) {
        return assetCategoryAttrMapper.selectAssetCategoryAttrList(assetCategoryAttr);
    }

    @Override
    public List<AssetCategoryAttr> selectAssetCategoryAttrByCategoryId(Long categoryId) {
        return assetCategoryAttrMapper.selectAssetCategoryAttrByCategoryId(categoryId);
    }

    @Override
    public int insertAssetCategoryAttr(AssetCategoryAttr assetCategoryAttr) {
        validateAssetCategoryAttr(assetCategoryAttr, false);
        assetCategoryAttr.setCreateTime(DateUtils.getNowDate());
        assetCategoryAttr.setUpdateTime(DateUtils.getNowDate());
        if (StringUtils.isBlank(assetCategoryAttr.getStatus())) {
            assetCategoryAttr.setStatus("0");
        }
        return assetCategoryAttrMapper.insertAssetCategoryAttr(assetCategoryAttr);
    }

    @Override
    public int updateAssetCategoryAttr(AssetCategoryAttr assetCategoryAttr) {
        validateAssetCategoryAttr(assetCategoryAttr, true);
        assetCategoryAttr.setUpdateTime(DateUtils.getNowDate());
        return assetCategoryAttrMapper.updateAssetCategoryAttr(assetCategoryAttr);
    }

    @Override
    public int disableAssetCategoryAttr(Long attrId, String updateBy) {
        AssetCategoryAttr currentAttr = assetCategoryAttrMapper.selectAssetCategoryAttrByAttrId(attrId);
        if (currentAttr == null) {
            throw new ServiceException("待禁用的扩展字段不存在");
        }
        AssetCategoryAttr disabledAttr = new AssetCategoryAttr();
        disabledAttr.setAttrId(attrId);
        disabledAttr.setStatus("1");
        disabledAttr.setUpdateBy(updateBy);
        disabledAttr.setUpdateTime(DateUtils.getNowDate());
        return assetCategoryAttrMapper.updateAssetCategoryAttr(disabledAttr);
    }

    @Override
    public int deleteAssetCategoryAttrByAttrIds(Long[] attrIds) {
        return assetCategoryAttrMapper.deleteAssetCategoryAttrByAttrIds(attrIds);
    }

    /**
     * 校验字段定义的必填项、保留编码和分类内唯一性。
     */
    private void validateAssetCategoryAttr(AssetCategoryAttr assetCategoryAttr, boolean update) {
        if (assetCategoryAttr == null) {
            throw new ServiceException("扩展字段定义不能为空");
        }
        if (update && assetCategoryAttr.getAttrId() == null) {
            throw new ServiceException("修改扩展字段时字段ID不能为空");
        }
        if (assetCategoryAttr.getCategoryId() == null) {
            throw new ServiceException("所属分类不能为空");
        }
        if (StringUtils.isBlank(assetCategoryAttr.getAttrCode())) {
            throw new ServiceException("字段编码不能为空");
        }
        if (StringUtils.isBlank(assetCategoryAttr.getAttrName())) {
            throw new ServiceException("字段名称不能为空");
        }
        if (StringUtils.isBlank(assetCategoryAttr.getDataType())) {
            throw new ServiceException("数据类型不能为空");
        }

        String normalizedAttrCode = normalizeAttrCode(assetCategoryAttr.getAttrCode());
        assetCategoryAttr.setAttrCode(normalizedAttrCode);
        assetCategoryAttr.setDataType(normalizeFieldType(assetCategoryAttr.getDataType()));
        if (StringUtils.isBlank(assetCategoryAttr.getAttrType())) {
            assetCategoryAttr.setAttrType(assetCategoryAttr.getDataType());
        } else {
            assetCategoryAttr.setAttrType(normalizeFieldType(assetCategoryAttr.getAttrType()));
        }
        if (RESERVED_ATTR_CODES.contains(normalizedAttrCode)) {
            throw new ServiceException("字段编码[" + normalizedAttrCode + "]为系统保留字段，不允许使用");
        }

        AssetCategoryAttr query = new AssetCategoryAttr();
        query.setCategoryId(assetCategoryAttr.getCategoryId());
        query.setAttrCode(normalizedAttrCode);
        List<AssetCategoryAttr> existsAttrs = assetCategoryAttrMapper.selectAssetCategoryAttrList(query);
        for (AssetCategoryAttr existsAttr : existsAttrs) {
            if (!update || !existsAttr.getAttrId().equals(assetCategoryAttr.getAttrId())) {
                throw new ServiceException("同一分类下字段编码已存在");
            }
        }
    }

    /**
     * 字段编码统一转为小写下划线风格，便于后续接口契约稳定。
     */
    private String normalizeAttrCode(String attrCode) {
        return StringUtils.trimToEmpty(attrCode).toLowerCase(Locale.ROOT);
    }

    /**
     * 字段类型统一去空白并转小写，确保前后端约定稳定。
     */
    private String normalizeFieldType(String fieldType) {
        return StringUtils.trimToEmpty(fieldType).toLowerCase(Locale.ROOT);
    }
}
