import request from '@/utils/http'

/**
 * 获取路由数据 (若依版)
 * @returns 返回路由菜单树
 */
export function getRouters() {
  return request.get<any>({
    url: '/getRouters'
  })
}
