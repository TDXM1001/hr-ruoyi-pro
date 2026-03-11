package com.ruoyi.web.controller.asset;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.asset.domain.AssetCategory;
import com.ruoyi.asset.service.IAssetCategoryService;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 资产分类Controller
 *
 * @author ruoyi
 * @date 2026-03-11
 */
@RestController
@RequestMapping("/asset/category")
public class AssetCategoryController extends BaseController {
    @Autowired
    private IAssetCategoryService assetCategoryService;

    /**
     * 查询资产分类列表
     */
    @PreAuthorize("@ss.hasPermi('asset:category:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetCategory assetCategory) {
        startPage();
        List<AssetCategory> list = assetCategoryService.selectAssetCategoryList(assetCategory);
        return getDataTable(list);
    }

    /**
     * 获取资产分类详细信息
     */
    @PreAuthorize("@ss.hasPermi('asset:category:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(assetCategoryService.selectAssetCategoryById(id));
    }

    /**
     * 新增资产分类
     */
    @PreAuthorize("@ss.hasPermi('asset:category:add')")
    @Log(title = "资产分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AssetCategory assetCategory) {
        return toAjax(assetCategoryService.insertAssetCategory(assetCategory));
    }

    /**
     * 修改资产分类
     */
    @PreAuthorize("@ss.hasPermi('asset:category:edit')")
    @Log(title = "资产分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AssetCategory assetCategory) {
        return toAjax(assetCategoryService.updateAssetCategory(assetCategory));
    }

    /**
     * 删除资产分类
     */
    @PreAuthorize("@ss.hasPermi('asset:category:remove')")
    @Log(title = "资产分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(assetCategoryService.deleteAssetCategoryByIds(ids));
    }
}
