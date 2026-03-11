package com.ruoyi.hr.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 员工主对象 hr_employee
 * 
 * @author ruoyi
 */
public class HrEmployee extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 员工ID */
    private Long employeeId;

    /** 工号 */
    @Excel(name = "工号")
    private String employeeNo;

    /** 关联系统用户ID */
    private Long userId;

    /** 姓名 */
    @Excel(name = "姓名")
    private String name;

    /** 性别（0男 1女 2未知） */
    @Excel(name = "性别", readConverterExp = "0=男,1=女,2=未知")
    private String gender;

    /** 身份证号 */
    @Excel(name = "身份证号")
    private String idCard;

    /** 出生日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "出生日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date birthday;

    /** 手机号 */
    @Excel(name = "手机号")
    private String phone;

    /** 邮箱 */
    @Excel(name = "邮箱")
    private String email;

    /** 照片路径 */
    private String photo;

    /** 所属组织ID */
    @Excel(name = "所属组织ID")
    private Long orgId;

    /** 岗位ID */
    @Excel(name = "岗位ID")
    private Long positionId;

    /** 职级ID */
    @Excel(name = "职级ID")
    private Long levelId;

    /** 直属上级ID */
    @Excel(name = "直属上级ID")
    private Long directLeaderId;

    /** 用工类型（1正式 2实习 3劳务派遣 4外包） */
    @Excel(name = "用工类型", readConverterExp = "1=正式,2=实习,3=劳务派遣,4=外包")
    private String employeeType;

    /** 状态（0待入职 1试用 2正式 3调岗中 4离职中 5已离职） */
    @Excel(name = "状态", readConverterExp = "0=待入职,1=试用,2=正式,3=调岗中,4=离职中,5=已离职")
    private String employeeStatus;

    /** 入职日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "入职日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date hireDate;

    /** 试用期结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "试用期结束日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date probationEnd;

    /** 转正日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "转正日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date regularDate;

    /** 离职日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "离职日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date leaveDate;

    /** 离职原因 */
    @Excel(name = "离职原因")
    private String leaveReason;

    /** 工作地点 */
    @Excel(name = "工作地点")
    private String workLocation;

    /** 民族 */
    @Excel(name = "民族")
    private String nationality;

    /** 籍贯 */
    @Excel(name = "籍贯")
    private String nativePlace;

    /** 婚姻状况 */
    @Excel(name = "婚姻状况")
    private String maritalStatus;

    /** 政治面貌 */
    @Excel(name = "政治面貌")
    private String political;

    /** 最高学历 */
    @Excel(name = "最高学历")
    private String highestEdu;

    public void setEmployeeId(Long employeeId) 
    {
        this.employeeId = employeeId;
    }

    public Long getEmployeeId() 
    {
        return employeeId;
    }
    public void setEmployeeNo(String employeeNo) 
    {
        this.employeeNo = employeeNo;
    }

    public String getEmployeeNo() 
    {
        return employeeNo;
    }
    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }
    public void setGender(String gender) 
    {
        this.gender = gender;
    }

    public String getGender() 
    {
        return gender;
    }
    public void setIdCard(String idCard) 
    {
        this.idCard = idCard;
    }

    public String getIdCard() 
    {
        return idCard;
    }
    public void setBirthday(Date birthday) 
    {
        this.birthday = birthday;
    }

    public Date getBirthday() 
    {
        return birthday;
    }
    public void setPhone(String phone) 
    {
        this.phone = phone;
    }

    public String getPhone() 
    {
        return phone;
    }
    public void setEmail(String email) 
    {
        this.email = email;
    }

    public String getEmail() 
    {
        return email;
    }
    public void setPhoto(String photo) 
    {
        this.photo = photo;
    }

    public String getPhoto() 
    {
        return photo;
    }
    public void setOrgId(Long orgId) 
    {
        this.orgId = orgId;
    }

    public Long getOrgId() 
    {
        return orgId;
    }
    public void setPositionId(Long positionId) 
    {
        this.positionId = positionId;
    }

    public Long getPositionId() 
    {
        return positionId;
    }
    public void setLevelId(Long levelId) 
    {
        this.levelId = levelId;
    }

    public Long getLevelId() 
    {
        return levelId;
    }
    public void setDirectLeaderId(Long directLeaderId) 
    {
        this.directLeaderId = directLeaderId;
    }

    public Long getDirectLeaderId() 
    {
        return directLeaderId;
    }
    public void setEmployeeType(String employeeType) 
    {
        this.employeeType = employeeType;
    }

    public String getEmployeeType() 
    {
        return employeeType;
    }
    public void setEmployeeStatus(String employeeStatus) 
    {
        this.employeeStatus = employeeStatus;
    }

    public String getEmployeeStatus() 
    {
        return employeeStatus;
    }
    public void setHireDate(Date hireDate) 
    {
        this.hireDate = hireDate;
    }

    public Date getHireDate() 
    {
        return hireDate;
    }
    public void setProbationEnd(Date probationEnd) 
    {
        this.probationEnd = probationEnd;
    }

    public Date getProbationEnd() 
    {
        return probationEnd;
    }
    public void setRegularDate(Date regularDate) 
    {
        this.regularDate = regularDate;
    }

    public Date getRegularDate() 
    {
        return regularDate;
    }
    public void setLeaveDate(Date leaveDate) 
    {
        this.leaveDate = leaveDate;
    }

    public Date getLeaveDate() 
    {
        return leaveDate;
    }
    public void setLeaveReason(String leaveReason) 
    {
        this.leaveReason = leaveReason;
    }

    public String getLeaveReason() 
    {
        return leaveReason;
    }
    public void setWorkLocation(String workLocation) 
    {
        this.workLocation = workLocation;
    }

    public String getWorkLocation() 
    {
        return workLocation;
    }
    public void setNationality(String nationality) 
    {
        this.nationality = nationality;
    }

    public String getNationality() 
    {
        return nationality;
    }
    public void setNativePlace(String nativePlace) 
    {
        this.nativePlace = nativePlace;
    }

    public String getNativePlace() 
    {
        return nativePlace;
    }
    public void setMaritalStatus(String maritalStatus) 
    {
        this.maritalStatus = maritalStatus;
    }

    public String getMaritalStatus() 
    {
        return maritalStatus;
    }
    public void setPolitical(String political) 
    {
        this.political = political;
    }

    public String getPolitical() 
    {
        return political;
    }
    public void setHighestEdu(String highestEdu) 
    {
        this.highestEdu = highestEdu;
    }

    public String getHighestEdu() 
    {
        return highestEdu;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("employeeId", getEmployeeId())
            .append("employeeNo", getEmployeeNo())
            .append("userId", getUserId())
            .append("name", getName())
            .append("gender", getGender())
            .append("idCard", getIdCard())
            .append("birthday", getBirthday())
            .append("phone", getPhone())
            .append("email", getEmail())
            .append("photo", getPhoto())
            .append("orgId", getOrgId())
            .append("positionId", getPositionId())
            .append("levelId", getLevelId())
            .append("directLeaderId", getDirectLeaderId())
            .append("employeeType", getEmployeeType())
            .append("employeeStatus", getEmployeeStatus())
            .append("hireDate", getHireDate())
            .append("probationEnd", getProbationEnd())
            .append("regularDate", getRegularDate())
            .append("leaveDate", getLeaveDate())
            .append("leaveReason", getLeaveReason())
            .append("workLocation", getWorkLocation())
            .append("nationality", getNationality())
            .append("nativePlace", getNativePlace())
            .append("maritalStatus", getMaritalStatus())
            .append("political", getPolitical())
            .append("highestEdu", getHighestEdu())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
