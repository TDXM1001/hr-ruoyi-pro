import request from '@/utils/request';

// 查询员工列表
export function listEmployee(query: any) {
  return request({
    url: '/hr/employee/list',
    method: 'get',
    params: query
  });
}

// 查询员工详情
export function getEmployee(employeeId: number | string) {
  return request({
    url: '/hr/employee/' + employeeId,
    method: 'get'
  });
}

// 新增员工
export function addEmployee(data: any) {
  return request({
    url: '/hr/employee',
    method: 'post',
    data: data
  });
}

// 修改员工
export function updateEmployee(data: any) {
  return request({
    url: '/hr/employee',
    method: 'put',
    data: data
  });
}

// 删除员工
export function delEmployee(employeeId: number | string) {
  return request({
    url: '/hr/employee/' + employeeId,
    method: 'delete'
  });
}

// 导出员工
export function exportEmployee(query: any) {
  return request({
    url: '/hr/employee/export',
    method: 'post',
    params: query
  });
}
