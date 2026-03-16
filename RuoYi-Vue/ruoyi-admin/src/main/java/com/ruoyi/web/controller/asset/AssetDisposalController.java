package com.ruoyi.web.controller.asset;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.asset.domain.AssetDisposal;
import com.ruoyi.asset.service.IAssetDisposalService;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 固定资产报废/处置 Controller。
 */
@RestController
@RequestMapping("/asset/disposal")
public class AssetDisposalController extends BaseController {
    
    @Autowired
    private IAssetDisposalService assetDisposalService;

    @PreAuthorize("@ss.hasPermi('asset:disposal:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetDisposal assetDisposal) {
        startPage();
        List<AssetDisposal> list = assetDisposalService.selectAssetDisposalList(assetDisposal);
        return getDataTable(list);
    }

    /**
     * 查询处置单详情。
     */
    @PreAuthorize("@ss.hasPermi('asset:disposal:query')")
    @GetMapping("/{disposalNo}")
    public AjaxResult getInfo(@PathVariable("disposalNo") String disposalNo) {
        return success(assetDisposalService.selectAssetDisposalByDisposalNo(disposalNo));
    }

    @PreAuthorize("@ss.hasPermi('asset:disposal:add')")
    @PostMapping
    public AjaxResult add(@RequestBody AssetDisposal assetDisposal) {
        assetDisposal.setApplyUserId(SecurityUtils.getUserId());
        assetDisposal.setApplyDeptId(SecurityUtils.getDeptId());
        if (assetDisposal.getDisposalNo() == null) {
            assetDisposal.setDisposalNo("DIS" + System.currentTimeMillis());
        }
        return toAjax(assetDisposalService.insertAssetDisposal(assetDisposal));
    }
}
