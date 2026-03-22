import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import OccupancyPanel from '@/views/asset/real-estate/detail/components/occupancy-panel.vue'

describe('OccupancyPanel occupancy flow', () => {
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
})
