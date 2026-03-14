/**
 * 资产模块共享类型定义。
 * 这里收敛前端和后端聚合接口共用的核心数据结构，避免页面重复声明。
 */

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
  assetStatus: string
  purchaseDate?: string
  capitalizationDate?: string
  createTime?: string
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
