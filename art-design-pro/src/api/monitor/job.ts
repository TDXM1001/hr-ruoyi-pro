import http from '@/utils/http'

/**
 * 查询定时任务列表
 * @param query 查询参数
 */
export function listJob(query?: any) {
    return http.request({
        url: '/monitor/job/list',
        method: 'get',
        params: query
    })
}

/**
 * 查询定时任务详细
 * @param jobId 任务ID
 */
export function getJob(jobId: number | string) {
    return http.request({
        url: '/monitor/job/' + jobId,
        method: 'get'
    })
}

/**
 * 新增定时任务
 * @param data 任务数据
 */
export function addJob(data: any) {
    return http.request({
        url: '/monitor/job',
        method: 'post',
        data: data
    })
}

/**
 * 修改定时任务
 * @param data 任务数据
 */
export function updateJob(data: any) {
    return http.request({
        url: '/monitor/job',
        method: 'put',
        data: data
    })
}

/**
 * 删除定时任务
 * @param jobIds 任务ID（多个用逗号分隔）
 */
export function delJob(jobIds: string) {
    return http.request({
        url: '/monitor/job/' + jobIds,
        method: 'delete'
    })
}

/**
 * 任务状态修改
 * @param jobId 任务ID
 * @param status 状态
 */
export function changeJobStatus(jobId: number | string, status: string) {
    const data = {
        jobId,
        status
    }
    return http.request({
        url: '/monitor/job/changeStatus',
        method: 'put',
        data: data
    })
}

/**
 * 定时任务立即执行一次
 * @param jobId 任务ID
 * @param jobGroup 任务组名
 */
export function runJob(jobId: number | string, jobGroup: string) {
    const data = {
        jobId,
        jobGroup
    }
    return http.request({
        url: '/monitor/job/run',
        method: 'put',
        data: data
    })
}

/**
 * 查询定时任务调度日志列表
 * @param query 查询参数
 */
export function listJobLog(query?: any) {
    return http.request({
        url: '/monitor/jobLog/list',
        method: 'get',
        params: query
    })
}

/**
 * 删除调度日志
 * @param jobLogId 日志ID
 */
export function delJobLog(jobLogId: number | string) {
    return http.request({
        url: '/monitor/jobLog/' + jobLogId,
        method: 'delete'
    })
}

/**
 * 清空调度日志
 */
export function cleanJobLog() {
    return http.request({
        url: '/monitor/jobLog/clean',
        method: 'delete'
    })
}
