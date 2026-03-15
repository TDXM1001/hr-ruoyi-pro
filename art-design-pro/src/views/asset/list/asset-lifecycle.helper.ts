import type { AssetLifecycleAction, AssetLifecycleContext } from '@/types/asset'

/** 生命周期入口负责声明“哪些动作已经能走通”，不在这里写页面跳转细节。 */
export function buildLifecycleActions(asset: AssetLifecycleContext): AssetLifecycleAction[] {
  const commonActions: AssetLifecycleAction[] = [
    {
      key: 'change',
      label: '变更',
      tone: 'primary',
      mode: 'placeholder',
      message: '资产变更流程待规划，当前先保留生命周期入口边界。'
    },
    {
      key: 'delete',
      label: '删除',
      tone: 'danger',
      mode: 'action'
    }
  ]

  if (asset.assetType === '2') {
    return [
      {
        key: 'realEstateOwnership',
        label: '权属变更',
        tone: 'warning',
        mode: 'action'
      },
      {
        key: 'realEstateUsage',
        label: '用途变更',
        tone: 'primary',
        mode: 'action'
      },
      {
        key: 'realEstateStatus',
        label: '状态变更',
        tone: 'info',
        mode: 'action'
      },
      {
        key: 'realEstateDisposal',
        label: '注销/处置',
        tone: 'danger',
        mode: 'action'
      },
      ...commonActions
    ]
  }

  return [
    {
      key: 'requisition',
      label: '领用',
      tone: 'primary',
      mode: 'action'
    },
    {
      key: 'repair',
      label: '维修',
      tone: 'warning',
      mode: 'action'
    },
    {
      key: 'disposal',
      label: '报废/处置',
      tone: 'warning',
      mode: 'action'
    },
    ...commonActions
  ]
}
