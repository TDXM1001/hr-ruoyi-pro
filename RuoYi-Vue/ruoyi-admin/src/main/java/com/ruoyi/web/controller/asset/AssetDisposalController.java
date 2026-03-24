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
import com.ruoyi.asset.domain.AssetApprovalRecord;
import com.ruoyi.asset.domain.bo.AssetApprovalActionBo;
import com.ruoyi.asset.domain.bo.AssetDisposalBo;
import com.ruoyi.asset.domain.vo.AssetDisposalVo;
import com.ruoyi.asset.service.IAssetDisposalService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;

/**
 * 资产处置控制器。
 *
 * @author Codex
 */
@RestController
@RequestMapping("/asset/disposal")
public class AssetDisposalController extends BaseController
{
    @Autowired
    private IAssetDisposalService assetDisposalService;

    /**
     * 查询处置列表。
     *
     * @param bo 查询参数
     * @return 列表数据
     */
    @PreAuthorize("@ss.hasPermi('asset:disposal:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetDisposalBo bo)
    {
        startPage();
        List<AssetDisposalVo> list = assetDisposalService.selectAssetDisposalList(bo);
        return getDataTable(list);
    }

    /**
     * 查询处置详情。
     *
     * @param disposalId 处置ID
     * @return 详情数据
     */
    @PreAuthorize("@ss.hasPermi('asset:disposal:query')")
    @GetMapping("/{disposalId}")
    public AjaxResult getInfo(@PathVariable Long disposalId)
    {
        return success(assetDisposalService.selectAssetDisposalById(disposalId));
    }

    /**
     * 提交处置审批。
     *
     * @param bo 处置参数
     * @return 处置ID
     */
    @Log(title = "资产处置", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('asset:disposal:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AssetDisposalBo bo)
    {
        return success(assetDisposalService.submitDisposalApproval(bo, getUsername()));
    }

    /**
     * 审批通过处置单。
     *
     * @param disposalId 处置单ID
     * @param bo 审批动作
     * @return 结果
     */
    @Log(title = "资产处置审批", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('asset:disposal:approve')")
    @PostMapping("/{disposalId}/approve")
    public AjaxResult approve(@PathVariable Long disposalId, @Validated @RequestBody AssetApprovalActionBo bo)
    {
        assetDisposalService.approveDisposal(disposalId, bo, getUsername());
        return success();
    }

    /**
     * 驳回处置单。
     *
     * @param disposalId 处置单ID
     * @param bo 审批动作
     * @return 结果
     */
    @Log(title = "资产处置审批", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('asset:disposal:approve')")
    @PostMapping("/{disposalId}/reject")
    public AjaxResult reject(@PathVariable Long disposalId, @Validated @RequestBody AssetApprovalActionBo bo)
    {
        assetDisposalService.rejectDisposal(disposalId, bo, getUsername());
        return success();
    }

    /**
     * 查询处置审批轨迹。
     *
     * @param disposalId 处置单ID
     * @return 审批轨迹
     */
    @PreAuthorize("@ss.hasPermi('asset:disposal:approval')")
    @GetMapping("/{disposalId}/approvals")
    public AjaxResult approvals(@PathVariable Long disposalId)
    {
        List<AssetApprovalRecord> approvals = assetDisposalService.selectDisposalApprovalRecords(disposalId);
        return success(approvals);
    }
}
