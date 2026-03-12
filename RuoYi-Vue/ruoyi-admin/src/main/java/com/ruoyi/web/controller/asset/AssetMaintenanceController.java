package com.ruoyi.web.controller.asset;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.asset.domain.AssetMaintenance;
import com.ruoyi.asset.service.IAssetMaintenanceService;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 资产维修 Controller
 */
@RestController
@RequestMapping("/asset/maintenance")
public class AssetMaintenanceController extends BaseController {
    
    @Autowired
    private IAssetMaintenanceService assetMaintenanceService;

    @PreAuthorize("@ss.hasPermi('asset:maintenance:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetMaintenance assetMaintenance) {
        startPage();
        List<AssetMaintenance> list = assetMaintenanceService.selectAssetMaintenanceList(assetMaintenance);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('asset:maintenance:add')")
    @PostMapping
    public AjaxResult add(@RequestBody AssetMaintenance assetMaintenance) {
        assetMaintenance.setApplyUserId(SecurityUtils.getUserId());
        assetMaintenance.setApplyDeptId(SecurityUtils.getDeptId());
        if (assetMaintenance.getMaintenanceNo() == null) {
            assetMaintenance.setMaintenanceNo("MNT" + System.currentTimeMillis());
        }
        return toAjax(assetMaintenanceService.insertAssetMaintenance(assetMaintenance));
    }
}
