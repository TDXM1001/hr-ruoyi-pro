package com.ruoyi.web.controller.asset;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.asset.domain.bo.AssetHandoverCreateBo;
import com.ruoyi.asset.domain.bo.AssetHandoverOrderBo;
import com.ruoyi.asset.domain.vo.AssetHandoverItemVo;
import com.ruoyi.asset.domain.vo.AssetHandoverOrderVo;
import com.ruoyi.asset.service.IAssetHandoverService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 资产交接控制器。
 *
 * <p>一期交接采用“主单 + 明细”模型。
 * 台账只提供结果展示，使用关系变更必须通过交接主单落单后统一回写。</p>
 *
 * @author Codex
 */
@RestController
@RequestMapping("/asset/handover")
public class AssetHandoverController extends BaseController
{
    @Autowired
    private IAssetHandoverService assetHandoverService;

    /**
     * 查询交接主单列表。
     *
     * @param bo 查询参数
     * @return 表格数据
     */
    @PreAuthorize("@ss.hasPermi('asset:handover:list')")
    @GetMapping("/order/list")
    public TableDataInfo list(AssetHandoverOrderBo bo)
    {
        startPage();
        List<AssetHandoverOrderVo> list = assetHandoverService.selectAssetHandoverOrderList(bo);
        return getDataTable(list);
    }

    /**
     * 查询交接主单详情。
     *
     * @param handoverOrderId 主单ID
     * @return 主单详情
     */
    @PreAuthorize("@ss.hasPermi('asset:handover:list')")
    @GetMapping("/order/{handoverOrderId}")
    public AjaxResult getInfo(@PathVariable Long handoverOrderId)
    {
        return success(assetHandoverService.selectAssetHandoverOrderById(handoverOrderId));
    }

    /**
     * 查询交接主单明细。
     *
     * @param handoverOrderId 主单ID
     * @return 明细列表
     */
    @PreAuthorize("@ss.hasPermi('asset:handover:list')")
    @GetMapping("/order/{handoverOrderId}/items")
    public AjaxResult items(@PathVariable Long handoverOrderId)
    {
        List<AssetHandoverItemVo> items = assetHandoverService.selectAssetHandoverItemsByOrderId(handoverOrderId);
        return success(items);
    }

    /**
     * 新增交接主单。
     *
     * @param bo 交接参数
     * @return 交接主单ID
     */
    @Log(title = "资产交接", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('asset:handover:add')")
    @PostMapping("/order")
    public AjaxResult add(@Validated @RequestBody AssetHandoverCreateBo bo)
    {
        return success(assetHandoverService.createHandoverOrder(bo, getUsername()));
    }
}
