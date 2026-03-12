package com.ruoyi.web.controller.asset;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.asset.domain.AssetRequisition;
import com.ruoyi.asset.service.IAssetRequisitionService;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 资产领用 Controller
 */
@RestController
@RequestMapping("/asset/requisition")
public class AssetRequisitionController extends BaseController {
    
    @Autowired
    private IAssetRequisitionService assetRequisitionService;

    /**
     * 查询领用列表
     */
    @PreAuthorize("@ss.hasPermi('asset:requisition:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetRequisition assetRequisition) {
        startPage();
        List<AssetRequisition> list = assetRequisitionService.selectAssetRequisitionList(assetRequisition);
        return getDataTable(list);
    }

    /**
     * 获取领用详细信息
     */
    @PreAuthorize("@ss.hasPermi('asset:requisition:query')")
    @GetMapping(value = "/{requisitionNo}")
    public AjaxResult getInfo(@PathVariable("requisitionNo") String requisitionNo) {
        return success(assetRequisitionService.selectAssetRequisitionByRequisitionNo(requisitionNo));
    }

    /**
     * 发起领用申请
     */
    @PreAuthorize("@ss.hasPermi('asset:requisition:add')")
    @PostMapping
    public AjaxResult add(@RequestBody AssetRequisition assetRequisition) {
        assetRequisition.setApplyUserId(SecurityUtils.getUserId());
        assetRequisition.setApplyDeptId(SecurityUtils.getDeptId());
        // 生成流水号
        if (assetRequisition.getRequisitionNo() == null) {
            assetRequisition.setRequisitionNo("REQ" + System.currentTimeMillis());
        }
        return toAjax(assetRequisitionService.insertAssetRequisition(assetRequisition));
    }
}
