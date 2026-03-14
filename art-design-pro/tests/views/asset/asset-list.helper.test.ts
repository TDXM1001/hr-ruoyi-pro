import { describe, expect, it } from 'vitest'

import {
  buildAssetListQuery,
  collectAssetIds,
  toApplyAssetContext
} from '../../../src/views/asset/list/asset-list.helper'

describe('asset list helper', () => {
  it('builds query with assetStatus', () => {
    expect(
      buildAssetListQuery({
        assetNo: 'FA-001',
        assetName: '笔记本电脑',
        assetStatus: '1',
        categoryId: 10
      })
    ).toEqual({
      assetNo: 'FA-001',
      assetName: '笔记本电脑',
      assetStatus: '1',
      categoryId: 10
    })
  })

  it('collects asset ids for bulk actions', () => {
    expect(collectAssetIds([{ assetId: 1 }, { assetId: 2 }])).toEqual([1, 2])
  })

  it('maps list row to apply context', () => {
    expect(
      toApplyAssetContext({
        assetId: 1,
        assetNo: 'FA-001',
        assetName: '笔记本电脑',
        assetStatus: '1'
      })
    ).toEqual({
      assetId: 1,
      assetNo: 'FA-001',
      assetName: '笔记本电脑',
      assetStatus: '1'
    })
  })
})
