// src/api/system/config.ts
import http from '@/utils/http'

/**
 * 查询参数列表
 * @param query 查询参数
 */
export function listConfig(query?: any) {
    return http.request({
        url: '/system/config/list',
        method: 'get',
        params: query
    })
}

/**
 * 查询参数详细
 * @param configId 参数ID
 */
export function getConfig(configId: number | string) {
    return http.request({
        url: '/system/config/' + configId,
        method: 'get'
    })
}

/**
 * 根据参数键名查询参数值
 * @param configKey 参数键名
 */
export function getConfigKey(configKey: string) {
    return http.request({
        url: '/system/config/configKey/' + configKey,
        method: 'get'
    })
}

/**
 * 新增参数配置
 * @param data 参数数据
 */
export function addConfig(data: any) {
    return http.request({
        url: '/system/config',
        method: 'post',
        data: data
    })
}

/**
 * 修改参数配置
 * @param data 参数数据
 */
export function updateConfig(data: any) {
    return http.request({
        url: '/system/config',
        method: 'put',
        data: data
    })
}

/**
 * 删除参数配置
 * @param configId 参数ID
 */
export function delConfig(configId: number | string) {
    return http.request({
        url: '/system/config/' + configId,
        method: 'delete'
    })
}

/**
 * 刷新参数缓存
 */
export function refreshCache() {
    return http.request({
        url: '/system/config/refreshCache',
        method: 'delete'
    })
}
