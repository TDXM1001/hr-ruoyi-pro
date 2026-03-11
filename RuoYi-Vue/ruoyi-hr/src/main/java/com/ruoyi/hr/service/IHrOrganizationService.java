package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.common.core.domain.TreeSelect;
import com.ruoyi.hr.domain.HrOrganization;

/**
 * 组织架构Service接口
 * 
 * @author ruoyi
 */
public interface IHrOrganizationService 
{
    /**
     * 查询组织架构
     * 
     * @param orgId 组织架构ID
     * @return 组织架构
     */
    public HrOrganization selectHrOrganizationByOrgId(Long orgId);

    /**
     * 查询组织架构列表
     * 
     * @param hrOrganization 组织架构
     * @return 组织架构集合
     */
    public List<HrOrganization> selectHrOrganizationList(HrOrganization hrOrganization);

    /**
     * 构建前端所需要树结构
     * 
     * @param orgs 组织架构列表
     * @return 树结构列表
     */
    public List<HrOrganization> buildOrgTree(List<HrOrganization> orgs);

    /**
     * 构建前端所需要下拉树结构
     * 
     * @param orgs 组织架构列表
     * @return 下拉树结构列表
     */
    public List<TreeSelect> buildOrgTreeSelect(List<HrOrganization> orgs);

    /**
     * 新增组织架构
     * 
     * @param hrOrganization 组织架构
     * @return 结果
     */
    public int insertHrOrganization(HrOrganization hrOrganization);

    /**
     * 修改组织架构
     * 
     * @param hrOrganization 组织架构
     * @return 结果
     */
    public int updateHrOrganization(HrOrganization hrOrganization);

    /**
     * 批量删除组织架构
     * 
     * @param orgIds 需要删除的组织架构ID
     * @return 结果
     */
    public int deleteHrOrganizationByOrgIds(Long[] orgIds);

    /**
     * 删除组织架构信息
     * 
     * @param orgId 组织架构ID
     * @return 结果
     */
    public int deleteHrOrganizationByOrgId(Long orgId);
}
