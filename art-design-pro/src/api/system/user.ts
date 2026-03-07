// art-design-pro/src/api/system/user.ts
import http from '@/utils/http'

export function listUser(query?: any) {
  return http.request({ url: '/system/user/list', method: 'get', params: query })
}
export function getUser(userId?: number | string) {
  const url = userId ? '/system/user/' + userId : '/system/user/'
  return http.request({ url, method: 'get', returnFullResponse: true })
}
export function addUser(data: any) {
  return http.request({ url: '/system/user', method: 'post', data: data })
}
export function updateUser(data: any) {
  return http.request({ url: '/system/user', method: 'put', data: data })
}
export function delUser(userId: number | string) {
  return http.request({ url: '/system/user/' + userId, method: 'delete' })
}
export function deptTreeSelect() {
  return http.request({ url: '/system/user/deptTree', method: 'get' })
}
