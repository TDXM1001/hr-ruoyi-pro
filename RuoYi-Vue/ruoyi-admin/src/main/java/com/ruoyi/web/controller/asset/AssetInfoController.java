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
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.service.IAssetInfoService;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 资产信息Controller
 *
 * @author ruoyi
 * @date 2026-03-11
 */
@RestController
@RequestMapping("/asset/info")
public class AssetInfoController extends BaseController {
    @Autowired
    private IAssetInfoService assetInfoService;

    /**
     * 查询资产信息列表
     */
    @PreAuthorize("@ss.hasPermi('asset:info:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetInfo assetInfo) {
        startPage();
        List<AssetInfo> list = assetInfoService.selectAssetInfoList(assetInfo);
        return getDataTable(list);
    }

    /**
     * 导出资产信息列表
     */
    @PreAuthorize("@ss.hasPermi('asset:info:export')")
    @Log(title = "资产信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AssetInfo assetInfo) {
        List<AssetInfo> list = assetInfoService.selectAssetInfoList(assetInfo);
        ExcelUtil<AssetInfo> util = new ExcelUtil<AssetInfo>(AssetInfo.class);
        util.exportExcel(response, list, "资产信息");
    }

    /**
     * 获取资产信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('asset:info:query')")
    @GetMapping(value = "/{assetNo}")
    public AjaxResult getInfo(@PathVariable("assetNo") String assetNo) {
        return success(assetInfoService.selectAssetInfoByAssetNo(assetNo));
    }

    /**
     * 新增资产信息
     */
    @PreAuthorize("@ss.hasPermi('asset:info:add')")
    @Log(title = "资产信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AssetInfo assetInfo) {
        return toAjax(assetInfoService.insertAssetInfo(assetInfo));
    }

    /**
     * 修改资产信息
     */
    @PreAuthorize("@ss.hasPermi('asset:info:edit')")
    @Log(title = "资产信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AssetInfo assetInfo) {
        return toAjax(assetInfoService.updateAssetInfo(assetInfo));
    }

    /**
     * 删除资产信息
     */
    @PreAuthorize("@ss.hasPermi('asset:info:remove')")
    @Log(title = "资产信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{assetNos}")
    public AjaxResult remove(@PathVariable String[] assetNos) {
        return toAjax(assetInfoService.deleteAssetInfoByAssetNos(assetNos));
    }
}
