package com.ruoyi.web.controller.asset;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateDisposal;
import com.ruoyi.asset.service.IAssetRealEstateDisposalService;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 不动产注销/处置 Controller。
 */
@RestController
@RequestMapping("/asset/real-estate/disposal")
public class AssetRealEstateDisposalController extends BaseController {

    @Autowired
    private IAssetRealEstateDisposalService disposalService;

    @PreAuthorize("@ss.hasPermi('asset:realEstateDisposal:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetRealEstateDisposal disposal) {
        startPage();
        List<AssetRealEstateDisposal> list = disposalService.selectDisposalList(disposal);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('asset:realEstateDisposal:query')")
    @GetMapping("/{disposalNo}")
    public AjaxResult getInfo(@PathVariable("disposalNo") String disposalNo) {
        return success(disposalService.selectDisposalByNo(disposalNo));
    }

    @PreAuthorize("@ss.hasPermi('asset:realEstateDisposal:add')")
    @PostMapping
    public AjaxResult add(@RequestBody AssetRealEstateDisposal disposal) {
        disposal.setApplyUserId(SecurityUtils.getUserId());
        disposal.setApplyDeptId(SecurityUtils.getDeptId());
        if (disposal.getDisposalNo() == null) {
            disposal.setDisposalNo("RED" + System.currentTimeMillis());
        }
        return toAjax(disposalService.insertDisposal(disposal));
    }
}
