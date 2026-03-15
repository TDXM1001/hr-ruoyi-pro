import { describe, expect, it } from 'vitest'

import {
  buildDepreciationRows,
  buildFinanceSummaryRows,
  canEditFinanceBaseFields
} from '../../../src/views/asset/list/modules/asset-finance.helper'

describe('asset finance helper', () => {
  it('locks base fields after depreciation starts', () => {
    expect(canEditFinanceBaseFields({ accumulatedDepreciation: 0 })).toBe(true)
    expect(canEditFinanceBaseFields({ accumulatedDepreciation: 10 })).toBe(false)
  })

  it('builds finance summary rows', () => {
    expect(
      buildFinanceSummaryRows({
        originalValue: 1000,
        salvageValue: 30,
        monthlyDepreciationAmount: 26.94
      })
    ).toEqual([
      { label: '原值', value: 1000 },
      { label: '净残值', value: 30 },
      { label: '月折旧额', value: 26.94 }
    ])
  })

  it('maps depreciation logs', () => {
    expect(
      buildDepreciationRows([
        {
          period: '2026-03',
          depreciationAmount: 26.94,
          accumulatedDepreciation: 26.94,
          netBookValue: 973.06,
          bookValue: 973.06,
          calcTime: '2026-03-31 23:59:59',
          calcType: 'AUTO'
        }
      ])
    ).toEqual([
      {
        periodLabel: '2026-03',
        depreciationAmount: 26.94,
        accumulatedDepreciation: 26.94,
        netBookValue: 973.06,
        bookValue: 973.06,
        calcTime: '2026-03-31 23:59:59',
        calcType: 'AUTO'
      }
    ])
  })
})
