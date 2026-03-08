import request from '@/utils/http'
import { AppRouteRecord } from '@/types/router'

/**
 * @deprecated 请改为使用 '@/api/system/user' 中的 listUser
 * 获取用户列表
 */
export function fetchGetUserList(params: Api.SystemManage.UserSearchParams) {
  return request.get<Api.SystemManage.UserList>({
    url: '/system/user/list',
    params
  })
}

/**
 * @deprecated 请改为使用 '@/api/system/role' 中的 listRole
 * 获取角色列表
 */
export function fetchGetRoleList(params: Api.SystemManage.RoleSearchParams) {
  return request.get<Api.SystemManage.RoleList>({
    url: '/system/role/list',
    params
  })
}

/**
 * @deprecated 请改为使用 '@/api/system/menu' 中的 listMenu
 * 获取菜单列表
 */
export function fetchGetMenuList() {
  return request.get<AppRouteRecord[]>({
    url: '/system/menu/list'
  })
}
