package com.ruoyi.web.controller.asset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.asset.domain.bo.AssetRealEstateBo;
import com.ruoyi.asset.domain.bo.AssetRectificationApprovalActionBo;
import com.ruoyi.asset.domain.bo.AssetRectificationBo;
import com.ruoyi.asset.domain.bo.AssetRectificationCompleteBo;
import com.ruoyi.asset.domain.vo.AssetRectificationVo;
import com.ruoyi.asset.domain.vo.AssetTreeNodeVo;
import com.ruoyi.asset.domain.vo.AssetUserOptionVo;
import com.ruoyi.asset.mapper.AssetLookupMapper;
import com.ruoyi.asset.service.IAssetRectificationService;
import com.ruoyi.asset.service.IAssetRealEstateService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.TreeSelect;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 不动产档案控制器。
 *
 * <p>用于支撑资产管理员按独立页面模式维护不动产档案，
 * 包括列表、详情、新建、编辑以及表单下拉所需的轻量查询接口。</p>
 *
 * @author Codex
 */
@RestController
@RequestMapping("/asset/real-estate")
public class AssetRealEstateController extends BaseController
{
    @Autowired
    private IAssetRealEstateService assetRealEstateService;

    @Autowired
    private AssetLookupMapper assetLookupMapper;

    @Autowired
    private IAssetRectificationService assetRectificationService;

