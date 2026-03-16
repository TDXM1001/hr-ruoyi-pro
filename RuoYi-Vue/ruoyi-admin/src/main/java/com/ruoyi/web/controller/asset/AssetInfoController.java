package com.ruoyi.web.controller.asset;

import java.util.List;
import com.ruoyi.asset.domain.AssetInfo;
import com.ruoyi.asset.domain.dto.AssetCreateReq;
import com.ruoyi.asset.domain.dto.AssetUpdateReq;
import com.ruoyi.asset.domain.vo.AssetListVo;
import com.ruoyi.asset.service.IAssetAggregateService;
import com.ruoyi.asset.service.IAssetInfoService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
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
 * 资产主档Controller
 *
 * @author ruoyi
 * @date 2026-03-14
 */
@RestController
@RequestMapping("/asset/info")
public class AssetInfoController extends BaseController {
    @Autowired
    private IAssetAggregateService assetAggregateService;

    @Autowired
    private IAssetInfoService assetInfoService;

    /**
     * 查询资产主档列表。
     */
    @PreAuthorize("@ss.hasPermi('asset:info:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetInfo assetInfo) {
        startPage();
        List<AssetListVo> list = assetAggregateService.selectAssetList(assetInfo);
        return getDataTable(list);
    }

    /**
     * 导出资产主档列表。
     */
    @PreAuthorize("@ss.hasPermi('asset:info:export')")
    @Log(title = "资产主档", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, AssetInfo assetInfo) {
        List<AssetListVo> list = assetAggregateService.selectAssetList(assetInfo);
        ExcelUtil<AssetListVo> util = new ExcelUtil<>(AssetListVo.class);
        util.exportExcel(response, list, "资产主档");
    }

    /**
     * 获取资产聚合详情。
     *
     * 返回主档、财务、不动产、动态属性、附件、最近折旧日志以及统一动作时间线。
     */
    @PreAuthorize("@ss.hasPermi('asset:info:query')")
    @GetMapping("/{assetId}")
    public AjaxResult getInfo(@PathVariable("assetId") Long assetId) {
        return success(assetAggregateService.selectAssetDetailByAssetId(assetId));
    }

    /**
     * 新增资产主档。
     */
    @PreAuthorize("@ss.hasPermi('asset:info:add')")
    @Log(title = "资产主档", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody AssetCreateReq assetCreateReq) {
        return toAjax(assetAggregateService.insertAsset(assetCreateReq));
    }

    /**
     * 修改资产主档。
     */
    @PreAuthorize("@ss.hasPermi('asset:info:edit')")
    @Log(title = "资产主档", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody AssetUpdateReq assetUpdateReq) {
        return toAjax(assetAggregateService.updateAsset(assetUpdateReq));
    }

    /**
     * 归档资产主档。
     */
    @PreAuthorize("@ss.hasPermi('asset:info:remove')")
    @Log(title = "资产主档归档", businessType = BusinessType.DELETE)
    @DeleteMapping("/{assetIds}")
    public AjaxResult archive(@PathVariable Long[] assetIds) {
        return toAjax(assetInfoService.deleteAssetInfoByAssetIds(assetIds));
    }
}
