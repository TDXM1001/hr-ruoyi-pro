// src/api/system/notice.ts
import http from '@/utils/http'

/**
 * 查询通知公告列表
 * @param query 查询参数
 */
export function listNotice(query?: any) {
    return http.request({
        url: '/system/notice/list',
        method: 'get',
        params: query
    })
}

/**
 * 查询通知公告详细
 * @param noticeId 公告ID
 */
export function getNotice(noticeId: number | string) {
    return http.request({
        url: '/system/notice/' + noticeId,
        method: 'get'
    })
}

/**
 * 新增通知公告
 * @param data 公告数据
 */
export function addNotice(data: any) {
    return http.request({
        url: '/system/notice',
        method: 'post',
        data: data
    })
}

/**
 * 修改通知公告
 * @param data 公告数据
 */
export function updateNotice(data: any) {
    return http.request({
        url: '/system/notice',
        method: 'put',
        data: data
    })
}

/**
 * 删除通知公告
 * @param noticeId 公告ID
 */
export function delNotice(noticeId: number | string) {
    return http.request({
        url: '/system/notice/' + noticeId,
        method: 'delete'
    })
}
