import http from '@/utils/http'

// 获取服务监控数据
export function getServer() {
    return http.request({
        url: '/monitor/server',
        method: 'get'
    })
}
