import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ElementPlus from 'element-plus'
import RectificationPanel from '@/views/asset/real-estate/detail/components/rectification-panel.vue'

describe('RectificationPanel 审批挂载位', () => {
  it('已完成且待提交审批时展示提交审批入口与闭环阶段文案', () => {
    const wrapper = mount(RectificationPanel, {
      props: {
        rectificationRecords: [
          {
            rectificationId: 9001,
            rectificationNo: 'RC-2026-0001',
            rectificationStatus: 'COMPLETED',
            approvalStatus: 'UNSUBMITTED',
            taskNo: 'INV-2026-0008',
            taskName: '第一季度不动产巡检',
            responsibleDeptName: '研发部门',
            responsibleUserName: '若依',
            completedTime: '2026-03-21 14:49:04',
            completionDesc: '已完成现场整改并复核',
            acceptanceRemark: '资产管理员复核通过'
          }
        ],
        rectificationLogs: [],
        getBizTypeLabel: (bizType?: string) => bizType || '业务动作',
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    expect(wrapper.text()).toContain('待提交审批')
    expect(wrapper.text()).toContain('提交审批')
    expect(wrapper.text()).toContain('查看审批轨迹')
    expect(wrapper.text()).toContain('整改事实已经收口，下一步提交审批并补充审批说明。')
  })

  it('已提交审批时展示通过/驳回动作，并展示最新审批意见摘要', async () => {
    const wrapper = mount(RectificationPanel, {
      props: {
        rectificationRecords: [
          {
            rectificationId: 9002,
            rectificationNo: 'RC-2026-0002',
            rectificationStatus: 'COMPLETED',
            approvalStatus: 'SUBMITTED',
            taskNo: 'INV-2026-0009',
            taskName: '消防设施专项巡检',
            responsibleDeptName: '研发部门',
            responsibleUserName: '若依',
            approvalSubmittedTime: '2026-03-21 15:00:00',
            latestApprovalOpinion: '请补充现场复核照片后继续审批。',
            latestApprovalOperateBy: '资产主管',
            latestApprovalOperateTime: '2026-03-21 15:10:00'
          }
        ],
        rectificationLogs: [],
        getBizTypeLabel: (bizType?: string) => bizType || '业务动作',
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    expect(wrapper.text()).toContain('审批中')
    expect(wrapper.text()).toContain('审批通过')
    expect(wrapper.text()).toContain('审批驳回')
    expect(wrapper.text()).toContain('请补充现场复核照片后继续审批。')
    expect(wrapper.text()).toContain('资产主管 / 2026-03-21 15:10:00')

    await wrapper.get('[data-testid="rectification-approve-link-9002"]').trigger('click')
    expect(wrapper.emitted('approve-approval')?.[0]).toEqual([9002])

    await wrapper.get('[data-testid="rectification-reject-link-9002"]').trigger('click')
    expect(wrapper.emitted('reject-approval')?.[0]).toEqual([9002])
  })

  it('概览区按审批阶段统计已完成整改单数量', () => {
    const wrapper = mount(RectificationPanel, {
      props: {
        rectificationRecords: [
          { rectificationId: 1, rectificationStatus: 'COMPLETED', approvalStatus: 'UNSUBMITTED' },
          { rectificationId: 2, rectificationStatus: 'COMPLETED', approvalStatus: 'SUBMITTED' },
          { rectificationId: 3, rectificationStatus: 'COMPLETED', approvalStatus: 'REJECTED' },
          { rectificationId: 4, rectificationStatus: 'COMPLETED', approvalStatus: 'APPROVED' }
        ],
        rectificationLogs: [],
        getBizTypeLabel: (bizType?: string) => bizType || '业务动作',
        canEdit: true
      },
      global: {
        plugins: [ElementPlus]
      }
    })

    expect(wrapper.text()).toContain('待提交审批')
    expect(wrapper.text()).toContain('审批中')
    expect(wrapper.text()).toContain('驳回待处理')
    expect(wrapper.text()).toContain('审批通过')
  })

  it('整改页签展示统一闭环提示与阶段化整改轨迹', () => {
    const wrapper = mount(RectificationPanel, {
      props: {
        rectificationRecords: [
          {
            rectificationId: 9301,
            rectificationNo: 'RC-2026-0030',
            rectificationStatus: 'PENDING',
            taskNo: 'INV-2026-0030',
            taskName: '门禁巡检',
            responsibleDeptName: '研发部门',
            responsibleUserName: '若依',
            deadlineDate: '2026-03-28'
          },
          {
            rectificationId: 9302,
            rectificationNo: 'RC-2026-0031',
            rectificationStatus: 'COMPLETED',
            approvalStatus: 'UNSUBMITTED',
            taskNo: 'INV-2026-0031',
            taskName: '消防巡检',
            responsibleDeptName: '研发部门',
            responsibleUserName: '若依',
            completedTime: '2026-03-21 09:30:00'
          }
        ],
        rectificationLogs: [
          {
            logId: 401,
            bizType: 'LEDGER_UPDATE',
            changeDesc: '完成整改单：RC-2026-0031，完成说明：已完成闭门器维修',
            operateBy: 'asset-admin',
            operateTime: '2026-03-21 09:30:00'
          },
          {
            logId: 402,
            bizType: 'LEDGER_UPDATE',
            changeDesc: '提交整改审批：RC-2026-0031，意见：整改已完成，请审批',
            operateBy: 'asset-admin',
            operateTime: '2026-03-21 10:00:00'
          }
        ] as any,
        rectificationSummary: {
          pendingRectificationCount: 1,
          pendingSubmitCount: 1,
          inReviewCount: 0,
          rejectedResubmitCount: 0,
          approvedClosedCount: 0,
          overallStage: 'PENDING_SUBMIT',
          overallLabel: '待提交审批',
          overallTagType: 'info',
          latestActionLabel: '完成整改',
          latestActionTime: '2026-03-21 09:30:00',
          latestActionDesc: '整改事实已收口，下一步建议尽快提交审批。',
          nextStep: '整改事实已经收口，但仍需尽快提交审批，避免闭环停在待提交阶段。'
        },
        getBizTypeLabel: (bizType?: string) => bizType || '业务动作',
        canEdit: true
      } as any,
      global: {
        plugins: [ElementPlus]
      }
    })

    expect(wrapper.text()).toContain('当前闭环状态')
    expect(wrapper.text()).toContain('待提交审批')
    expect(wrapper.text()).toContain('最近整改动作')
    expect(wrapper.text()).toContain('整改事实已经收口，但仍需尽快提交审批，避免闭环停在待提交阶段。')
    expect(wrapper.text()).toContain('完成整改')
    expect(wrapper.text()).toContain('提交审批')
  })
})
