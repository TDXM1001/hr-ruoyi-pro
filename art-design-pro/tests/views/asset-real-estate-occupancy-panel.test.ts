import { afterEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus, { ElMessageBox } from 'element-plus'
import { nextTick } from 'vue'
import OccupancyPanel from '@/views/asset/real-estate/detail/components/occupancy-panel.vue'

describe('OccupancyPanel occupancy flow', () => {
  afterEach(() => {
    vi.useRealTimers()
    vi.restoreAllMocks()
    window.localStorage.clear()
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
    expect(wrapper.emitted('change-occupancy')?.[0]).toEqual([
      expect.objectContaining({ occupancyId: 9101 })
    ])

    await wrapper.get('[data-testid="occupancy-release-link-9101"]').trigger('click')
    expect(wrapper.emitted('release-occupancy')?.[0]).toEqual([
      expect.objectContaining({ occupancyId: 9101 })
    ])
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
    expect(syncSummary.text()).toContain('\u4e3b\u6863\u5df2\u540c\u6b65')

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

  it('filters occupancy history by custom time range', async () => {
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
            occupancyNo: 'OCC-2026-8998',
            occupancyStatus: 'RELEASED',
            useDeptName: 'dept-gamma',
            responsibleUserName: 'user-gamma',
            locationName: 'loc-gamma',
            startDate: '2026-03-15',
            endDate: '2026-03-16',
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

    await wrapper.get('[data-testid="occupancy-custom-start"]').setValue('2026-03-12')
    await wrapper.get('[data-testid="occupancy-custom-end"]').setValue('2026-03-20')
    await wrapper.get('[data-testid="occupancy-custom-apply"]').trigger('click')

    const historyList = wrapper.get('[data-testid="occupancy-history-list"]')
    expect(historyList.text()).toContain('OCC-2026-9001')
    expect(historyList.text()).toContain('OCC-2026-8998')
    expect(historyList.text()).not.toContain('OCC-2026-8999')
  })

  it('switches occupancy history sort order', async () => {
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
            occupancyNo: 'OCC-2026-8998',
            occupancyStatus: 'RELEASED',
            useDeptName: 'dept-gamma',
            responsibleUserName: 'user-gamma',
            locationName: 'loc-gamma',
            startDate: '2026-03-15',
            endDate: '2026-03-16',
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

    const getTitles = () =>
      wrapper
        .findAll('.record-item__title')
        .map((node) => node.text())

    expect(getTitles()[0]).toBe('OCC-2026-9001')

    await wrapper.get('[data-testid="occupancy-sort-asc"]').trigger('click')
    expect(getTitles()[0]).toBe('OCC-2026-8999')
  })

  it('highlights diff items in summary cards', async () => {
    const wrapper = mount(OccupancyPanel, {
      props: {
        detailData: {
          assetCode: 'RE-2026-0001',
          ownerDeptName: 'owner-dept',
          useDeptName: 'dept-ledger',
          responsibleUserName: 'user-ledger',
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
            responsibleUserName: 'user-alpha',
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

    expect(wrapper.get('[data-testid="occupancy-ledger-sync-item-useDeptName"]').classes()).toContain(
      'compare-item--changed'
    )
    expect(
      wrapper.get('[data-testid="occupancy-ledger-sync-item-responsibleUserName"]').classes()
    ).toContain('compare-item--changed')
    expect(
      wrapper.get('[data-testid="occupancy-last-change-item-locationName"]').classes()
    ).toContain('compare-item--changed')
    expect(
      wrapper.get('[data-testid="occupancy-last-change-item-responsibleUserName"]').classes()
    ).not.toContain('compare-item--changed')
  })

  it('links summaries to corresponding occupancy history records', async () => {
    if (!HTMLElement.prototype.scrollIntoView) {
      Object.defineProperty(HTMLElement.prototype, 'scrollIntoView', {
        value: () => undefined,
        configurable: true,
        writable: true
      })
    }
    const scrollSpy = vi
      .spyOn(HTMLElement.prototype, 'scrollIntoView')
      .mockImplementation(() => undefined)

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

    await wrapper.get('[data-testid="occupancy-last-change-summary"]').trigger('click')
    await nextTick()
    expect(wrapper.get('[data-testid="occupancy-filter-released"]').classes()).toContain(
      'el-button--primary'
    )
    expect(wrapper.get('[data-testid="occupancy-record-9100"]').classes()).toContain(
      'record-item--focused'
    )
    expect(scrollSpy).toHaveBeenCalled()

    await wrapper.get('[data-testid="occupancy-ledger-sync-summary"]').trigger('click')
    await nextTick()
    expect(wrapper.get('[data-testid="occupancy-filter-active"]').classes()).toContain(
      'el-button--primary'
    )
    expect(wrapper.get('[data-testid="occupancy-record-9101"]').classes()).toContain(
      'record-item--focused'
    )
  })

  it('restores persisted occupancy filters for the same asset', async () => {
    const props = {
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
        }
      ],
      canEdit: true
    }

    const firstWrapper = mount(OccupancyPanel, {
      props,
      global: {
        plugins: [ElementPlus]
      }
    })

    await firstWrapper.get('[data-testid="occupancy-filter-released"]').trigger('click')
    await firstWrapper.get('[data-testid="occupancy-keyword-input"]').setValue('dept-beta')
    await firstWrapper.get('[data-testid="occupancy-custom-start"]').setValue('2026-03-01')
    await firstWrapper.get('[data-testid="occupancy-custom-end"]').setValue('2026-03-15')
    await firstWrapper.get('[data-testid="occupancy-custom-apply"]').trigger('click')
    await firstWrapper.get('[data-testid="occupancy-sort-asc"]').trigger('click')
    firstWrapper.unmount()

    const secondWrapper = mount(OccupancyPanel, {
      props,
      global: {
        plugins: [ElementPlus]
      }
    })

    expect(secondWrapper.get('[data-testid="occupancy-filter-released"]').classes()).toContain(
      'el-button--primary'
    )
    expect(secondWrapper.get('[data-testid="occupancy-sort-asc"]').classes()).toContain(
      'el-button--primary'
    )
    expect(
      (secondWrapper.get('[data-testid="occupancy-keyword-input"]').element as HTMLInputElement).value
    ).toBe('dept-beta')
    expect(
      (secondWrapper.get('[data-testid="occupancy-custom-start"]').element as HTMLInputElement).value
    ).toBe('2026-03-01')
    expect(
      (secondWrapper.get('[data-testid="occupancy-custom-end"]').element as HTMLInputElement).value
    ).toBe('2026-03-15')
    expect(secondWrapper.get('[data-testid="occupancy-history-list"]').text()).toContain(
      'OCC-2026-8999'
    )
    expect(secondWrapper.get('[data-testid="occupancy-history-list"]').text()).not.toContain(
      'OCC-2026-9001'
    )
  })

  it('exports current filtered occupancy history as csv', async () => {
    const createObjectURL = vi.fn(() => 'blob:occupancy-export')
    const revokeObjectURL = vi.fn()
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined)
    URL.createObjectURL = createObjectURL
    URL.revokeObjectURL = revokeObjectURL

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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-filter-released"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-keyword-input"]').setValue('dept-beta')
    await wrapper.get('[data-testid="occupancy-export-link"]').trigger('click')

    expect(createObjectURL).toHaveBeenCalledTimes(1)
    const exportedBlob = createObjectURL.mock.calls[0][0] as Blob
    const content = await exportedBlob.text()

    expect(content).toContain('OCC-2026-8999')
    expect(content).not.toContain('OCC-2026-9001')
    expect(clickSpy).toHaveBeenCalledTimes(1)
    expect(revokeObjectURL).toHaveBeenCalledTimes(1)
  })

  it('configures export fields before exporting occupancy history', async () => {
    const createObjectURL = vi.fn(() => 'blob:occupancy-export')
    const revokeObjectURL = vi.fn()
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined)
    URL.createObjectURL = createObjectURL
    URL.revokeObjectURL = revokeObjectURL

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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-export-field-changeReason"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-export-field-releaseReason"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-export-link"]').trigger('click')

    const exportedBlob = createObjectURL.mock.calls[0][0] as Blob
    const content = await exportedBlob.text()

    expect(content).toContain('占用单号')
    expect(content).toContain('使用部门')
    expect(content).not.toContain('发起/变更原因')
    expect(content).not.toContain('閲婃斁鍘熷洜')
    expect(clickSpy).toHaveBeenCalledTimes(1)
    expect(revokeObjectURL).toHaveBeenCalledTimes(1)
  })

  it('switches occupancy history to grouped view', async () => {
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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-view-grouped"]').trigger('click')

    expect(wrapper.get('[data-testid="occupancy-group-ACTIVE"]').text()).toContain('有效占用')
    expect(wrapper.get('[data-testid="occupancy-group-RELEASED"]').text()).toContain('已释放')
  })

  it('applies linked filter shortcuts from matrix cards', async () => {
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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-shortcut-released"]').trigger('click')
    expect(wrapper.get('[data-testid="occupancy-filter-released"]').classes()).toContain(
      'el-button--primary'
    )
    expect(wrapper.get('[data-testid="occupancy-history-list"]').text()).toContain('OCC-2026-8999')
    expect(wrapper.get('[data-testid="occupancy-history-list"]').text()).not.toContain(
      'OCC-2026-9001'
    )
  })

  it('applies export presets before exporting occupancy history', async () => {
    const createObjectURL = vi.fn(() => 'blob:occupancy-export')
    const revokeObjectURL = vi.fn()
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined)
    URL.createObjectURL = createObjectURL
    URL.revokeObjectURL = revokeObjectURL

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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-export-preset-release"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-export-link"]').trigger('click')

    const exportedBlob = createObjectURL.mock.calls[0][0] as Blob
    const content = await exportedBlob.text()

    expect(content).toContain('释放原因')
    expect(content).toContain('发起/变更原因')
    expect(content).not.toContain('责任人')
    expect(clickSpy).toHaveBeenCalledTimes(1)
    expect(revokeObjectURL).toHaveBeenCalledTimes(1)
  })

  it('switches occupancy history to annotation view', async () => {
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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-view-annotation"]').trigger('click')

    const annotationList = wrapper.get('[data-testid="occupancy-annotation-list"]')
    expect(annotationList.text()).toContain('轨迹批注')
    expect(wrapper.get('[data-testid="occupancy-annotation-card-9100"]').text()).toContain(
      'release-beta'
    )
  })

  it('emits cross-tab switch events from occupancy quick links', async () => {
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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-tab-link-inspection"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-tab-link-rectification"]').trigger('click')

    expect(wrapper.emitted('switch-tab')?.[0]).toEqual(['inspection'])
    expect(wrapper.emitted('switch-tab')?.[1]).toEqual(['rectification'])
  })

  it('renames export preset labels and restores them after remount', async () => {
    const props = {
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
        }
      ],
      canEdit: true
    }

    const firstWrapper = mount(OccupancyPanel, {
      props,
      global: {
        plugins: [ElementPlus]
      }
    })

    await firstWrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    await firstWrapper.get('[data-testid="occupancy-preset-name-toggle"]').trigger('click')
    await firstWrapper.get('[data-testid="occupancy-preset-name-release"]').setValue('閲婃斁鍥為【')
    await firstWrapper.get('[data-testid="occupancy-preset-name-apply"]').trigger('click')

    expect(firstWrapper.get('[data-testid="occupancy-export-preset-release"]').text()).toContain(
      '閲婃斁鍥為【'
    )

    firstWrapper.unmount()

    const secondWrapper = mount(OccupancyPanel, {
      props,
      global: {
        plugins: [ElementPlus]
      }
    })

    await secondWrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    expect(secondWrapper.get('[data-testid="occupancy-export-preset-release"]').text()).toContain(
      '閲婃斁鍥為【'
    )
  })

  it('exports annotation view as csv', async () => {
    const createObjectURL = vi.fn(() => 'blob:occupancy-annotation-export')
    const revokeObjectURL = vi.fn()
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined)
    URL.createObjectURL = createObjectURL
    URL.revokeObjectURL = revokeObjectURL

    const wrapper = mount(OccupancyPanel, {
      props: {
        detailData: {
          assetCode: 'RE-2026-0001',
          ownerDeptName: 'owner-dept'
        },
        occupancyRecords: [
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

    await wrapper.get('[data-testid="occupancy-view-annotation"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-export-annotation-link"]').trigger('click')

    const exportedBlob = createObjectURL.mock.calls[0][0] as Blob
    const content = await exportedBlob.text()

    expect(content).toContain('状态说明')
    expect(content).toContain('占用批注')
    expect(content).toContain('释放批注')
    expect(content).toContain('release-beta')
    expect(clickSpy).toHaveBeenCalledTimes(1)
    expect(revokeObjectURL).toHaveBeenCalledTimes(1)
  })

  it('duplicates system preset into custom preset and restores it after remount', async () => {
    const props = {
      detailData: {
        assetCode: 'RE-2026-0001',
        ownerDeptName: 'owner-dept'
      },
      occupancyRecords: [
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
    }

    const firstWrapper = mount(OccupancyPanel, {
      props,
      global: {
        plugins: [ElementPlus]
      }
    })

    await firstWrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    await firstWrapper.get('[data-testid="occupancy-preset-copy-toggle"]').trigger('click')
    await firstWrapper.get('[data-testid="occupancy-preset-copy-source-release"]').trigger('click')
    await firstWrapper.get('[data-testid="occupancy-preset-copy-name"]').setValue('閲婃斁澶嶇洏澧炲己')
    await firstWrapper.get('[data-testid="occupancy-preset-copy-apply"]').trigger('click')

    expect(firstWrapper.get('[data-testid="occupancy-export-preset-custom-0"]').text()).toContain(
      '閲婃斁澶嶇洏澧炲己'
    )

    await firstWrapper.get('[data-testid="occupancy-export-field-releaseReason"]').trigger('click')
    expect(
      firstWrapper.get('[data-testid="occupancy-export-field-releaseReason"]').classes()
    ).not.toContain('export-field-chip--active')

    await firstWrapper.get('[data-testid="occupancy-export-preset-custom-0"]').trigger('click')
    expect(
      firstWrapper.get('[data-testid="occupancy-export-field-releaseReason"]').classes()
    ).toContain('export-field-chip--active')

    firstWrapper.unmount()

    const secondWrapper = mount(OccupancyPanel, {
      props,
      global: {
        plugins: [ElementPlus]
      }
    })

    await secondWrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    expect(secondWrapper.get('[data-testid="occupancy-export-preset-custom-0"]').text()).toContain(
      '閲婃斁澶嶇洏澧炲己'
    )
  })

  it('switches annotation templates and uses the selected template during export', async () => {
    const createObjectURL = vi.fn(() => 'blob:occupancy-annotation-export-template')
    const revokeObjectURL = vi.fn()
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined)
    URL.createObjectURL = createObjectURL
    URL.revokeObjectURL = revokeObjectURL

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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-view-annotation"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-annotation-template-audit"]').trigger('click')

    const annotationList = wrapper.get('[data-testid="occupancy-annotation-list"]')
    expect(annotationList.text()).toContain('审计视角')

    await wrapper.get('[data-testid="occupancy-export-annotation-link"]').trigger('click')

    const exportedBlob = createObjectURL.mock.calls[0][0] as Blob
    const content = await exportedBlob.text()

    expect(content).toContain('审计视角')
    expect(clickSpy).toHaveBeenCalledTimes(1)
    expect(revokeObjectURL).toHaveBeenCalledTimes(1)
  })

  it('edits custom preset name and overwrites its fields with current selection', async () => {
    const props = {
      detailData: {
        assetCode: 'RE-2026-0001',
        ownerDeptName: 'owner-dept'
      },
      occupancyRecords: [
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
    }

    const wrapper = mount(OccupancyPanel, {
      props,
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-copy-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-copy-source-release"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-copy-name"]').setValue('閲婃斁澶嶇洏澧炲己')
    await wrapper.get('[data-testid="occupancy-preset-copy-apply"]').trigger('click')

    await wrapper.get('[data-testid="occupancy-export-field-releaseReason"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-custom-preset-edit-0"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-copy-name"]').setValue('閲婃斁澶嶇洏缁堢増')
    await wrapper.get('[data-testid="occupancy-preset-copy-save"]').trigger('click')

    expect(wrapper.get('[data-testid="occupancy-export-preset-custom-0"]').text()).toContain(
      '閲婃斁澶嶇洏缁堢増'
    )

    await wrapper.get('[data-testid="occupancy-export-field-occupancyStatus"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-export-preset-custom-0"]').trigger('click')

    expect(
      wrapper.get('[data-testid="occupancy-export-field-releaseReason"]').classes()
    ).not.toContain('export-field-chip--active')
    expect(
      wrapper.get('[data-testid="occupancy-export-field-occupancyStatus"]').classes()
    ).toContain('export-field-chip--active')
  })

  it('deletes custom preset and does not restore it after remount', async () => {
    const props = {
      detailData: {
        assetCode: 'RE-2026-0001',
        ownerDeptName: 'owner-dept'
      },
      occupancyRecords: [
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
    }

    const firstWrapper = mount(OccupancyPanel, {
      props,
      global: {
        plugins: [ElementPlus]
      }
    })

    await firstWrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    await firstWrapper.get('[data-testid="occupancy-preset-copy-toggle"]').trigger('click')
    await firstWrapper.get('[data-testid="occupancy-preset-copy-source-release"]').trigger('click')
    await firstWrapper.get('[data-testid="occupancy-preset-copy-name"]').setValue('待删除预设')
    await firstWrapper.get('[data-testid="occupancy-preset-copy-apply"]').trigger('click')

    expect(firstWrapper.find('[data-testid="occupancy-export-preset-custom-0"]').exists()).toBe(true)

    await firstWrapper.get('[data-testid="occupancy-custom-preset-delete-0"]').trigger('click')
    expect(firstWrapper.find('[data-testid="occupancy-export-preset-custom-0"]').exists()).toBe(false)

    firstWrapper.unmount()

    const secondWrapper = mount(OccupancyPanel, {
      props,
      global: {
        plugins: [ElementPlus]
      }
    })

    await secondWrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    expect(secondWrapper.find('[data-testid="occupancy-export-preset-custom-0"]').exists()).toBe(false)
  })

  it('shows template preview content for the selected annotation template', async () => {
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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-view-annotation"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-annotation-template-manager"]').trigger('click')

    expect(wrapper.get('[data-testid="occupancy-annotation-preview"]').text()).toContain('管理视角')

    await wrapper.get('[data-testid="occupancy-annotation-template-audit"]').trigger('click')
    expect(wrapper.get('[data-testid="occupancy-annotation-preview"]').text()).toContain('审计视角')
  })

  it('tracks cross-tab link statistics and restores them after remount', async () => {
    const props = {
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
        }
      ],
      canEdit: true
    }

    const firstWrapper = mount(OccupancyPanel, {
      props,
      global: {
        plugins: [ElementPlus]
      }
    })

    await firstWrapper.get('[data-testid="occupancy-tab-link-inspection"]').trigger('click')
    await firstWrapper.get('[data-testid="occupancy-tab-link-inspection"]').trigger('click')
    await firstWrapper.get('[data-testid="occupancy-tab-link-rectification"]').trigger('click')

    expect(firstWrapper.get('[data-testid="occupancy-link-stat-inspection"]').text()).toContain('2')
    expect(firstWrapper.get('[data-testid="occupancy-link-stat-rectification"]').text()).toContain('1')
    expect(firstWrapper.get('[data-testid="occupancy-link-stat-last-target"]').text()).toContain(
      '看整改进展'
    )

    firstWrapper.unmount()

    const secondWrapper = mount(OccupancyPanel, {
      props,
      global: {
        plugins: [ElementPlus]
      }
    })

    expect(secondWrapper.get('[data-testid="occupancy-link-stat-inspection"]').text()).toContain('2')
    expect(secondWrapper.get('[data-testid="occupancy-link-stat-rectification"]').text()).toContain('1')
    expect(secondWrapper.get('[data-testid="occupancy-link-stat-last-target"]').text()).toContain(
      '看整改进展'
    )
  })

  it('exports custom presets as json', async () => {
    const createObjectURL = vi.fn(() => 'blob:occupancy-preset-export')
    const revokeObjectURL = vi.fn()
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => undefined)
    URL.createObjectURL = createObjectURL
    URL.revokeObjectURL = revokeObjectURL

    const wrapper = mount(OccupancyPanel, {
      props: {
        detailData: {
          assetCode: 'RE-2026-0001',
          ownerDeptName: 'owner-dept'
        },
        occupancyRecords: [
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

    await wrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-copy-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-copy-source-release"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-copy-name"]').setValue('閲婃斁澶嶇洏瀵煎嚭')
    await wrapper.get('[data-testid="occupancy-preset-copy-apply"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-export-link"]').trigger('click')

    const exportedBlob = createObjectURL.mock.calls[0][0] as Blob
    const content = await exportedBlob.text()

    expect(content).toContain('"version":1')
    expect(content).toContain('閲婃斁澶嶇洏瀵煎嚭')
    expect(clickSpy).toHaveBeenCalledTimes(1)
    expect(revokeObjectURL).toHaveBeenCalledTimes(1)
  })

  it('imports custom presets from json and restores them after remount', async () => {
    const props = {
      detailData: {
        assetCode: 'RE-2026-0001',
        ownerDeptName: 'owner-dept'
      },
      occupancyRecords: [
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
    }

    const wrapper = mount(OccupancyPanel, {
      props,
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-input"]').setValue(`{
  "version": 1,
  "presets": [
    {
      "label": "瀵煎叆杩愯惀鎽樿",
      "fields": ["occupancyNo", "useDeptName", "locationName"]
    }
  ]
}`)
    await wrapper.get('[data-testid="occupancy-preset-import-preview"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-apply"]').trigger('click')

    expect(wrapper.get('[data-testid="occupancy-export-preset-custom-0"]').text()).toContain(
      '瀵煎叆杩愯惀鎽樿'
    )

    wrapper.unmount()

    const remountWrapper = mount(OccupancyPanel, {
      props,
      global: {
        plugins: [ElementPlus]
      }
    })

    await remountWrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    expect(remountWrapper.get('[data-testid="occupancy-export-preset-custom-0"]').text()).toContain(
      '瀵煎叆杩愯惀鎽樿'
    )
  })

  it('shows annotation template compare panel and highlights differences', async () => {
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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-view-annotation"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-annotation-template-standard"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-annotation-compare-target-audit"]').trigger('click')

    const comparePanel = wrapper.get('[data-testid="occupancy-annotation-compare"]')
    expect(comparePanel.text()).toContain('标准模板')
    expect(comparePanel.text()).toContain('审计视角')
    expect(
      wrapper.get('[data-testid="occupancy-annotation-compare-item-status"]').classes()
    ).toContain('annotation-compare-item--changed')
  })

  it('shows 7 day link trend chart from persisted events', async () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-21T12:00:00+08:00'))
    const currentTime = new Date()
    const oneHourAgo = new Date(currentTime.getTime() - 60 * 60 * 1000).toISOString()
    const twoHoursAgo = new Date(currentTime.getTime() - 2 * 60 * 60 * 1000).toISOString()
    const yesterday = new Date(currentTime.getTime() - 24 * 60 * 60 * 1000).toISOString()
    window.localStorage.setItem(
      'asset-real-estate-occupancy-link-stats:RE-2026-0001',
      JSON.stringify({
        counts: {
          overview: 1,
          inspection: 2,
          rectification: 1,
          disposal: 0
        },
        lastTargetKey: 'rectification',
        lastTargetLabel: '看整改进展',
        events: [
          {
            targetKey: 'inspection',
            targetLabel: '鐪嬪贰妫€鑱斿姩',
            occurredAt: oneHourAgo
          },
          {
            targetKey: 'inspection',
            targetLabel: '鐪嬪贰妫€鑱斿姩',
            occurredAt: twoHoursAgo
          },
          {
            targetKey: 'rectification',
            targetLabel: '看整改进展',
            occurredAt: yesterday
          }
        ]
      })
    )

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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    const trendChart = wrapper.get('[data-testid="occupancy-link-trend-chart"]')
    const vm = wrapper.vm as any

    expect(trendChart.text()).toContain('近 7 天联动趋势')
    expect(vm.linkTrendItems).toHaveLength(7)
    expect(vm.linkTrendItems.some((item: any) => item.count > 0)).toBe(true)
    expect(vm.linkTrendItems.some((item: any) => item.topLabel !== '鏆傛棤鑱斿姩')).toBe(true)
  })
  it('previews imported presets and marks conflicts before apply', async () => {
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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-input"]').setValue(`{
  "version": 1,
  "presets": [
    {
      "label": "释放分析",
      "fields": ["occupancyNo", "releaseReason"]
    },
    {
      "label": "导入治理视图",
      "fields": ["occupancyNo", "useDeptName"]
    }
  ]
}`)
    await wrapper.get('[data-testid="occupancy-preset-import-preview"]').trigger('click')

    const previewPanel = wrapper.get('[data-testid="occupancy-preset-import-preview-panel"]')
    expect(previewPanel.text()).toContain('释放分析')
    expect(previewPanel.text()).toContain('系统预设重名')
    expect(previewPanel.text()).toContain('导入治理视图')
  })

  it('imports conflicting presets with rename policy', async () => {
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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-input"]').setValue(`{
  "version": 1,
  "presets": [
    {
      "label": "释放分析",
      "fields": ["occupancyNo", "releaseReason"]
    }
  ]
}`)
    await wrapper.get('[data-testid="occupancy-preset-import-preview"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-policy-rename"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-apply"]').trigger('click')

    expect(wrapper.get('[data-testid="occupancy-export-preset-custom-0"]').text()).toContain(
      '释放分析（导入）'
    )
  })

  it('switches link trend window to 30 days and recalculates buckets', async () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-21T12:00:00+08:00'))
    const currentTime = new Date()
    const twoHoursAgo = new Date(currentTime.getTime() - 2 * 60 * 60 * 1000).toISOString()
    const twentyDaysAgo = new Date(currentTime.getTime() - 20 * 24 * 60 * 60 * 1000).toISOString()
    window.localStorage.setItem(
      'asset-real-estate-occupancy-link-stats:RE-2026-0001',
      JSON.stringify({
        counts: {
          overview: 0,
          inspection: 2,
          rectification: 0,
          disposal: 0
        },
        lastTargetKey: 'inspection',
        lastTargetLabel: '看巡检联动',
        events: [
          {
            targetKey: 'inspection',
            targetLabel: '看巡检联动',
            occurredAt: twoHoursAgo
          },
          {
            targetKey: 'inspection',
            targetLabel: '看巡检联动',
            occurredAt: twentyDaysAgo
          }
        ]
      })
    )

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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    const vm = wrapper.vm as any
    expect(vm.linkTrendItems).toHaveLength(7)

    await wrapper.get('[data-testid="occupancy-link-window-30d"]').trigger('click')

    expect(vm.linkTrendItems).toHaveLength(30)
    expect(vm.linkTrendItems.some((item: any) => item.count > 0)).toBe(true)
    expect(vm.linkTrendItems.some((item: any) => item.date === '2026-03-01')).toBe(true)
  })

  it('enters and exits trend drilldown when clicking a trend bucket', async () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-21T12:00:00+08:00'))
    const currentTime = new Date()
    const twoHoursAgo = new Date(currentTime.getTime() - 2 * 60 * 60 * 1000).toISOString()
    window.localStorage.setItem(
      'asset-real-estate-occupancy-link-stats:RE-2026-0001',
      JSON.stringify({
        counts: {
          overview: 0,
          inspection: 1,
          rectification: 0,
          disposal: 0
        },
        lastTargetKey: 'inspection',
        lastTargetLabel: '看巡检联动',
        events: [
          {
            targetKey: 'inspection',
            targetLabel: '看巡检联动',
            occurredAt: twoHoursAgo
          }
        ]
      })
    )

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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-link-trend-day-2026-03-21"]').trigger('click')
    expect(wrapper.get('[data-testid="occupancy-link-drilldown-panel"]').text()).toContain(
      '2026-03-21'
    )

    await wrapper.get('[data-testid="occupancy-link-trend-day-2026-03-21"]').trigger('click')
    expect(wrapper.find('[data-testid="occupancy-link-drilldown-panel"]').exists()).toBe(false)
  })

  it('resets link stats and clears drilldown state', async () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-21T12:00:00+08:00'))
    vi.spyOn(ElMessageBox, 'confirm').mockResolvedValue('confirm' as any)
    const currentTime = new Date()
    const twoHoursAgo = new Date(currentTime.getTime() - 2 * 60 * 60 * 1000).toISOString()
    window.localStorage.setItem(
      'asset-real-estate-occupancy-link-stats:RE-2026-0001',
      JSON.stringify({
        counts: {
          overview: 0,
          inspection: 1,
          rectification: 0,
          disposal: 0
        },
        lastTargetKey: 'inspection',
        lastTargetLabel: '看巡检联动',
        events: [
          {
            targetKey: 'inspection',
            targetLabel: '看巡检联动',
            occurredAt: twoHoursAgo
          }
        ]
      })
    )

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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-link-trend-day-2026-03-21"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-link-stats-reset"]').trigger('click')

    const vm = wrapper.vm as any
    expect(wrapper.get('[data-testid="occupancy-link-stat-last-target"]').text()).toContain('暂无')
    expect(vm.linkTrendItems.every((item: any) => item.count === 0)).toBe(true)
    expect(wrapper.find('[data-testid="occupancy-link-drilldown-panel"]').exists()).toBe(false)
  })

  it('shows import validation summary including invalid preset items', async () => {
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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-input"]').setValue(`{
  "version": 1,
  "presets": [
    {
      "label": "释放分析",
      "fields": ["occupancyNo", "releaseReason"]
    },
    {
      "label": "",
      "fields": ["occupancyNo"]
    },
    {
      "label": "全非法字段",
      "fields": ["invalidOnly"]
    }
  ]
}`)
    await wrapper.get('[data-testid="occupancy-preset-import-preview"]').trigger('click')

    const summary = wrapper.get('[data-testid="occupancy-preset-import-summary"]')
    expect(summary.text()).toContain('可导入 1')
    expect(summary.text()).toContain('冲突 1')
    expect(summary.text()).toContain('无效 2')

    const invalidList = wrapper.get('[data-testid="occupancy-preset-import-invalid-list"]')
    expect(invalidList.text()).toContain('名称为空')
    expect(invalidList.text()).toContain('无有效字段')
  })

  it('applies row-level conflict policy before global policy', async () => {
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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-copy-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-copy-name"]').setValue('点测自定义预设')
    await wrapper.get('[data-testid="occupancy-preset-copy-apply"]').trigger('click')

    await wrapper.get('[data-testid="occupancy-preset-import-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-input"]').setValue(`{
  "version": 1,
  "presets": [
    {
      "label": "点测自定义预设",
      "fields": ["occupancyNo", "releaseReason"]
    }
  ]
}`)
    await wrapper.get('[data-testid="occupancy-preset-import-preview"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-policy-skip"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-item-policy-overwrite-preview-0"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-apply"]').trigger('click')

    expect(wrapper.findAll('[data-testid^="occupancy-export-preset-custom-"]')).toHaveLength(1)
    await wrapper.get('[data-testid="occupancy-export-preset-custom-0"]').trigger('click')
    const vm = wrapper.vm as any
    expect(vm.selectedExportFields).toContain('releaseReason')
  })

  it('syncs trend drilldown into history filters and restores them on clear', async () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-21T12:00:00+08:00'))
    window.localStorage.setItem(
      'asset-real-estate-occupancy-link-stats:RE-2026-0001',
      JSON.stringify({
        counts: {
          overview: 0,
          inspection: 1,
          rectification: 0,
          disposal: 0
        },
        lastTargetKey: 'inspection',
        lastTargetLabel: '看巡检联动',
        events: [
          {
            targetKey: 'inspection',
            targetLabel: '看巡检联动',
            occurredAt: '2026-03-21T09:00:00+08:00'
          }
        ]
      })
    )

    const wrapper = mount(OccupancyPanel, {
      attachTo: document.body,
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
            startDate: '2026-03-21',
            changeReason: 'reason-alpha'
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    const vm = wrapper.vm as any
    vm.customRangeDraft.start = '2026-03-01'
    vm.customRangeDraft.end = '2026-03-02'
    await nextTick()

    await wrapper.get('[data-testid="occupancy-link-trend-day-2026-03-21"]').trigger('click')

    expect(wrapper.get('[data-testid="occupancy-history-drilldown-tip"]').text()).toContain(
      '2026-03-21'
    )
    expect(vm.timeFilter).toBe('CUSTOM')
    expect(vm.customRangeDraft.start).toBe('2026-03-21')
    expect(vm.customRangeDraft.end).toBe('2026-03-21')

    await wrapper.get('[data-testid="occupancy-link-drilldown-clear"]').trigger('click')

    expect(wrapper.find('[data-testid="occupancy-history-drilldown-tip"]').exists()).toBe(false)
    expect(vm.customRangeDraft.start).toBe('2026-03-01')
    expect(vm.customRangeDraft.end).toBe('2026-03-02')
  })

  it('requires confirmation before resetting link stats', async () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-21T12:00:00+08:00'))
    const confirmSpy = vi.spyOn(ElMessageBox, 'confirm')
    confirmSpy.mockResolvedValue('confirm' as any)
    window.localStorage.setItem(
      'asset-real-estate-occupancy-link-stats:RE-2026-0001',
      JSON.stringify({
        counts: {
          overview: 0,
          inspection: 1,
          rectification: 0,
          disposal: 0
        },
        lastTargetKey: 'inspection',
        lastTargetLabel: '看巡检联动',
        events: [
          {
            targetKey: 'inspection',
            targetLabel: '看巡检联动',
            occurredAt: '2026-03-21T09:00:00+08:00'
          }
        ]
      })
    )

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
            startDate: '2026-03-21',
            changeReason: 'reason-alpha'
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-link-stats-reset"]').trigger('click')

    expect(confirmSpy).toHaveBeenCalled()
    expect(wrapper.get('[data-testid="occupancy-link-stat-inspection"]').text()).toContain('0')
  })

  it('exports preset import validation report as json', async () => {
    const createObjectURL = vi.fn(() => 'blob:occupancy-import-report')
    const revokeObjectURL = vi.fn()
    URL.createObjectURL = createObjectURL
    URL.revokeObjectURL = revokeObjectURL

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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-input"]').setValue(`{
  "version": 1,
  "presets": [
    {
      "label": "释放分析",
      "fields": ["occupancyNo", "releaseReason"]
    },
    {
      "label": "",
      "fields": ["occupancyNo"]
    }
  ]
}`)
    await wrapper.get('[data-testid="occupancy-preset-import-preview"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-export-report"]').trigger('click')

    expect(createObjectURL).toHaveBeenCalledTimes(1)
    const exportedBlob = createObjectURL.mock.calls[0][0] as Blob
    const content = await exportedBlob.text()
    const report = JSON.parse(content)
    expect(report.summary.conflictCount).toBe(1)
    expect(report.summary.invalidCount).toBe(1)
    expect(report.invalidItems[0].reason).toBe('名称为空')
  })

  it('stores last import result and reuses row-level conflict policies', async () => {
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
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-export-config-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-copy-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-copy-name"]').setValue('批次十四冲突预设')
    await wrapper.get('[data-testid="occupancy-preset-copy-apply"]').trigger('click')

    await wrapper.get('[data-testid="occupancy-preset-import-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-input"]').setValue(`{
  "version": 1,
  "presets": [
    {
      "label": "批次十四冲突预设",
      "fields": ["occupancyNo", "releaseReason"]
    }
  ]
}`)
    await wrapper.get('[data-testid="occupancy-preset-import-preview"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-item-policy-overwrite-preview-0"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-apply"]').trigger('click')

    const lastResult = wrapper.get('[data-testid="occupancy-preset-import-last-result"]')
    expect(lastResult.text()).toContain('覆盖 1')

    await wrapper.get('[data-testid="occupancy-preset-import-toggle"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-input"]').setValue(`{
  "version": 1,
  "presets": [
    {
      "label": "批次十四冲突预设",
      "fields": ["occupancyNo", "releaseReason"]
    }
  ]
}`)
    await wrapper.get('[data-testid="occupancy-preset-import-preview"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-preset-import-reuse-last-policies"]').trigger('click')

    expect(
      wrapper
        .get('[data-testid="occupancy-preset-import-item-policy-overwrite-preview-0"]')
        .classes()
    ).toContain('export-preset-chip--active')
  })

  it('saves, restores and clears trend drilldown snapshots', async () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-21T12:00:00+08:00'))
    window.localStorage.setItem(
      'asset-real-estate-occupancy-link-stats:RE-2026-0001',
      JSON.stringify({
        counts: {
          overview: 0,
          inspection: 1,
          rectification: 0,
          disposal: 0
        },
        lastTargetKey: 'inspection',
        lastTargetLabel: '看巡检联动',
        events: [
          {
            targetKey: 'inspection',
            targetLabel: '看巡检联动',
            occurredAt: '2026-03-21T09:00:00+08:00'
          }
        ]
      })
    )

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
            startDate: '2026-03-21',
            changeReason: 'reason-alpha'
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-link-trend-day-2026-03-21"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-link-drilldown-save-snapshot"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-link-drilldown-clear"]').trigger('click')

    expect(wrapper.find('[data-testid="occupancy-history-drilldown-tip"]').exists()).toBe(false)
    expect(wrapper.get('[data-testid="occupancy-history-restore-snapshot"]').text()).toContain(
      '恢复快照'
    )

    await wrapper.get('[data-testid="occupancy-history-restore-snapshot"]').trigger('click')
    expect(wrapper.get('[data-testid="occupancy-history-drilldown-tip"]').text()).toContain(
      '2026-03-21'
    )

    await wrapper.get('[data-testid="occupancy-history-clear-snapshot"]').trigger('click')
    expect(wrapper.find('[data-testid="occupancy-history-restore-snapshot"]').exists()).toBe(false)
  })

  it('resets only the selected link stats scope', async () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-21T12:00:00+08:00'))
    const confirmSpy = vi.spyOn(ElMessageBox, 'confirm')
    confirmSpy.mockResolvedValue('confirm' as any)
    window.localStorage.setItem(
      'asset-real-estate-occupancy-link-stats:RE-2026-0001',
      JSON.stringify({
        counts: {
          overview: 0,
          inspection: 2,
          rectification: 1,
          disposal: 0
        },
        lastTargetKey: 'rectification',
        lastTargetLabel: '看整改进展',
        events: [
          {
            targetKey: 'inspection',
            targetLabel: '看巡检联动',
            occurredAt: '2026-03-21T09:00:00+08:00'
          },
          {
            targetKey: 'rectification',
            targetLabel: '看整改进展',
            occurredAt: '2026-03-21T10:00:00+08:00'
          }
        ]
      })
    )

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
            startDate: '2026-03-21',
            changeReason: 'reason-alpha'
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    await wrapper.get('[data-testid="occupancy-link-stats-reset-scope-events"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-link-stats-reset"]').trigger('click')

    expect(wrapper.get('[data-testid="occupancy-link-stat-inspection"]').text()).toContain('2')
    expect(wrapper.get('[data-testid="occupancy-link-trend-day-2026-03-21"]').text()).toContain('0')

    await wrapper.get('[data-testid="occupancy-link-stats-reset-scope-counts"]').trigger('click')
    await wrapper.get('[data-testid="occupancy-link-stats-reset"]').trigger('click')

    expect(wrapper.get('[data-testid="occupancy-link-stat-inspection"]').text()).toContain('0')
    expect(wrapper.get('[data-testid="occupancy-link-trend-day-2026-03-21"]').text()).toContain('0')
  })
})


