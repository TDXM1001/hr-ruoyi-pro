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
import com.ruoyi.asset.domain.AssetInventoryTask;
import com.ruoyi.asset.domain.bo.AssetInventoryResultBo;
import com.ruoyi.asset.domain.bo.AssetInventoryTaskBo;
import com.ruoyi.asset.domain.vo.AssetInventoryTaskVo;
import com.ruoyi.asset.service.IAssetInventoryService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 资产盘点控制器。
 *
 * @author Codex
 */
@RestController
@RequestMapping("/asset/inventory")
public class AssetInventoryController extends BaseController
{
    @Autowired
    private IAssetInventoryService assetInventoryService;

    /**
     * 查询盘点任务列表。
     *
     * @param bo 查询参数
     * @return 任务列表
     */
    @PreAuthorize("@ss.hasPermi('asset:inventory:list')")
    @GetMapping("/task/list")
    public TableDataInfo list(AssetInventoryTaskBo bo)
    {
        startPage();
        List<AssetInventoryTaskVo> list = assetInventoryService.selectAssetInventoryTaskList(bo);
        return getDataTable(list);
    }

    /**
     * 查询盘点任务详情。
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    @PreAuthorize("@ss.hasPermi('asset:inventory:query')")
    @GetMapping("/task/{taskId}")
    public AjaxResult getInfo(@PathVariable Long taskId)
    {
        AssetInventoryTask task = assetInventoryService.selectAssetInventoryTaskById(taskId);
        return success(task);
    }

    /**
     * 新增盘点任务。
     *
     * @param bo 创建参数
     * @return 任务ID
     */
    @Log(title = "资产盘点", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('asset:inventory:add')")
    @PostMapping("/task")
    public AjaxResult addTask(@Validated @RequestBody AssetInventoryTaskBo bo)
    {
        return success(assetInventoryService.createAssetInventoryTask(bo, getUsername()));
    }

    /**
     * 提交盘点结果。
     *
     * @param bo 盘点结果
     * @return 处理结果
     */
    @Log(title = "资产盘点", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('asset:inventory:edit')")
    @PostMapping("/result")
    public AjaxResult submitResult(@Validated @RequestBody AssetInventoryResultBo bo)
    {
        assetInventoryService.submitResult(bo, getUsername());
        return success();
    }
}
