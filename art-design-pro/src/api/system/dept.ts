import http from '@/utils/http'

export function listDept(query?: any) {
  return http.request({ url: '/system/dept/list', method: 'get', params: query })
}
export function listDeptExcludeChild(deptId: number | string) {
  return http.request({ url: '/system/dept/list/exclude/' + deptId, method: 'get' })
}
export function getDept(deptId: number | string) {
  return http.request({ url: '/system/dept/' + deptId, method: 'get' })
}
export function addDept(data: any) {
  return http.request({ url: '/system/dept', method: 'post', data: data })
}
export function updateDept(data: any) {
  return http.request({ url: '/system/dept', method: 'put', data: data })
}
export function delDept(deptId: number | string) {
  return http.request({ url: '/system/dept/' + deptId, method: 'delete' })
}
