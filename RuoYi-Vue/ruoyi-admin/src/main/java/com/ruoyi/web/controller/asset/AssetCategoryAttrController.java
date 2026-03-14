package com.ruoyi.web.controller.asset;

import java.util.List;
import com.ruoyi.asset.domain.AssetCategoryAttr;
import com.ruoyi.asset.service.IAssetCategoryAttrService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 资产分类扩展字段Controller。
 *
 * @author ruoyi
 * @date 2026-03-14
 */
@RestController
@RequestMapping("/asset/categoryAttr")
public class AssetCategoryAttrController extends BaseController {
    @Autowired
    private IAssetCategoryAttrService assetCategoryAttrService;

    /**
     * 查询扩展字段定义列表。
     */
    @PreAuthorize("@ss.hasPermi('asset:categoryAttr:list')")
    @GetMapping("/list")
    public AjaxResult list(AssetCategoryAttr assetCategoryAttr) {
        List<AssetCategoryAttr> list = assetCategoryAttrService.selectAssetCategoryAttrList(assetCategoryAttr);
        return success(list);
    }

    /**
     * 按分类查询扩展字段定义。
     */
    @PreAuthorize("@ss.hasPermi('asset:categoryAttr:list')")
    @GetMapping("/category/{categoryId}")
    public AjaxResult listByCategoryId(@PathVariable("categoryId") Long categoryId) {
        return success(assetCategoryAttrService.selectAssetCategoryAttrByCategoryId(categoryId));
    }

    /**
     * 查询扩展字段定义详情。
     */
    @PreAuthorize("@ss.hasPermi('asset:categoryAttr:query')")
    @GetMapping("/{attrId}")
    public AjaxResult getInfo(@PathVariable("attrId") Long attrId) {
        return success(assetCategoryAttrService.selectAssetCategoryAttrByAttrId(attrId));
    }

    /**
     * 新增扩展字段定义。
     */
    @PreAuthorize("@ss.hasPermi('asset:categoryAttr:add')")
    @Log(title = "资产分类扩展字段", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AssetCategoryAttr assetCategoryAttr) {
        assetCategoryAttr.setCreateBy(getUsername());
        assetCategoryAttr.setUpdateBy(getUsername());
        return toAjax(assetCategoryAttrService.insertAssetCategoryAttr(assetCategoryAttr));
    }

    /**
     * 修改扩展字段定义。
     */
    @PreAuthorize("@ss.hasPermi('asset:categoryAttr:edit')")
    @Log(title = "资产分类扩展字段", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AssetCategoryAttr assetCategoryAttr) {
        assetCategoryAttr.setUpdateBy(getUsername());
        return toAjax(assetCategoryAttrService.updateAssetCategoryAttr(assetCategoryAttr));
    }

    /**
     * 禁用扩展字段定义。
     */
    @PreAuthorize("@ss.hasPermi('asset:categoryAttr:edit')")
    @Log(title = "资产分类扩展字段", businessType = BusinessType.UPDATE)
    @PutMapping("/disable/{attrId}")
    public AjaxResult disable(@PathVariable("attrId") Long attrId) {
        return toAjax(assetCategoryAttrService.disableAssetCategoryAttr(attrId, getUsername()));
    }

    /**
     * 删除扩展字段定义。
     */
    @PreAuthorize("@ss.hasPermi('asset:categoryAttr:remove')")
    @Log(title = "资产分类扩展字段", businessType = BusinessType.DELETE)
    @DeleteMapping("/{attrIds}")
    public AjaxResult remove(@PathVariable Long[] attrIds) {
        return toAjax(assetCategoryAttrService.deleteAssetCategoryAttrByAttrIds(attrIds));
    }
}
