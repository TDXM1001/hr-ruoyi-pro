/**
 * 资产模块共享类型定义。
 * 这里收敛前端和后端聚合接口共用的核心数据结构，避免页面重复声明。
 */

/** 资产类型：1=固定资产，2=不动产。 */
export const ASSET_TYPE_OPTIONS = [
  { label: '固定资产', value: '1' },
  { label: '不动产', value: '2' }
] as const

/** 资产状态：1=在用 2=领用中 3=维修中 4=盘点中 5=已报废 6=已处置 7=闲置。 */
export const ASSET_STATUS_OPTIONS = [
  { label: '在用', value: '1' },
  { label: '领用中', value: '2' },
  { label: '维修中', value: '3' },
  { label: '盘点中', value: '4' },
  { label: '已报废', value: '5' },
  { label: '已处置', value: '6' },
  { label: '闲置', value: '7' }
] as const

/** 已报废、已处置属于固定资产终态，不再开放固定资产生命周期动作。 */
export const FIXED_ASSET_TERMINAL_STATUSES = ['5', '6'] as const

/** 固定资产处置类型。 */
export const FIXED_ASSET_DISPOSAL_TYPE_OPTIONS = [
  { label: '报废', value: 'scrap' },
  { label: '出售', value: 'sell' },
  { label: '划转', value: 'transfer' },
  { label: '捐赠', value: 'donate' }
] as const

/** 固定资产处置类型联合类型。 */
export type FixedAssetDisposalType = (typeof FIXED_ASSET_DISPOSAL_TYPE_OPTIONS)[number]['value']

/** 把固定资产处置类型翻译成页面文案。 */
export function formatFixedAssetDisposalType(value?: string) {
  return FIXED_ASSET_DISPOSAL_TYPE_OPTIONS.find((item) => item.value === value)?.label || '--'
}

/** 资产基础信息。 */
export interface AssetBasicInfo {
  assetId?: number
  assetNo: string
  assetName: string
  categoryId?: number
  /** 资产类型：1=固定资产，2=不动产。 */
  assetType: '1' | '2'
  specModel?: string
  unit?: string
  ownershipOrgId?: number
  manageDeptId?: number
  useDeptId?: number
  responsibleUserId?: number
  userId?: number
  locationId?: number
  locationText?: string
  acquireMethod?: string
  purchaseDate?: string
  capitalizationDate?: string
  enableDate?: string
  /** 资产状态：1=在用 2=领用中 3=维修中 4=盘点中 5=已报废 6=已处置 7=闲置。 */
  assetStatus: string
  remark?: string
  createTime?: string
  updateTime?: string
}

/** 资产财务信息。 */
export interface AssetFinanceInfo {
  financeId?: number
  assetId?: number
  bookType: string
  currencyCode: string
  originalValue: number
  salvageRate: number
  salvageValue?: number
  depreciableValue?: number
  depreciationMethod: string
  usefulLifeMonth: number
  depreciationStartDate: string
  depreciationEndDate?: string
  monthlyDepreciationAmount?: number
  accumulatedDepreciation?: number
  netBookValue?: number
  impairmentAmount?: number
  bookValue?: number
  disposedValue?: number
  financeStatus?: string
  lastDepreciationPeriod?: string
  versionNo?: number
  remark?: string
}

/** 不动产扩展信息。 */
export interface AssetRealEstateInfo {
  realEstateId?: number
  assetId?: number
  propertyCertNo?: string
  legacyCertNo?: string
  realEstateUnitNo?: string
  addressFull?: string
  landNature?: string
  landUse?: string
  buildingUse?: string
  landArea?: number
  sharedLandArea?: number
  buildingArea?: number
  innerArea?: number
  buildingStructure?: string
  buildingNo?: string
  floorNo?: string
  roomNo?: string
  completionDate?: string
  builtYear?: number
  landTermStartDate?: string
  landTermEndDate?: string
  rightsType?: string
  rightsHolder?: string
  coOwnershipType?: string
  mortgageStatus?: string
  mortgagee?: string
  seizureStatus?: string
  registrationDate?: string
  remark?: string
}

