import request from '@/utils/http'

/**
 * 菜单实体 definition (对应若依 SysMenu)
 */
export interface SysMenu {
  /** 菜单ID */
  menuId: number
  /** 菜单名称 */
  menuName: string
  /** 父菜单ID */
  parentId: number
  /** 显示顺序 */
  orderNum: number
  /** 路由地址 */
  path: string
  /** 组件路径 */
  component: string
  /** 路由参数 */
  query?: string
  /** 是否为外链（0是 1否） */
  isFrame: string
  /** 是否缓存（0缓存 1不缓存） */
  isCache: string
  /** 菜单类型（M目录 C菜单 F按钮） */
  menuType: 'M' | 'C' | 'F'
  /** 菜单状态（0显示 1隐藏） */
  visible: string
  /** 菜单状态（0正常 1停用） */
  status: string
  /** 权限标识 */
  perms?: string
  /** 菜单图标 */
  icon?: string
  /** 子菜单 */
  children?: SysMenu[]
  /** 创建时间 */
  createTime?: string
}

/**
 * 查询菜单列表
 * @param params 查询参数 (menuName, status 等)
 */
export function listMenu(params?: any) {
  return request.get<SysMenu[]>({
    url: '/system/menu/list',
    params
  })
}

/**
 * 查询菜单树结构
 * @returns 菜单树数据
 */
export function treeselect() {
  return request.get<any[]>({
    url: '/system/menu/treeselect'
  })
}

/**
 * 获取菜单详细信息
 * @param menuId 菜单ID
 */
export function getMenu(menuId: number) {
  return request.get<SysMenu>({
    url: `/system/menu/${menuId}`
  })
}

/**
 * 新增菜单
 * @param data 菜单数据
 */
export function addMenu(data: Partial<SysMenu>) {
  return request.post({
    url: '/system/menu',
    data
  })
}

/**
 * 修改菜单
 * @param data 菜单数据
 */
export function updateMenu(data: Partial<SysMenu>) {
  return request.put({
    url: '/system/menu',
    data
  })
}

/**
 * 根据角色ID查询菜单树结构
 * @param roleId 角色ID
 */
export function roleMenuTreeselect(roleId: number | string) {
  return request.get<{ checkedKeys: number[]; menus: any[] }>({
    url: '/system/menu/roleMenuTreeselect/' + roleId
  })
}

/**
 * 删除菜单
 * @param menuId 菜单ID
 */
export function delMenu(menuId: number) {
  return request.del({
    url: `/system/menu/${menuId}`
  })
}
