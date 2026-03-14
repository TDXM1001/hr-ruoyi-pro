import type {
  AssetAggregateDetail,
  AssetAggregateReq,
  AssetAttachment,
  AssetBasicInfo,
  AssetDynamicAttrValue,
  AssetFinanceInfo,
  AssetRealEstateInfo
} from '@/types/asset'

/** 编辑抽屉的聚合状态。 */
export interface AssetDrawerState {
  basicForm: AssetBasicInfo
  financeForm: AssetFinanceInfo
  realEstateForm: AssetRealEstateInfo
  dynamicAttrForm: Record<string, unknown>
  dynamicAttrs: AssetDynamicAttrValue[]
  attachments: AssetAttachment[]
}

/** 创建空的基础信息表单。 */
function createEmptyBasicForm(): AssetBasicInfo {
  return {
    assetNo: '',
    assetName: '',
    assetType: '1',
    assetStatus: '1',
    remark: ''
  }
}

/** 创建空的财务信息表单。 */
function createEmptyFinanceForm(): AssetFinanceInfo {
  return {
    bookType: '1',
    currencyCode: 'CNY',
    originalValue: 0,
    salvageRate: 0,
    depreciationMethod: '1',
    usefulLifeMonth: 0,
    depreciationStartDate: ''
  }
}

/** 创建空的不动产信息表单。 */
function createEmptyRealEstateForm(): AssetRealEstateInfo {
  return {
    propertyCertNo: '',
    realEstateUnitNo: '',
    addressFull: '',
    landUse: '',
    buildingUse: '',
    buildingArea: 0,
    completionDate: '',
    rightsHolder: ''
  }
}

/** 把动态属性值映射为表单可编辑记录。 */
function createDynamicAttrForm(items: AssetDynamicAttrValue[] = []) {
  return items.reduce<Record<string, unknown>>((acc, item) => {
    acc[item.attrCode] =
      item.attrValueText ?? item.attrValueNumber ?? item.attrValueDate ?? item.attrValueJson ?? ''
    return acc
  }, {})
}

/** 创建抽屉初始状态。 */
export function createEmptyDrawerState(): AssetDrawerState {
  return {
    basicForm: createEmptyBasicForm(),
    financeForm: createEmptyFinanceForm(),
    realEstateForm: createEmptyRealEstateForm(),
    dynamicAttrForm: {},
    dynamicAttrs: [],
    attachments: []
  }
}

/** 用聚合详情回填抽屉状态。 */
export function hydrateDrawerState(
  detail?: Partial<AssetAggregateDetail> | null
): AssetDrawerState {
  const state = createEmptyDrawerState()
  if (!detail) {
    return state
  }

  const basicInfo = detail.basicInfo || {}
  const financeInfo = detail.financeInfo || {}
  const realEstateInfo = detail.realEstateInfo || {}
  const dynamicAttrs = detail.dynamicAttrs || []
  const attachments = detail.attachments || []

  return {
    basicForm: {
      ...state.basicForm,
      ...basicInfo
    },
    financeForm: {
      ...state.financeForm,
      ...financeInfo
    },
    realEstateForm: {
      ...state.realEstateForm,
      ...realEstateInfo
    },
    dynamicAttrForm: createDynamicAttrForm(dynamicAttrs),
    dynamicAttrs: dynamicAttrs.map((item) => ({ ...item })),
    attachments: attachments.map((item) => ({ ...item }))
  }
}

/** 把抽屉状态重新组装为聚合请求体。 */
export function buildAggregatePayload(
  state: AssetDrawerState,
  mode: 'add' | 'edit'
): AssetAggregateReq {
  const basicInfo = {
    ...state.basicForm
  }

  if (mode === 'add') {
    delete basicInfo.assetId
  }

  const dynamicAttrs = state.dynamicAttrs.map((item) => {
    const nextItem: AssetDynamicAttrValue = {
      ...item,
      attrValueText: undefined,
      attrValueNumber: undefined,
      attrValueDate: undefined,
      attrValueJson: undefined
    }
    const nextValue = state.dynamicAttrForm[item.attrCode]

    if (typeof nextValue === 'number') {
      nextItem.attrValueNumber = nextValue
    } else if (typeof nextValue === 'string') {
      nextItem.attrValueText = nextValue
    } else if (nextValue != null) {
      nextItem.attrValueJson = JSON.stringify(nextValue)
    }

    return nextItem
  })

  return {
    basicInfo,
    financeInfo: {
      ...state.financeForm
    },
    realEstateInfo:
      state.basicForm.assetType === '2'
        ? {
            ...state.realEstateForm,
            assetId: mode === 'edit' ? state.basicForm.assetId : undefined
          }
        : null,
    dynamicAttrs,
    attachments: state.attachments.map((item) => ({ ...item }))
  }
}
