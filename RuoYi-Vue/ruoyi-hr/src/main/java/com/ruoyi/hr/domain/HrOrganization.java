package com.ruoyi.hr.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 组织架构对象 hr_organization
 * 
 * @author ruoyi
 */
public class HrOrganization extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 组织ID */
    private Long orgId;

    /** 上级组织ID */
    @Excel(name = "上级组织ID")
    private Long parentId;

    /** 祖级列表 */
    @Excel(name = "祖级列表")
    private String ancestors;

    /** 组织名称 */
    @Excel(name = "组织名称")
    private String orgName;

    /** 组织编码 */
    @Excel(name = "组织编码")
    private String orgCode;

    /** 组织类型（1集团 2公司 3事业部 4中心 5部门 6团队） */
    @Excel(name = "组织类型", readConverterExp = "1=集团,2=公司,3=事业部,4=中心,5=部门,6=团队")
    private String orgType;

    /** 组织层级 */
    @Excel(name = "组织层级")
    private Integer orgLevel;

    /** 负责人ID */
    @Excel(name = "负责人ID")
    private Long leaderId;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String phone;

    /** 办公地址 */
    @Excel(name = "办公地址")
    private String address;

    /** 成立日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "成立日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date establishDate;

    /** 排序号 */
    @Excel(name = "排序号")
    private Integer orderNum;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 关联若依 sys_dept_id */
    @Excel(name = "关联若依 sys_dept_id")
    private Long sysDeptId;

    /** 子组织 */
    private List<HrOrganization> children = new ArrayList<HrOrganization>();

    public void setOrgId(Long orgId) 
    {
        this.orgId = orgId;
    }

    public Long getOrgId() 
    {
        return orgId;
    }
    public void setParentId(Long parentId) 
    {
        this.parentId = parentId;
    }

    public Long getParentId() 
    {
        return parentId;
    }
    public void setAncestors(String ancestors) 
    {
        this.ancestors = ancestors;
    }

    public String getAncestors() 
    {
        return ancestors;
    }
    public void setOrgName(String orgName) 
    {
        this.orgName = orgName;
    }

    public String getOrgName() 
    {
        return orgName;
    }
    public void setOrgCode(String orgCode) 
    {
        this.orgCode = orgCode;
    }

    public String getOrgCode() 
    {
        return orgCode;
    }
    public void setOrgType(String orgType) 
    {
        this.orgType = orgType;
    }

    public String getOrgType() 
    {
        return orgType;
    }
    public void setOrgLevel(Integer orgLevel) 
    {
        this.orgLevel = orgLevel;
    }

    public Integer getOrgLevel() 
    {
        return orgLevel;
    }
    public void setLeaderId(Long leaderId) 
    {
        this.leaderId = leaderId;
    }

    public Long getLeaderId() 
    {
        return leaderId;
    }
    public void setPhone(String phone) 
    {
        this.phone = phone;
    }

    public String getPhone() 
    {
        return phone;
    }
    public void setAddress(String address) 
    {
        this.address = address;
    }

    public String getAddress() 
    {
        return address;
    }
    public void setEstablishDate(Date establishDate) 
    {
        this.establishDate = establishDate;
    }

    public Date getEstablishDate() 
    {
        return establishDate;
    }
    public void setOrderNum(Integer orderNum) 
    {
        this.orderNum = orderNum;
    }

    public Integer getOrderNum() 
    {
        return orderNum;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setSysDeptId(Long sysDeptId) 
    {
        this.sysDeptId = sysDeptId;
    }

    public Long getSysDeptId() 
    {
        return sysDeptId;
    }

    public List<HrOrganization> getChildren() {
        return children;
    }

    public void setChildren(List<HrOrganization> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("orgId", getOrgId())
            .append("parentId", getParentId())
            .append("ancestors", getAncestors())
            .append("orgName", getOrgName())
            .append("orgCode", getOrgCode())
            .append("orgType", getOrgType())
            .append("orgLevel", getOrgLevel())
            .append("leaderId", getLeaderId())
            .append("phone", getPhone())
            .append("address", getAddress())
            .append("establishDate", getEstablishDate())
            .append("orderNum", getOrderNum())
            .append("status", getStatus())
            .append("sysDeptId", getSysDeptId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
