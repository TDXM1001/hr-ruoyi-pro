import { describe, expect, it } from 'vitest'

import {
  buildDynamicAttrPayload,
  findReservedAttrCodes,
  toDynamicAttrFormRecord
} from '../../../src/views/asset/list/modules/asset-dynamic-attr.helper'

describe('asset dynamic attr helper', () => {
  it('maps backend values into form record', () => {
    expect(
      toDynamicAttrFormRecord([
        { attrCode: 'manufacturer', attrValueText: '联想' },
        { attrCode: 'weight', attrValueNumber: 2.5 }
      ])
    ).toEqual({
      manufacturer: '联想',
      weight: 2.5
    })
  })

  it('blocks reserved attr codes', () => {
    expect(findReservedAttrCodes([{ attrCode: 'asset_no' }, { attrCode: 'brand' }])).toEqual([
      'asset_no'
    ])
  })

  it('builds typed dynamic attr payload', () => {
    expect(
      buildDynamicAttrPayload(
        [
          { attrId: 1, categoryId: 10, attrCode: 'manufacturer', dataType: 'text' },
          { attrId: 2, categoryId: 10, attrCode: 'weight', dataType: 'number' }
        ],
        { manufacturer: '联想', weight: 2.5 }
      )
    ).toEqual([
      { attrId: 1, categoryId: 10, attrCode: 'manufacturer', attrValueText: '联想' },
      { attrId: 2, categoryId: 10, attrCode: 'weight', attrValueNumber: 2.5 }
    ])
  })
})
