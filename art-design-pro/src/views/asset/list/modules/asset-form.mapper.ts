import type {
  AssetAggregateDetail,
  AssetAggregateReq,
  AssetAttachment,
  AssetBasicInfo,
  AssetDynamicAttrDefinition,
  AssetDynamicAttrValue,
  AssetFinanceInfo,
  AssetRealEstateInfo
} from '@/types/asset'
import {
  buildDynamicAttrPayload,
  toDynamicAttrFormRecord,
  type AssetDynamicAttrFormValue
} from './asset-dynamic-attr.helper'

/** 编辑抽屉的聚合状态。 */
export interface AssetDrawerState {
  basicForm: AssetBasicInfo
  financeForm: AssetFinanceInfo
  realEstateForm: AssetRealEstateInfo
  dynamicAttrForm: Record<string, AssetDynamicAttrFormValue>
  dynamicAttrDefinitions: AssetDynamicAttrDefinition[]
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

/** 创建抽屉初始状态。 */
export function createEmptyDrawerState(): AssetDrawerState {
  return {
    basicForm: createEmptyBasicForm(),
    financeForm: createEmptyFinanceForm(),
    realEstateForm: createEmptyRealEstateForm(),
    dynamicAttrForm: {},
    dynamicAttrDefinitions: [],
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
    dynamicAttrForm: toDynamicAttrFormRecord(dynamicAttrs),
    dynamicAttrDefinitions: [],
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
    dynamicAttrs: buildDynamicAttrPayload(state.dynamicAttrDefinitions, state.dynamicAttrForm),
    attachments: state.attachments.map((item) => ({ ...item }))
  }
}
