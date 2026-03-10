import http from '@/utils/http'

// 查询缓存监控
export function getCache() {
    return http.request({
        url: '/monitor/cache',
        method: 'get'
    })
}

// 查询缓存名称列表
export function listCacheName() {
    return http.request({
        url: '/monitor/cache/getNames',
        method: 'get'
    })
}

// 查询缓存键名列表
export function listCacheKey(cacheName: string) {
    return http.request({
        url: '/monitor/cache/getKeys/' + cacheName,
        method: 'get'
    })
}

// 查询缓存内容
export function getCacheValue(cacheName: string, cacheKey: string) {
    return http.request({
        url: '/monitor/cache/getValue/' + cacheName + '/' + cacheKey,
        method: 'get'
    })
}

// 清理指定名称缓存
export function clearCacheName(cacheName: string) {
    return http.request({
        url: '/monitor/cache/clearCacheName/' + cacheName,
        method: 'delete'
    })
}

// 清理指定键名缓存
export function clearCacheKey(cacheKey: string) {
    return http.request({
        url: '/monitor/cache/clearCacheKey/' + cacheKey,
        method: 'delete'
    })
}

// 清理全部缓存
export function clearCacheAll() {
    return http.request({
        url: '/monitor/cache/clearCacheAll',
        method: 'delete'
    })
}
