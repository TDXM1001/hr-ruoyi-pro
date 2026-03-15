import request from '@/utils/http'

/** 通用上传接口响应。 */
export interface CommonUploadResp {
  url: string
  fileName: string
  newFileName: string
  originalFilename: string
}

/**
 * 上传单个通用文件。
 * 后续资产附件上传直接复用该封装，避免页面层自行拼接 FormData。
 */
export function uploadCommonFile(file: File) {
  const data = new FormData()
  data.append('file', file)

  return request.request<CommonUploadResp>({
    url: '/common/upload',
    method: 'POST',
    data
  })
}
