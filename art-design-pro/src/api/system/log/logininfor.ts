// src/api/system/log/logininfor.ts
import http from '@/utils/http'

/**
 * 查询登录日志列表
 * @param query 查询参数
 */
export function list(query?: any) {
    return http.request({
        url: '/monitor/logininfor/list',
        method: 'get',
        params: query
    })
}

/**
 * 删除登录日志
 * @param infoId 日志ID
 */
export function delLogininfor(infoId: number | string) {
    return http.request({
        url: '/monitor/logininfor/' + infoId,
        method: 'delete'
    })
}

/**
 * 清空登录日志
 */
export function cleanLogininfor() {
    return http.request({
        url: '/monitor/logininfor/clean',
        method: 'delete'
    })
}

/**
 * 解锁用户登录状态
 * @param userName 用户名
 */
export function unlockLogininfor(userName: string) {
    return http.request({
        url: '/monitor/logininfor/unlock/' + userName,
        method: 'get'
    })
}
