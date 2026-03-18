package com.ruoyi.web.controller.asset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
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
import com.ruoyi.asset.domain.vo.AssetTreeNodeVo;
import com.ruoyi.asset.domain.vo.AssetUserOptionVo;
import com.ruoyi.asset.domain.bo.AssetLedgerBo;
import com.ruoyi.asset.domain.vo.AssetLedgerVo;
import com.ruoyi.asset.mapper.AssetLookupMapper;
import com.ruoyi.asset.service.IAssetLedgerService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.TreeSelect;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 资产台账操作控制器。
 *
 * <p>当前控制器提供固定资产一期台账管理的最小闭环接口，
 * 供前端动态路由页面完成列表查询、详情查看、新增、编辑与导出。</p>
 *
 * @author Codex
 */
@RestController
@RequestMapping("/asset/ledger")
public class AssetLedgerController extends BaseController
{
    @Autowired
    private IAssetLedgerService assetLedgerService;

    @Autowired
    private AssetLookupMapper assetLookupMapper;

    /**
     * 查询资产台账列表。
     *
     * @param bo 查询条件
     * @return 分页列表
     */
    @PreAuthorize("@ss.hasPermi('asset:ledger:list')")
    @GetMapping("/list")
    public TableDataInfo list(AssetLedgerBo bo)
    {
        startPage();
        List<AssetLedgerVo> list = assetLedgerService.selectAssetLedgerList(bo);
        return getDataTable(list);
    }

    /**
     * 导出资产台账。
     *
     * @param response 响应对象
     * @param bo 查询条件
     */
    @Log(title = "资产台账", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('asset:ledger:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, AssetLedgerBo bo)
    {
        List<AssetLedgerVo> list = assetLedgerService.selectAssetLedgerList(bo);
        ExcelUtil<AssetLedgerVo> util = new ExcelUtil<AssetLedgerVo>(AssetLedgerVo.class);
        util.exportExcel(response, list, "资产台账数据");
    }

    /**
     * 查询资产台账详情。
     *
     * @param assetId 资产ID
     * @return 详情数据
     */
    @PreAuthorize("@ss.hasPermi('asset:ledger:query')")
    @GetMapping("/{assetId}")
    public AjaxResult getInfo(@PathVariable Long assetId)
    {
        return success(assetLedgerService.selectAssetLedgerById(assetId));
    }

    /**
     * 获取下一条建议资产编号。
     *
     * <p>固定资产一期默认由后端提供建议编号，
     * 避免前端手工录入导致编号规则漂移。</p>
     *
     * @return 建议资产编号
     */
    @PreAuthorize("@ss.hasPermi('asset:ledger:add')")
    @GetMapping("/nextCode")
    public AjaxResult nextCode()
    {
        // 显式按 data 字段返回建议编号，避免 String 重载被识别为消息文本。
        return success((Object) assetLedgerService.getNextAssetCode());
    }

    /**
     * 查询资产分类树。
     *
     * <p>页面版资产台账使用树选择器替代录入分类ID，
     * 当前阶段固定过滤固定资产分类。</p>
     *
     * @return 分类树
     */
    @PreAuthorize("@ss.hasAnyPermi('asset:ledger:list,asset:ledger:query,asset:ledger:add,asset:ledger:edit')")
    @GetMapping("/categoryTree")
    public AjaxResult categoryTree()
    {
        List<AssetTreeNodeVo> nodes = assetLookupMapper.selectAssetCategoryNodes("FIXED");
        return success(buildTreeSelect(nodes));
    }

    /**
     * 查询资产部门树。
     *
     * @return 部门树
     */
    @PreAuthorize("@ss.hasAnyPermi('asset:ledger:list,asset:ledger:query,asset:ledger:add,asset:ledger:edit')")
    @GetMapping("/deptTree")
    public AjaxResult deptTree()
    {
        List<AssetTreeNodeVo> nodes = assetLookupMapper.selectDeptNodes();
        return success(buildTreeSelect(nodes));
    }

    /**
     * 查询责任人远程搜索选项。
     *
     * @param keyword 搜索关键字
     * @return 责任人下拉项
     */
    @PreAuthorize("@ss.hasAnyPermi('asset:ledger:list,asset:ledger:query,asset:ledger:add,asset:ledger:edit')")
    @GetMapping("/responsibleUsers")
    public AjaxResult responsibleUsers(String keyword)
    {
        List<AssetUserOptionVo> options = assetLookupMapper.selectResponsibleUserOptions(StringUtils.trim(keyword));
        return success(options);
    }

    /**
     * 新增资产台账。
     *
     * @param bo 台账入参
     * @return 新增后的资产ID
     */
    @Log(title = "资产台账", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('asset:ledger:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AssetLedgerBo bo)
    {
        return success(assetLedgerService.createAsset(bo, getUsername()));
    }

    /**
     * 修改资产台账。
     *
     * @param bo 台账入参
     * @return 修改结果
     */
    @Log(title = "资产台账", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('asset:ledger:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AssetLedgerBo bo)
    {
        return toAjax(assetLedgerService.updateAsset(bo, getUsername()));
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