/** 分类动态属性定义。 */
export interface AssetDynamicAttrDefinition {
  attrId: number
  categoryId: number
  attrCode: string
  attrName?: string
  attrType?: string
  dataType?: string
  isRequired?: string
  isUnique?: string
  isListDisplay?: string
  isQueryCondition?: string
  defaultValue?: string
  optionSourceType?: string
  optionSource?: string
  validationRule?: string
  sortOrder?: number
  status?: string
  remark?: string
}

/** 分类动态属性定义请求体。 */
export interface AssetDynamicAttrDefinitionReq {
  attrId?: number
  categoryId: number
  attrCode: string
  attrName: string
  attrType?: string
  dataType?: string
  isRequired?: string
  isUnique?: string
  isListDisplay?: string
  isQueryCondition?: string
  defaultValue?: string
  optionSourceType?: string
  optionSource?: string
  validationRule?: string
  status?: string
  remark?: string
}

/** 动态属性值。 */
export interface AssetDynamicAttrValue {
  valueId?: number
  assetId?: number
  categoryId?: number
  attrId: number
  attrCode: string
  attrValueText?: string
  attrValueNumber?: number
  attrValueDate?: string
  attrValueJson?: string
  remark?: string
}

/** 资产附件。 */
export interface AssetAttachment {
  attachmentId?: number
  assetId?: number
  bizType: string
  fileName: string
  fileUrl: string
  fileSize?: number
  fileSuffix?: string
  sortOrder?: number
  remark?: string
  createTime?: string
}

/** 折旧日志。 */
export interface AssetDepreciationLog {
  logId?: number
  assetId?: number
  period: string
  depreciationAmount?: number
  accumulatedDepreciation?: number
  netBookValue?: number
  bookValue?: number
  calcTime?: string
  calcType?: string
  remark?: string
  createTime?: string
}

/** 资产台账列表项。 */
export interface AssetListItem {
  assetId: number
  assetNo: string
  assetName: string
  categoryId?: number
  assetType: '1' | '2' | string
  specModel?: string
  useDeptId?: number
  locationText?: string
  /** 资产状态：1=在用 2=领用中 3=维修中 4=盘点中 5=已报废 6=已处置 7=闲置。 */
  assetStatus: string
  purchaseDate?: string
  capitalizationDate?: string
  createTime?: string
}

/** 生命周期入口上下文。 */
export interface AssetLifecycleContext {
  assetType: '1' | '2' | string
  assetStatus?: string
}

/** 生命周期入口定义。 */
export interface AssetLifecycleAction {
  key:
    | 'change'
    | 'delete'
    | 'requisition'
    | 'repair'
    | 'scrap'
    | 'dispose'
    | 'realEstateOwnership'
    | 'realEstateUsage'
    | 'realEstateStatus'
    | 'realEstateDisposal'
  label: string
  tone?: 'primary' | 'warning' | 'danger' | 'info'
  /** action=有实际处理函数，placeholder=只给出规划提示。 */
  mode: 'action' | 'placeholder'
  message?: string
}

/** 资产聚合详情。 */
export interface AssetAggregateDetail {
  basicInfo: AssetBasicInfo
  financeInfo: AssetFinanceInfo
  realEstateInfo: AssetRealEstateInfo | null
  dynamicAttrs: AssetDynamicAttrValue[]
  attachments: AssetAttachment[]
  depreciationLogs: AssetDepreciationLog[]
}

/** 资产聚合新增/修改请求体。 */
export interface AssetAggregateReq {
  basicInfo: AssetBasicInfo
  financeInfo: AssetFinanceInfo
  realEstateInfo: AssetRealEstateInfo | null
  dynamicAttrs: AssetDynamicAttrValue[]
  attachments: AssetAttachment[]
}
