package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.hr.mapper.HrEmployeeMapper;
import com.ruoyi.hr.domain.HrEmployee;
import com.ruoyi.hr.service.IHrEmployeeService;

/**
 * 员工主Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class HrEmployeeServiceImpl implements IHrEmployeeService 
{
    @Autowired
    private HrEmployeeMapper hrEmployeeMapper;
    
    @Autowired
    private ISysUserService sysUserService;

    /**
     * 查询员工主
     * 
     * @param employeeId 员工主主键
     * @return 员工主
     */
    @Override
    public HrEmployee selectHrEmployeeByEmployeeId(Long employeeId)
    {
        return hrEmployeeMapper.selectHrEmployeeByEmployeeId(employeeId);
    }

    /**
     * 查询员工主列表
     * 
     * @param hrEmployee 员工主
     * @return 员工主
     */
    @Override
    public List<HrEmployee> selectHrEmployeeList(HrEmployee hrEmployee)
    {
        return hrEmployeeMapper.selectHrEmployeeList(hrEmployee);
    }

    /**
     * 新增员工主
     * 
     * @param hrEmployee 员工主
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertHrEmployee(HrEmployee hrEmployee)
    {
        hrEmployee.setCreateTime(DateUtils.getNowDate());
        
        // 联动创建系统用户 (如果需要并且没有userId)
        if (hrEmployee.getUserId() == null && StringUtils.isNotEmpty(hrEmployee.getPhone())) {
            SysUser user = new SysUser();
            user.setUserName(hrEmployee.getEmployeeNo());
            user.setNickName(hrEmployee.getName());
            user.setPhonenumber(hrEmployee.getPhone());
            user.setEmail(hrEmployee.getEmail());
            user.setSex(hrEmployee.getGender());
            // 默认密码使用 SecurityUtils 工具的默认密码加密
            user.setPassword(SecurityUtils.encryptPassword("123456"));
            // 可以默认给个部门
            // user.setDeptId(100L); 
            
            sysUserService.insertUser(user);
            hrEmployee.setUserId(user.getUserId());
        }
        
        return hrEmployeeMapper.insertHrEmployee(hrEmployee);
    }

    /**
     * 修改员工主
     * 
     * @param hrEmployee 员工主
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateHrEmployee(HrEmployee hrEmployee)
    {
        hrEmployee.setUpdateTime(DateUtils.getNowDate());
        
        // 同步更新用户信息
        if (hrEmployee.getUserId() != null) {
            SysUser user = new SysUser();
            user.setUserId(hrEmployee.getUserId());
            user.setNickName(hrEmployee.getName());
            user.setPhonenumber(hrEmployee.getPhone());
            user.setEmail(hrEmployee.getEmail());
            user.setSex(hrEmployee.getGender());
            sysUserService.updateUserProfile(user);
        }
        
        return hrEmployeeMapper.updateHrEmployee(hrEmployee);
    }

    /**
     * 批量删除员工主
     * 
     * @param employeeIds 需要删除的员工主主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteHrEmployeeByEmployeeIds(Long[] employeeIds)
    {
        return hrEmployeeMapper.deleteHrEmployeeByEmployeeIds(employeeIds);
    }

    /**
     * 删除员工主信息
     * 
     * @param employeeId 员工主主键
     * @return 结果
     */
    @Override
    public int deleteHrEmployeeByEmployeeId(Long employeeId)
    {
        return hrEmployeeMapper.deleteHrEmployeeByEmployeeId(employeeId);
    }
}
