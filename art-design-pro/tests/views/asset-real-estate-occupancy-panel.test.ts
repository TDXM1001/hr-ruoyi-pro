import { afterEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import OccupancyPanel from '@/views/asset/real-estate/detail/components/occupancy-panel.vue'

describe('OccupancyPanel occupancy flow', () => {
  afterEach(() => {
    vi.useRealTimers()
  })

  it('shows create entry when no active occupancy exists', async () => {
    const wrapper = mount(OccupancyPanel, {
      props: {
        detailData: {
          assetCode: 'RE-2026-0001',
          ownerDeptName: 'owner-dept'
        },
        occupancyRecords: [],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    expect(wrapper.text()).toContain('\u5f53\u524d\u6709\u6548\u5360\u7528')
    expect(wrapper.text()).toContain('\u6682\u65e0\u6709\u6548\u5360\u7528')
    expect(wrapper.text()).toContain('\u53d1\u8d77\u5360\u7528')
    expect(wrapper.text()).toContain('\u72b6\u6001\u77e9\u9635')
    expect(wrapper.text()).toContain('\u65e0\u6709\u6548\u5360\u7528')
    expect(wrapper.text()).toContain('\u5b58\u5728\u6709\u6548\u5360\u7528')
    expect(wrapper.text()).toContain('\u5df2\u91ca\u653e\u5386\u53f2')

    await wrapper.get('[data-testid="occupancy-create-link"]').trigger('click')
    expect(wrapper.emitted('create-occupancy')?.length).toBe(1)
  })

  it('shows change and release actions when active occupancy exists', async () => {
    const wrapper = mount(OccupancyPanel, {
      props: {
        detailData: {
          assetCode: 'RE-2026-0001',
          ownerDeptName: 'owner-dept'
        },
        occupancyRecords: [
          {
            occupancyId: 9101,
            occupancyNo: 'OCC-2026-9001',
            occupancyStatus: 'ACTIVE',
            useDeptName: 'dept-alpha',
            responsibleUserName: 'user-alpha',
            locationName: 'loc-alpha',
            startDate: '2026-03-22',
            changeReason: 'reason-alpha'
          },
          {
            occupancyId: 9100,
            occupancyNo: 'OCC-2026-8999',
            occupancyStatus: 'RELEASED',
            useDeptName: 'dept-beta',
            responsibleUserName: 'user-beta',
            locationName: 'loc-beta',
            startDate: '2026-03-01',
            endDate: '2026-03-10',
            changeReason: 'reason-beta',
            releaseReason: 'release-beta'
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    expect(wrapper.text()).toContain('OCC-2026-9001')
    expect(wrapper.text()).toContain('OCC-2026-8999')
    expect(wrapper.text()).toContain('\u53d8\u66f4\u5360\u7528')
    expect(wrapper.text()).toContain('\u91ca\u653e\u5360\u7528')

    await wrapper.get('[data-testid="occupancy-change-link-9101"]').trigger('click')
    expect(wrapper.emitted('change-occupancy')?.[0]).toEqual([expect.objectContaining({ occupancyId: 9101 })])

    await wrapper.get('[data-testid="occupancy-release-link-9101"]').trigger('click')
    expect(wrapper.emitted('release-occupancy')?.[0]).toEqual([expect.objectContaining({ occupancyId: 9101 })])
  })

  it('filters occupancy history by status and keyword', async () => {
    const wrapper = mount(OccupancyPanel, {
      props: {
        detailData: {
          assetCode: 'RE-2026-0001',
          ownerDeptName: 'owner-dept'
        },
        occupancyRecords: [
          {
            occupancyId: 9101,
            occupancyNo: 'OCC-2026-9001',
            occupancyStatus: 'ACTIVE',
            useDeptName: 'dept-alpha',
            responsibleUserName: 'user-alpha',
            locationName: 'loc-alpha',
            startDate: '2026-03-22',
            changeReason: 'reason-alpha'
          },
          {
            occupancyId: 9100,
            occupancyNo: 'OCC-2026-8999',
            occupancyStatus: 'RELEASED',
            useDeptName: 'dept-beta',
            responsibleUserName: 'user-beta',
            locationName: 'loc-beta',
            startDate: '2026-03-01',
            endDate: '2026-03-10',
            changeReason: 'reason-beta',
            releaseReason: 'release-beta'
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    const historyList = wrapper.get('[data-testid="occupancy-history-list"]')
    expect(historyList.text()).toContain('OCC-2026-9001')
    expect(historyList.text()).toContain('OCC-2026-8999')

    await wrapper.get('[data-testid="occupancy-filter-released"]').trigger('click')
    expect(historyList.text()).toContain('OCC-2026-8999')
    expect(historyList.text()).not.toContain('OCC-2026-9001')

    await wrapper.get('[data-testid="occupancy-filter-all"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-keyword-input"]').setValue('dept-alpha')
    expect(historyList.text()).toContain('OCC-2026-9001')
    expect(historyList.text()).not.toContain('OCC-2026-8999')
  })

  it('shows ledger sync summary and latest change summary for active occupancy', async () => {
    const wrapper = mount(OccupancyPanel, {
      props: {
        detailData: {
          assetCode: 'RE-2026-0001',
          ownerDeptName: 'owner-dept',
          useDeptName: 'dept-alpha',
          responsibleUserName: 'user-alpha',
          locationName: 'loc-alpha'
        },
        occupancyRecords: [
          {
            occupancyId: 9101,
            occupancyNo: 'OCC-2026-9001',
            occupancyStatus: 'ACTIVE',
            useDeptName: 'dept-alpha',
            responsibleUserName: 'user-alpha',
            locationName: 'loc-alpha',
            startDate: '2026-03-22',
            changeReason: 'reason-alpha'
          },
          {
            occupancyId: 9100,
            occupancyNo: 'OCC-2026-8999',
            occupancyStatus: 'RELEASED',
            useDeptName: 'dept-beta',
            responsibleUserName: 'user-beta',
            locationName: 'loc-beta',
            startDate: '2026-03-01',
            endDate: '2026-03-21',
            changeReason: 'reason-beta',
            releaseReason: 'release-beta'
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    const syncSummary = wrapper.get('[data-testid="occupancy-ledger-sync-summary"]')
    expect(syncSummary.text()).toContain('dept-alpha')
    expect(syncSummary.text()).toContain('user-alpha')
    expect(syncSummary.text()).toContain('loc-alpha')
    expect(syncSummary.text()).toContain('主档已同步')

    const latestChangeSummary = wrapper.get('[data-testid="occupancy-last-change-summary"]')
    expect(latestChangeSummary.text()).toContain('OCC-2026-8999')
    expect(latestChangeSummary.text()).toContain('dept-beta')
    expect(latestChangeSummary.text()).toContain('dept-alpha')
  })

  it('shows release guidance when no active occupancy exists but release history remains', async () => {
    const wrapper = mount(OccupancyPanel, {
      props: {
        detailData: {
          assetCode: 'RE-2026-0001',
          ownerDeptName: 'owner-dept'
        },
        occupancyRecords: [
          {
            occupancyId: 9102,
            occupancyNo: 'OCC-2026-9002',
            occupancyStatus: 'RELEASED',
            useDeptName: 'dept-gamma',
            responsibleUserName: 'user-gamma',
            locationName: 'loc-gamma',
            startDate: '2026-03-01',
            endDate: '2026-03-22',
            changeReason: 'reason-gamma',
            releaseReason: 'release-gamma'
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    const releasedSummary = wrapper.get('[data-testid="occupancy-empty-released-summary"]')
    expect(releasedSummary.text()).toContain('OCC-2026-9002')
    expect(releasedSummary.text()).toContain('release-gamma')

    await wrapper.get('[data-testid="occupancy-focus-released-link"]').trigger('click')
    expect(wrapper.get('[data-testid="occupancy-filter-released"]').classes()).toContain(
      'el-button--primary'
    )
  })

  it('filters occupancy history by time window', async () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-22T10:00:00+08:00'))

    const wrapper = mount(OccupancyPanel, {
      props: {
        detailData: {
          assetCode: 'RE-2026-0001',
          ownerDeptName: 'owner-dept'
        },
        occupancyRecords: [
          {
            occupancyId: 9101,
            occupancyNo: 'OCC-2026-9001',
            occupancyStatus: 'ACTIVE',
            useDeptName: 'dept-alpha',
            responsibleUserName: 'user-alpha',
            locationName: 'loc-alpha',
            startDate: '2026-03-20',
            changeReason: 'reason-alpha'
          },
          {
            occupancyId: 9100,
            occupancyNo: 'OCC-2026-8999',
            occupancyStatus: 'RELEASED',
            useDeptName: 'dept-beta',
            responsibleUserName: 'user-beta',
            locationName: 'loc-beta',
            startDate: '2026-03-01',
            endDate: '2026-03-10',
            changeReason: 'reason-beta',
            releaseReason: 'release-beta'
          },
          {
            occupancyId: 9099,
            occupancyNo: 'OCC-2025-8001',
            occupancyStatus: 'RELEASED',
            useDeptName: 'dept-old',
            responsibleUserName: 'user-old',
            locationName: 'loc-old',
            startDate: '2025-11-01',
            endDate: '2025-12-01',
            changeReason: 'reason-old',
            releaseReason: 'release-old'
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    const historyList = wrapper.get('[data-testid="occupancy-history-list"]')
    expect(historyList.text()).toContain('OCC-2025-8001')

    await wrapper.get('[data-testid="occupancy-time-30d"]').trigger('click')
    expect(historyList.text()).toContain('OCC-2026-9001')
    expect(historyList.text()).toContain('OCC-2026-8999')
    expect(historyList.text()).not.toContain('OCC-2025-8001')
  })
})
