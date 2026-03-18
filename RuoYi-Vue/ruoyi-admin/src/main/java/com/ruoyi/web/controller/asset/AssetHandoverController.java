package com.ruoyi.web.controller.asset;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.asset.domain.bo.AssetHandoverBo;
import com.ruoyi.asset.domain.vo.AssetHandoverVo;
import com.ruoyi.asset.service.IAssetHandoverService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 资产交接控制器。
 *
 * <p>
 * 第一期开通领用、调拨、退还统一交接入口，
 * 统一由后端校验状态并回写台账，避免前端直改台账字段。
 * </p>
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
     * 查询交接记录列表。
     *
     * @param bo 查询条件
     * @return 分页列表
     */
    @PreAuthorize("@ss.hasPermi('asset:handover:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetHandoverBo bo)
    {
        startPage();
        List<AssetHandoverVo> list = assetHandoverService.selectAssetHandoverList(bo);
        return getDataTable(list);
    }

    /**
     * 新增交接记录。
     *
     * @param bo 交接入参
     * @return 交接ID
     */
    @Log(title = "资产交接", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('asset:handover:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AssetHandoverBo bo)
    {
        return success(assetHandoverService.createHandover(bo, getUsername()));
    }
}
