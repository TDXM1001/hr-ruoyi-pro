package com.ruoyi.hr.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.TreeSelect;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrOrganization;
import com.ruoyi.hr.mapper.HrOrganizationMapper;
import com.ruoyi.hr.service.IHrOrganizationService;

/**
 * 组织架构Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class HrOrganizationServiceImpl implements IHrOrganizationService 
{
    @Autowired
    private HrOrganizationMapper hrOrganizationMapper;

    /**
     * 查询组织架构
     * 
     * @param orgId 组织架构ID
     * @return 组织架构
     */
    @Override
    public HrOrganization selectHrOrganizationByOrgId(Long orgId)
    {
        return hrOrganizationMapper.selectHrOrganizationByOrgId(orgId);
    }

    /**
     * 查询组织架构列表
     * 
     * @param hrOrganization 组织架构
     * @return 组织架构
     */
    @Override
    public List<HrOrganization> selectHrOrganizationList(HrOrganization hrOrganization)
    {
        return hrOrganizationMapper.selectHrOrganizationList(hrOrganization);
    }

    /**
     * 构建前端所需要树结构
     * 
     * @param orgs 组织架构列表
     * @return 树结构列表
     */
    @Override
    public List<HrOrganization> buildOrgTree(List<HrOrganization> orgs)
    {
        List<HrOrganization> returnList = new ArrayList<HrOrganization>();
        List<Long> tempList = orgs.stream().map(HrOrganization::getOrgId).collect(Collectors.toList());
        for (HrOrganization org : orgs)
        {
            // 如果是顶级节点, 遍历该父节点的所有子节点
            if (!tempList.contains(org.getParentId()))
            {
                recursionFn(orgs, org);
                returnList.add(org);
            }
        }
        if (returnList.isEmpty())
        {
            returnList = orgs;
        }
        return returnList;
    }

    /**
     * 构建前端所需要下拉树结构
     * 
     * @param orgs 组织架构列表
     * @return 下拉树结构列表
     */
    @Override
    public List<TreeSelect> buildOrgTreeSelect(List<HrOrganization> orgs)
    {
        List<HrOrganization> orgTrees = buildOrgTree(orgs);
        return orgTrees.stream().map(this::convertToTreeSelect).collect(Collectors.toList());
    }

    private TreeSelect convertToTreeSelect(HrOrganization org) {
        TreeSelect treeSelect = new TreeSelect();
        treeSelect.setId(org.getOrgId());
        treeSelect.setLabel(org.getOrgName());
        if (org.getChildren() != null && !org.getChildren().isEmpty()) {
            treeSelect.setChildren(org.getChildren().stream().map(this::convertToTreeSelect).collect(Collectors.toList()));
        }
        return treeSelect;
    }

    /**
     * 新增组织架构
     * 
     * @param hrOrganization 组织架构
     * @return 结果
     */
    @Override
    public int insertHrOrganization(HrOrganization hrOrganization)
    {
        hrOrganization.setCreateTime(DateUtils.getNowDate());
        return hrOrganizationMapper.insertHrOrganization(hrOrganization);
    }

    /**
     * 修改组织架构
     * 
     * @param hrOrganization 组织架构
     * @return 结果
     */
    @Override
    public int updateHrOrganization(HrOrganization hrOrganization)
    {
        hrOrganization.setUpdateTime(DateUtils.getNowDate());
        return hrOrganizationMapper.updateHrOrganization(hrOrganization);
    }

    /**
     * 批量删除组织架构
     * 
     * @param orgIds 需要删除的组织架构ID
     * @return 结果
     */
    @Override
    public int deleteHrOrganizationByOrgIds(Long[] orgIds)
    {
        return hrOrganizationMapper.deleteHrOrganizationByOrgIds(orgIds);
    }

    /**
     * 删除组织架构信息
     * 
     * @param orgId 组织架构ID
     * @return 结果
     */
    @Override
    public int deleteHrOrganizationByOrgId(Long orgId)
    {
        return hrOrganizationMapper.deleteHrOrganizationByOrgId(orgId);
    }

    /**
     * 递归列表
     */
    private void recursionFn(List<HrOrganization> list, HrOrganization t)
    {
        // 得到子节点列表
        List<HrOrganization> childList = getChildList(list, t);
        t.setChildren(childList);
        for (HrOrganization tChild : childList)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<HrOrganization> getChildList(List<HrOrganization> list, HrOrganization t)
    {
        List<HrOrganization> tlist = new ArrayList<HrOrganization>();
        Iterator<HrOrganization> it = list.iterator();
        while (it.hasNext())
        {
            HrOrganization n = (HrOrganization) it.next();
            if (StringUtils.isNotNull(n.getParentId()) && (n.getParentId() != null && n.getParentId().longValue() == t.getOrgId().longValue()))
            {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<HrOrganization> list, HrOrganization t)
    {
        return getChildList(list, t).size() > 0;
    }
}
