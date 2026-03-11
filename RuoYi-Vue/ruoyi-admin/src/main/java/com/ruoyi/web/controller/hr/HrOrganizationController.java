package com.ruoyi.web.controller.hr;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.hr.domain.HrOrganization;
import com.ruoyi.hr.service.IHrOrganizationService;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 组织架构Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/hr/organization")
public class HrOrganizationController extends BaseController
{
    @Autowired
    private IHrOrganizationService hrOrganizationService;

    /**
     * 查询组织架构列表
     */
    @PreAuthorize("@ss.hasPermi('hr:organization:list')")
    @GetMapping("/list")
    public AjaxResult list(HrOrganization hrOrganization)
    {
        List<HrOrganization> list = hrOrganizationService.selectHrOrganizationList(hrOrganization);
        return AjaxResult.success(list);
    }

    /**
     * 获取部门下拉树列表
     */
    @GetMapping("/treeselect")
    public AjaxResult treeselect(HrOrganization hrOrganization)
    {
        List<HrOrganization> depts = hrOrganizationService.selectHrOrganizationList(hrOrganization);
        return AjaxResult.success(hrOrganizationService.buildOrgTreeSelect(depts));
    }

    /**
     * 导出组织架构列表
     */
    @PreAuthorize("@ss.hasPermi('hr:organization:export')")
    @Log(title = "组织架构", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrOrganization hrOrganization)
    {
        List<HrOrganization> list = hrOrganizationService.selectHrOrganizationList(hrOrganization);
        ExcelUtil<HrOrganization> util = new ExcelUtil<HrOrganization>(HrOrganization.class);
        util.exportExcel(response, list, "组织架构数据");
    }

    /**
     * 获取组织架构详细信息
     */
    @PreAuthorize("@ss.hasPermi('hr:organization:query')")
    @GetMapping(value = "/{orgId}")
    public AjaxResult getInfo(@PathVariable("orgId") Long orgId)
    {
        return AjaxResult.success(hrOrganizationService.selectHrOrganizationByOrgId(orgId));
    }

    /**
     * 新增组织架构
     */
    @PreAuthorize("@ss.hasPermi('hr:organization:add')")
    @Log(title = "组织架构", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HrOrganization hrOrganization)
    {
        hrOrganization.setCreateBy(getUsername());
        return toAjax(hrOrganizationService.insertHrOrganization(hrOrganization));
    }

    /**
     * 修改组织架构
     */
    @PreAuthorize("@ss.hasPermi('hr:organization:edit')")
    @Log(title = "组织架构", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HrOrganization hrOrganization)
    {
        hrOrganization.setUpdateBy(getUsername());
        return toAjax(hrOrganizationService.updateHrOrganization(hrOrganization));
    }

    /**
     * 删除组织架构
     */
    @PreAuthorize("@ss.hasPermi('hr:organization:remove')")
    @Log(title = "组织架构", businessType = BusinessType.DELETE)
	@DeleteMapping("/{orgIds}")
    public AjaxResult remove(@PathVariable Long[] orgIds)
    {
        return toAjax(hrOrganizationService.deleteHrOrganizationByOrgIds(orgIds));
    }
}
