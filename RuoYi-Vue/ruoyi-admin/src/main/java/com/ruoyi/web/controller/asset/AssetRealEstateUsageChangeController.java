package com.ruoyi.web.controller.asset;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateUsageChange;
import com.ruoyi.asset.service.IAssetRealEstateUsageChangeService;
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
 * 不动产用途变更 Controller。
 */
@RestController
@RequestMapping("/asset/real-estate/usage")
public class AssetRealEstateUsageChangeController extends BaseController {

    @Autowired
    private IAssetRealEstateUsageChangeService usageChangeService;

    @PreAuthorize("@ss.hasPermi('asset:realEstateUsage:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetRealEstateUsageChange usageChange) {
        startPage();
        List<AssetRealEstateUsageChange> list = usageChangeService.selectUsageChangeList(usageChange);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('asset:realEstateUsage:query')")
    @GetMapping("/{usageChangeNo}")
    public AjaxResult getInfo(@PathVariable("usageChangeNo") String usageChangeNo) {
        return success(usageChangeService.selectUsageChangeByNo(usageChangeNo));
    }

    @PreAuthorize("@ss.hasPermi('asset:realEstateUsage:add')")
    @PostMapping
    public AjaxResult add(@RequestBody AssetRealEstateUsageChange usageChange) {
        usageChange.setApplyUserId(SecurityUtils.getUserId());
        usageChange.setApplyDeptId(SecurityUtils.getDeptId());
        if (usageChange.getUsageChangeNo() == null) {
            usageChange.setUsageChangeNo("REU" + System.currentTimeMillis());
        }
        return toAjax(usageChangeService.insertUsageChange(usageChange));
    }
}
