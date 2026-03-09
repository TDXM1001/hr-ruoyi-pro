import request from '@/utils/http'

export interface SysRole {
  roleId?: number
  roleName: string
  roleKey: string
  roleSort: number
  status: string // '0' 正常, '1' 停用
  remark?: string
  menuIds?: number[]
  createTime?: string
  [key: string]: any
}

// 获取角色列表
export function listRole(query?: any) {
  return request.get<{ rows: SysRole[]; total: number }>({
    url: '/system/role/list',
    params: query
  })
}

// 获取角色详细信息
export function getRole(roleId: number | string) {
  return request.get<SysRole>({ url: '/system/role/' + roleId })
}

// 新增角色
export function addRole(data: SysRole) {
  return request.post({ url: '/system/role', data: data })
}

// 修改角色
export function updateRole(data: SysRole) {
  return request.put({ url: '/system/role', data: data })
}

// 删除角色
export function delRole(roleId: number | string) {
  return request.del({ url: '/system/role/' + roleId })
}

// 角色状态修改
export function changeRoleStatus(roleId: number | string, status: string) {
  const data = { roleId, status }
  return request.put({ url: '/system/role/changeStatus', data: data })
}
