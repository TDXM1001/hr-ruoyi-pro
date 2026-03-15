package com.ruoyi.web.controller.asset;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateOwnershipChange;
import com.ruoyi.asset.service.IAssetRealEstateOwnershipChangeService;
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
 * 不动产权属变更 Controller。
 */
@RestController
@RequestMapping("/asset/real-estate/ownership")
public class AssetRealEstateOwnershipChangeController extends BaseController {

    @Autowired
    private IAssetRealEstateOwnershipChangeService ownershipChangeService;

    @PreAuthorize("@ss.hasPermi('asset:realEstateOwnership:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetRealEstateOwnershipChange ownershipChange) {
        startPage();
        List<AssetRealEstateOwnershipChange> list = ownershipChangeService.selectOwnershipChangeList(ownershipChange);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('asset:realEstateOwnership:query')")
    @GetMapping("/{ownershipChangeNo}")
    public AjaxResult getInfo(@PathVariable("ownershipChangeNo") String ownershipChangeNo) {
        return success(ownershipChangeService.selectOwnershipChangeByNo(ownershipChangeNo));
    }

    @PreAuthorize("@ss.hasPermi('asset:realEstateOwnership:add')")
    @PostMapping
    public AjaxResult add(@RequestBody AssetRealEstateOwnershipChange ownershipChange) {
        ownershipChange.setApplyUserId(SecurityUtils.getUserId());
        ownershipChange.setApplyDeptId(SecurityUtils.getDeptId());
        if (ownershipChange.getOwnershipChangeNo() == null) {
            ownershipChange.setOwnershipChangeNo("OWN" + System.currentTimeMillis());
        }
        return toAjax(ownershipChangeService.insertOwnershipChange(ownershipChange));
    }
}
