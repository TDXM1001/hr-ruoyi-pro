// art-design-pro/src/api/system/post.ts
import http from '@/utils/http'

export function listPost(query?: any) {
  return http.request({ url: '/system/post/list', method: 'get', params: query })
}
export function getPost(postId: number | string) {
  return http.request({ url: '/system/post/' + postId, method: 'get' })
}
export function addPost(data: any) {
  return http.request({ url: '/system/post', method: 'post', data: data })
}
export function updatePost(data: any) {
  return http.request({ url: '/system/post', method: 'put', data: data })
}
export function delPost(postId: number | string) {
  return http.request({ url: '/system/post/' + postId, method: 'delete' })
}
