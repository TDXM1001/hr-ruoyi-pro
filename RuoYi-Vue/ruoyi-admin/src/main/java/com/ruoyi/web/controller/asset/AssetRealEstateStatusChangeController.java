package com.ruoyi.web.controller.asset;

import java.util.List;
import com.ruoyi.asset.domain.AssetRealEstateStatusChange;
import com.ruoyi.asset.service.IAssetRealEstateStatusChangeService;
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
 * 不动产状态变更 Controller。
 */
@RestController
@RequestMapping("/asset/real-estate/status")
public class AssetRealEstateStatusChangeController extends BaseController {

    @Autowired
    private IAssetRealEstateStatusChangeService statusChangeService;

    @PreAuthorize("@ss.hasPermi('asset:realEstateStatus:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetRealEstateStatusChange statusChange) {
        startPage();
        List<AssetRealEstateStatusChange> list = statusChangeService.selectStatusChangeList(statusChange);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('asset:realEstateStatus:query')")
    @GetMapping("/{statusChangeNo}")
    public AjaxResult getInfo(@PathVariable("statusChangeNo") String statusChangeNo) {
        return success(statusChangeService.selectStatusChangeByNo(statusChangeNo));
    }

    @PreAuthorize("@ss.hasPermi('asset:realEstateStatus:add')")
    @PostMapping
    public AjaxResult add(@RequestBody AssetRealEstateStatusChange statusChange) {
        statusChange.setApplyUserId(SecurityUtils.getUserId());
        statusChange.setApplyDeptId(SecurityUtils.getDeptId());
        if (statusChange.getStatusChangeNo() == null) {
            statusChange.setStatusChangeNo("RES" + System.currentTimeMillis());
        }
        return toAjax(statusChangeService.insertStatusChange(statusChange));
    }
}
