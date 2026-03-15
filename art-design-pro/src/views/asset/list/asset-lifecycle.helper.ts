import type { AssetLifecycleAction, AssetLifecycleContext } from '@/types/asset'

/** 生命周期入口只负责控制展示边界，不在这里混入具体审批流程。 */
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
        key: 'realEstateChange',
        label: '权属变更',
        tone: 'warning',
        mode: 'placeholder',
        message: '不动产权属变更入口待规划。'
      },
      {
        key: 'disposal',
        label: '注销/处置',
        tone: 'warning',
        mode: 'placeholder',
        message: '不动产注销/处置入口待规划。'
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
      mode: 'placeholder',
      message: '固定资产维修入口待接后端接口。'
    },
    {
      key: 'disposal',
      label: '报废/处置',
      tone: 'warning',
      mode: 'placeholder',
      message: '固定资产报废/处置入口待规划。'
    },
    ...commonActions
  ]
}
