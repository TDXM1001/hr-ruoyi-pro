import http from '@/utils/http'

/**
 * 查询在线用户列表
 * @param query 查询参数
 */
export function listOnline(query?: any) {
    return http.request({
        url: '/monitor/online/list',
        method: 'get',
        params: query
    })
}

/**
 * 强退用户
 * @param tokenId 会话ID
 */
export function forceLogout(tokenId: string) {
    return http.request({
        url: '/monitor/online/' + tokenId,
        method: 'delete'
    })
}
