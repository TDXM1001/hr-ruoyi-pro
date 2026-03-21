import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import OccupancyPanel from '@/views/asset/real-estate/detail/components/occupancy-panel.vue'

describe('OccupancyPanel 占用闭环', () => {
  it('无有效占用时展示发起占用入口', async () => {
    const wrapper = mount(OccupancyPanel, {
      props: {
        detailData: {
          assetCode: 'RE-2026-0001',
          ownerDeptName: '研发部门'
        },
        occupancyRecords: [],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    expect(wrapper.text()).toContain('当前有效占用')
    expect(wrapper.text()).toContain('暂无有效占用')
    expect(wrapper.text()).toContain('发起占用')

    await wrapper.get('[data-testid="occupancy-create-link"]').trigger('click')
    expect(wrapper.emitted('create-occupancy')?.length).toBe(1)
  })

  it('存在有效占用时展示变更释放和历史记录', async () => {
    const wrapper = mount(OccupancyPanel, {
      props: {
        detailData: {
          assetCode: 'RE-2026-0001',
          ownerDeptName: '研发部门'
        },
        occupancyRecords: [
          {
            occupancyId: 9101,
            occupancyNo: 'OCC-2026-9001',
            occupancyStatus: 'ACTIVE',
            useDeptName: '研发部门',
            responsibleUserName: '若依',
            locationName: '深圳南山科技园A座',
            startDate: '2026-03-22',
            changeReason: '前端闭环点测样例'
          },
          {
            occupancyId: 9100,
            occupancyNo: 'OCC-2026-8999',
            occupancyStatus: 'RELEASED',
            useDeptName: '行政部门',
            responsibleUserName: '王敏',
            locationName: '深圳南山科技园B座',
            startDate: '2026-03-01',
            endDate: '2026-03-10',
            changeReason: '历史占用',
            releaseReason: '部门搬离'
          }
        ],
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    expect(wrapper.text()).toContain('OCC-2026-9001')
    expect(wrapper.text()).toContain('研发部门')
    expect(wrapper.text()).toContain('变更占用')
    expect(wrapper.text()).toContain('释放占用')
    expect(wrapper.text()).toContain('占用历史记录')
    expect(wrapper.text()).toContain('OCC-2026-8999')
    expect(wrapper.text()).toContain('已释放')

    await wrapper.get('[data-testid="occupancy-change-link-9101"]').trigger('click')
    expect(wrapper.emitted('change-occupancy')?.[0]).toEqual([
      expect.objectContaining({ occupancyId: 9101 })
    ])

    await wrapper.get('[data-testid="occupancy-release-link-9101"]').trigger('click')
    expect(wrapper.emitted('release-occupancy')?.[0]).toEqual([
      expect.objectContaining({ occupancyId: 9101 })
    ])
  })
})