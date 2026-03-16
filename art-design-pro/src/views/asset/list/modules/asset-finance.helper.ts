import type { AssetDepreciationLog, AssetFinanceInfo } from '@/types/asset'

/** 财务摘要行，统一弹窗顶部摘要区的展示结构。 */
export interface AssetFinanceSummaryRow {
  label: string
  value: number | string | undefined
}

/** 折旧日志表格行，避免页面层重复处理字段映射。 */
export interface AssetDepreciationRow {
  periodLabel: string
  depreciationAmount?: number
  accumulatedDepreciation?: number
  netBookValue?: number
  bookValue?: number
  calcTime?: string
  calcType?: string
}

/** 只要已经产生累计折旧，就锁定财务基础字段。 */
export interface AssetFinanceReadonlyContext {
  accumulatedDepreciation?: number
  lastDepreciationPeriod?: string
}

/** 财务基础字段一旦发生折旧，就必须转为只读口径。 */
export function isFinanceFieldReadonly(finance: AssetFinanceReadonlyContext): boolean {
  if ((finance.accumulatedDepreciation || 0) > 0) {
    return true
  }
  return Boolean(String(finance.lastDepreciationPeriod || '').trim())
}

/** 兼容现有调用方，沿用“可编辑”语义暴露结果。 */
export function canEditFinanceBaseFields(finance: AssetFinanceReadonlyContext): boolean {
  return !isFinanceFieldReadonly(finance)
}

/** 构建财务摘要区，保证页面展示顺序稳定。 */
export function buildFinanceSummaryRows(
  finance: Partial<AssetFinanceInfo>
): AssetFinanceSummaryRow[] {
  return [
    { label: '原值', value: finance.originalValue },
    { label: '净残值', value: finance.salvageValue },
    { label: '月折旧额', value: finance.monthlyDepreciationAmount }
  ]
}

/** 把后端折旧日志映射成表格可直接消费的行结构。 */
export function buildDepreciationRows(
  logs: Array<Partial<AssetDepreciationLog>>
): AssetDepreciationRow[] {
  return logs.map((item) => ({
    periodLabel: item.period || '',
    depreciationAmount: item.depreciationAmount,
    accumulatedDepreciation: item.accumulatedDepreciation,
    netBookValue: item.netBookValue,
    bookValue: item.bookValue,
    calcTime: item.calcTime,
    calcType: item.calcType
  }))
}
