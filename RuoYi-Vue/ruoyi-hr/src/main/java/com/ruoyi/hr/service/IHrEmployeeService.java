package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrEmployee;

/**
 * 员工主Service接口
 * 
 * @author ruoyi
 */
public interface IHrEmployeeService 
{
    /**
     * 查询员工主
     * 
     * @param employeeId 员工主主键
     * @return 员工主
     */
    public HrEmployee selectHrEmployeeByEmployeeId(Long employeeId);

    /**
     * 查询员工主列表
     * 
     * @param hrEmployee 员工主
     * @return 员工主集合
     */
    public List<HrEmployee> selectHrEmployeeList(HrEmployee hrEmployee);

    /**
     * 新增员工主
     * 
     * @param hrEmployee 员工主
     * @return 结果
     */
    public int insertHrEmployee(HrEmployee hrEmployee);

    /**
     * 修改员工主
     * 
     * @param hrEmployee 员工主
     * @return 结果
     */
    public int updateHrEmployee(HrEmployee hrEmployee);

    /**
     * 批量删除员工主
     * 
     * @param employeeIds 需要删除的员工主主键集合
     * @return 结果
     */
    public int deleteHrEmployeeByEmployeeIds(Long[] employeeIds);

    /**
     * 删除员工主信息
     * 
     * @param employeeId 员工主主键
     * @return 结果
     */
    public int deleteHrEmployeeByEmployeeId(Long employeeId);
}
