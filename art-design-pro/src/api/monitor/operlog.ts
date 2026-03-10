// src/api/monitor/operlog.ts
import http from '@/utils/http'

/**
 * 查询操作日志列表
 * @param query 查询参数
 */
export function list(query?: any) {
    return http.request({
        url: '/monitor/operlog/list',
        method: 'get',
        params: query
    })
}

/**
 * 删除操作日志
 * @param operId 日志ID
 */
export function delOperlog(operId: number | string) {
    return http.request({
        url: '/monitor/operlog/' + operId,
        method: 'delete'
    })
}

/**
 * 清空操作日志
 */
export function cleanOperlog() {
    return http.request({
        url: '/monitor/operlog/clean',
        method: 'delete'
    })
}
