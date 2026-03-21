import { vi, describe, it, expect, beforeEach } from 'vitest'

vi.mock('@/utils/http', () => {
  return {
    default: {
      request: vi.fn()
    }
  }
})

import http from '@/utils/http'
import {
  addRealEstate,
  addRealEstateOccupancy,
  approveRealEstateRectification,
  changeRealEstateOccupancy,
  completeRealEstateRectification,
  getRealEstateCategoryTree,
  getRealEstateDetail,
  getRealEstateLifecycle,
  getRealEstateRectification,
  getRealEstateList,
  getNextRealEstateCode,
  listRealEstateOccupancies,
  listRealEstateRectificationApprovalRecords,
  listRealEstateRectifications,
  releaseRealEstateOccupancy,
  rejectRealEstateRectificationApproval,
  submitRealEstateRectificationApproval,
  updateRealEstate
} from '../../src/api/asset/real-estate'

describe('Asset Real Estate API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should request /asset/real-estate/list', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce({ rows: [], total: 0 })

    await getRealEstateList({ ownershipCertNo: 'CERT-001' })

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/real-estate/list',
      method: 'get',
      params: { ownershipCertNo: 'CERT-001' }
    })
  })

  it('should request /asset/real-estate/{assetId}', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce({ data: { assetId: 1001 } })

    await getRealEstateDetail(1001)

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/real-estate/1001',
      method: 'get'
    })
  })

  it('should request /asset/real-estate/{assetId}/lifecycle', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce({ data: { ledger: {}, changeLogs: [] } })

    await getRealEstateLifecycle(1001)

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/real-estate/1001/lifecycle',
      method: 'get'
    })
  })

  it('should request create real estate archive', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce({ data: 20001 })

    await addRealEstate({ assetName: '深圳研发办公楼A座' })

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/real-estate',
      method: 'post',
      data: { assetName: '深圳研发办公楼A座' }
    })
  })

  it('should request update real estate archive', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce({ code: 200 })

    await updateRealEstate({ assetId: 20001, assetName: '深圳研发办公楼A座-更新' })

    expect(requestMock).toHaveBeenCalledWith({
      url: '/asset/real-estate',
      method: 'put',
      data: { assetId: 20001, assetName: '深圳研发办公楼A座-更新' }
    })
  })

  it('should request real estate category tree and next code', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce([{ id: 1101, label: '办公用房' }])
    requestMock.mockResolvedValueOnce('RE-2026-0002')

    await getRealEstateCategoryTree()
    const code = await getNextRealEstateCode()

    expect(code).toBe('RE-2026-0002')
    expect(requestMock).toHaveBeenNthCalledWith(1, {
      url: '/asset/real-estate/categoryTree',
      method: 'get'
    })
    expect(requestMock).toHaveBeenNthCalledWith(2, {
      url: '/asset/real-estate/nextCode',
      method: 'get'
    })
  })

  it('should request rectification detail and approval endpoints', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce({ rows: [], total: 0 })
    requestMock.mockResolvedValueOnce({ data: { rectificationId: 9001 } })
    requestMock.mockResolvedValueOnce({ data: [] })
    requestMock.mockResolvedValueOnce({ code: 200 })
    requestMock.mockResolvedValueOnce({ code: 200 })
    requestMock.mockResolvedValueOnce({ code: 200 })
    requestMock.mockResolvedValueOnce({ code: 200 })

    await listRealEstateRectifications(20001)
    await getRealEstateRectification(20001, 9001)
    await listRealEstateRectificationApprovalRecords(20001, 9001)
    await submitRealEstateRectificationApproval(20001, 9001, { opinion: '提交整改审批' })
    await approveRealEstateRectification(20001, 9001, { opinion: '审批通过' })
    await rejectRealEstateRectificationApproval(20001, 9001, { opinion: '审批驳回' })
    await completeRealEstateRectification(20001, 9001, {
      completionDesc: '完成说明',
      acceptanceRemark: '验收备注'
    })

    expect(requestMock).toHaveBeenNthCalledWith(1, {
      url: '/asset/real-estate/20001/rectifications',
      method: 'get'
    })
    expect(requestMock).toHaveBeenNthCalledWith(2, {
      url: '/asset/real-estate/20001/rectifications/9001',
      method: 'get'
    })
    expect(requestMock).toHaveBeenNthCalledWith(3, {
      url: '/asset/real-estate/20001/rectifications/9001/approval-records',
      method: 'get'
    })
    expect(requestMock).toHaveBeenNthCalledWith(4, {
      url: '/asset/real-estate/20001/rectifications/9001/submit-approval',
      method: 'post',
      data: { opinion: '提交整改审批' }
    })
    expect(requestMock).toHaveBeenNthCalledWith(5, {
      url: '/asset/real-estate/20001/rectifications/9001/approve',
      method: 'post',
      data: { opinion: '审批通过' }
    })
    expect(requestMock).toHaveBeenNthCalledWith(6, {
      url: '/asset/real-estate/20001/rectifications/9001/reject',
      method: 'post',
      data: { opinion: '审批驳回' }
    })
    expect(requestMock).toHaveBeenNthCalledWith(7, {
      url: '/asset/real-estate/20001/rectifications/9001/complete',
      method: 'post',
      data: {
        completionDesc: '完成说明',
        acceptanceRemark: '验收备注'
      }
    })
  })

  it('should request occupancy endpoints', async () => {
    const requestMock = vi.mocked(http.request)
    requestMock.mockResolvedValueOnce([])
    requestMock.mockResolvedValueOnce({ data: 9101 })
    requestMock.mockResolvedValueOnce({ data: 9102 })
    requestMock.mockResolvedValueOnce({ code: 200 })

    await listRealEstateOccupancies(20001)
    await addRealEstateOccupancy(20001, {
      useDeptId: 103,
      responsibleUserId: 1,
      locationName: '深圳南山科技园A座',
      startDate: '2026-03-22',
      changeReason: '前端点测'
    })
    await changeRealEstateOccupancy(20001, 9101, {
      useDeptId: 105,
      responsibleUserId: 3,
      locationName: '深圳南山科技园B座',
      startDate: '2026-03-23',
      changeReason: '部门调整'
    })
    await releaseRealEstateOccupancy(20001, 9102, {
      endDate: '2026-03-25',
      releaseReason: '释放占用'
    })

    expect(requestMock).toHaveBeenNthCalledWith(1, {
      url: '/asset/real-estate/20001/occupancies',
      method: 'get'
    })
    expect(requestMock).toHaveBeenNthCalledWith(2, {
      url: '/asset/real-estate/20001/occupancies',
      method: 'post',
      data: {
        useDeptId: 103,
        responsibleUserId: 1,
        locationName: '深圳南山科技园A座',
        startDate: '2026-03-22',
        changeReason: '前端点测'
      }
    })
    expect(requestMock).toHaveBeenNthCalledWith(3, {
      url: '/asset/real-estate/20001/occupancies/9101/change',
      method: 'post',
      data: {
        useDeptId: 105,
        responsibleUserId: 3,
        locationName: '深圳南山科技园B座',
        startDate: '2026-03-23',
        changeReason: '部门调整'
      }
    })
    expect(requestMock).toHaveBeenNthCalledWith(4, {
      url: '/asset/real-estate/20001/occupancies/9102/release',
      method: 'post',
      data: {
        endDate: '2026-03-25',
        releaseReason: '释放占用'
      }
    })
  })
})