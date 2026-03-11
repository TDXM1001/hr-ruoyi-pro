package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrOrganization;

/**
 * 组织架构Mapper接口
 * 
 * @author ruoyi
 */
public interface HrOrganizationMapper 
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
     * 删除组织架构
     * 
     * @param orgId 组织架构ID
     * @return 结果
     */
    public int deleteHrOrganizationByOrgId(Long orgId);

    /**
     * 批量删除组织架构
     * 
     * @param orgIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteHrOrganizationByOrgIds(Long[] orgIds);
}