    /**
     * 查询不动产档案列表。
     *
     * @param query 查询条件
     * @return 分页数据
     */
    @PreAuthorize("@ss.hasPermi('asset:realEstate:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetRealEstateBo query)
    {
        startPage();
        return getDataTable(assetRealEstateService.selectList(query));
    }

    /**
     * 查询不动产档案详情。
     *
     * @param assetId 资产ID
     * @return 详情
     */
    @PreAuthorize("@ss.hasPermi('asset:realEstate:query')")
    @GetMapping("/{assetId}")
    public AjaxResult getInfo(@PathVariable Long assetId)
    {
        return success(assetRealEstateService.selectDetailByAssetId(assetId));
    }

    /**
     * 查询不动产生命周期详情。
     *
     * @param assetId 资产ID
     * @return 生命周期详情
     */
    @PreAuthorize("@ss.hasPermi('asset:realEstate:query')")
    @GetMapping("/{assetId}/lifecycle")
    public AjaxResult getLifecycle(@PathVariable Long assetId)
    {
        return success(assetRealEstateService.selectLifecycleByAssetId(assetId));
    }

    /**
     * 获取下一条建议资产编码。
     *
     * @return 建议资产编码
     */
    @PreAuthorize("@ss.hasPermi('asset:realEstate:add')")
    @GetMapping("/nextCode")
    public AjaxResult nextCode()
    {
        return success((Object) assetRealEstateService.getNextAssetCode());
    }

    /**
     * 查询不动产分类树。
     *
     * @return 分类树
     */
    @PreAuthorize("@ss.hasAnyPermi('asset:realEstate:list,asset:realEstate:query,asset:realEstate:add,asset:realEstate:edit')")
    @GetMapping("/categoryTree")
    public AjaxResult categoryTree()
    {
        List<AssetTreeNodeVo> nodes = assetLookupMapper.selectAssetCategoryNodes("REAL_ESTATE");
        return success(buildTreeSelect(nodes));
    }

    /**
     * 查询部门树。
     *
     * @return 部门树
     */
    @PreAuthorize("@ss.hasAnyPermi('asset:realEstate:list,asset:realEstate:query,asset:realEstate:add,asset:realEstate:edit')")
    @GetMapping("/deptTree")
    public AjaxResult deptTree()
    {
        List<AssetTreeNodeVo> nodes = assetLookupMapper.selectDeptNodes();
        return success(buildTreeSelect(nodes));
    }

    /**
     * 查询责任人远程搜索选项。
     *
     * @param keyword 搜索关键词
     * @return 责任人下拉项
     */
    @PreAuthorize("@ss.hasAnyPermi('asset:realEstate:list,asset:realEstate:query,asset:realEstate:add,asset:realEstate:edit')")
    @GetMapping("/responsibleUsers")
    public AjaxResult responsibleUsers(String keyword)
    {
        List<AssetUserOptionVo> options = assetLookupMapper.selectResponsibleUserOptions(StringUtils.trim(keyword));
        return success(options);
    }

    /**
     * 新增不动产档案。
     *
     * @param bo 建档入参
     * @return 新增后的资产ID
     */
    @Log(title = "不动产档案", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('asset:realEstate:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AssetRealEstateBo bo)
    {
        return success(assetRealEstateService.createAsset(bo, getUsername()));
    }

    /**
     * 修改不动产档案。
     *
     * @param bo 编辑入参
     * @return 修改结果
     */
    @Log(title = "不动产档案", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('asset:realEstate:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AssetRealEstateBo bo)
    {
        return toAjax(assetRealEstateService.updateAsset(bo, getUsername()));
    }

    /**
     * 查询整改列表。
     *
     * @param assetId 资产ID
     * @return 整改列表
     */
    @PreAuthorize("@ss.hasPermi('asset:realEstate:query')")
    @GetMapping("/{assetId}/rectifications")
    public AjaxResult listRectifications(@PathVariable Long assetId)
    {
        return success(assetRectificationService.selectAssetRectificationListByAssetId(assetId));
    }

    /**
     * 查询整改详情。
     *
     * @param assetId 资产ID
     * @param rectificationId 整改单ID
     * @return 整改详情
     */
    @PreAuthorize("@ss.hasPermi('asset:realEstate:query')")
    @GetMapping("/{assetId}/rectifications/{rectificationId}")
    public AjaxResult getRectificationInfo(@PathVariable Long assetId, @PathVariable Long rectificationId)
    {
        AssetRectificationVo detail = assetRectificationService.selectAssetRectificationById(rectificationId);
        if (!assetId.equals(detail.getAssetId()))
        {
            throw new ServiceException("\u6574\u6539\u5355\u4e0e\u8d44\u4ea7\u4e0d\u5339\u914d");
        }
        return success(detail);
    }

    /**
     * 发起整改登记。
     *
     * @param assetId 资产ID
     * @param bo 整改入参
     * @return 整改单ID
     */
    @Log(title = "不动产整改登记", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('asset:realEstate:edit')")
    @PostMapping("/{assetId}/rectifications")
    public AjaxResult addRectification(@PathVariable Long assetId, @Validated @RequestBody AssetRectificationBo bo)
    {
        bo.setAssetId(assetId);
        return success(assetRectificationService.createAssetRectification(bo, getUsername()));
    }

    /**
     * 更新整改登记。
     *
     * @param assetId 资产ID
     * @param bo 整改入参
     * @return 更新结果
     */
    @Log(title = "不动产整改登记", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('asset:realEstate:edit')")
    @PutMapping("/{assetId}/rectifications")
    public AjaxResult editRectification(@PathVariable Long assetId, @Validated @RequestBody AssetRectificationBo bo)
    {
        bo.setAssetId(assetId);
        return toAjax(assetRectificationService.updateAssetRectification(bo, getUsername()));
    }

    @Log(title = "不动产整改完成", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('asset:realEstate:edit')")
    @PostMapping("/{assetId}/rectifications/{rectificationId}/complete")
    public AjaxResult completeRectification(@PathVariable Long assetId, @PathVariable Long rectificationId,
        @Validated @RequestBody AssetRectificationCompleteBo bo)
    {
        return toAjax(assetRectificationService.completeAssetRectification(assetId, rectificationId, bo, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('asset:realEstate:query')")
    @GetMapping("/{assetId}/rectifications/{rectificationId}/approval-records")
    public AjaxResult approvalRecords(@PathVariable Long assetId, @PathVariable Long rectificationId)
    {
        return success(assetRectificationService.selectRectificationApprovalRecords(assetId, rectificationId));
    }

    @Log(title = "不动产整改审批提交", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('asset:realEstate:edit')")
    @PostMapping("/{assetId}/rectifications/{rectificationId}/submit-approval")
    public AjaxResult submitApproval(@PathVariable Long assetId, @PathVariable Long rectificationId,
        @Validated @RequestBody AssetRectificationApprovalActionBo bo)
    {
        return toAjax(assetRectificationService.submitRectificationApproval(assetId, rectificationId, bo, getUsername()));
    }

    @Log(title = "不动产整改审批通过", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('asset:realEstate:edit')")
    @PostMapping("/{assetId}/rectifications/{rectificationId}/approve")
    public AjaxResult approve(@PathVariable Long assetId, @PathVariable Long rectificationId,
        @Validated @RequestBody AssetRectificationApprovalActionBo bo)
    {
        return toAjax(assetRectificationService.approveRectificationApproval(assetId, rectificationId, bo, getUsername()));
    }

    @Log(title = "不动产整改审批驳回", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('asset:realEstate:edit')")
    @PostMapping("/{assetId}/rectifications/{rectificationId}/reject")
    public AjaxResult reject(@PathVariable Long assetId, @PathVariable Long rectificationId,
        @Validated @RequestBody AssetRectificationApprovalActionBo bo)
    {
        return toAjax(assetRectificationService.rejectRectificationApproval(assetId, rectificationId, bo, getUsername()));
    }

    /**
     * 将平铺节点组装为前端可直接消费的树选择结构。
     *
     * @param nodes 平铺节点
     * @return TreeSelect 列表
     */
    private List<TreeSelect> buildTreeSelect(List<AssetTreeNodeVo> nodes)
    {
        List<TreeSelect> roots = new ArrayList<TreeSelect>();
        Map<Long, TreeSelect> nodeMap = new HashMap<Long, TreeSelect>();
        List<AssetTreeNodeVo> sortedNodes = new ArrayList<AssetTreeNodeVo>(nodes);
        sortedNodes.sort(Comparator
            .comparing((AssetTreeNodeVo item) -> item.getOrderNum() == null ? Integer.MAX_VALUE : item.getOrderNum())
            .thenComparing(item -> item.getId() == null ? Long.MAX_VALUE : item.getId()));

        for (AssetTreeNodeVo node : sortedNodes)
        {
            TreeSelect treeSelect = new TreeSelect();
            treeSelect.setId(node.getId());
            treeSelect.setLabel(node.getLabel());
            treeSelect.setDisabled(Boolean.TRUE.equals(node.getDisabled()));
            treeSelect.setChildren(new ArrayList<TreeSelect>());
            nodeMap.put(node.getId(), treeSelect);
        }

        for (AssetTreeNodeVo node : sortedNodes)
        {
            TreeSelect current = nodeMap.get(node.getId());
            Long parentId = node.getParentId();
            if (parentId == null || parentId.longValue() == 0L || !nodeMap.containsKey(parentId))
            {
                roots.add(current);
                continue;
            }
            nodeMap.get(parentId).getChildren().add(current);
        }

        trimEmptyChildren(roots);
        return roots;
    }

    /**
     * 清理叶子节点空 children，保持响应结构简洁。
     *
     * @param nodes 树节点列表
     */
    private void trimEmptyChildren(List<TreeSelect> nodes)
    {
        for (TreeSelect node : nodes)
        {
            if (node.getChildren() == null || node.getChildren().isEmpty())
            {
                node.setChildren(null);
                continue;
            }
            trimEmptyChildren(node.getChildren());
        }
    }
}
