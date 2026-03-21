import { existsSync, readdirSync, readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const currentDir = dirname(fileURLToPath(import.meta.url))
const assetSqlDir = join(currentDir, '..', '..', '..', 'RuoYi-Vue', 'sql', 'asset')
const cleanupSqlFile = join(assetSqlDir, '15-asset-real-estate-detail-tab-route-cleanup-20260321.sql')

const aggregateAssetSql = () => {
  return readdirSync(assetSqlDir)
    .filter((fileName) => fileName.endsWith('.sql'))
    .sort()
    .map((fileName) => readFileSync(join(assetSqlDir, fileName), 'utf8'))
    .join('\n')
}

describe('Asset Real Estate SQL Contract', () => {
  it('提供不动产主路由、独立业务页路由以及权限种子', () => {
    const sql = aggregateAssetSql()

    expect(sql).toContain("'AssetRealEstate'")
    expect(sql).toContain("'real-estate'")
    expect(sql).toContain("'asset/real-estate/index'")
    expect(sql).toContain("'AssetRealEstateDetail'")
    expect(sql).toContain("'real-estate/detail/:assetId'")
    expect(sql).toContain("'AssetRealEstateCreate'")
    expect(sql).toContain("'real-estate/create'")
    expect(sql).toContain("'AssetRealEstateEdit'")
    expect(sql).toContain("'real-estate/edit/:assetId'")
    expect(sql).toContain("'AssetRealEstateInspectionTaskDetail'")
    expect(sql).toContain("'real-estate/detail/:assetId/inspection-task/:taskId'")
    expect(sql).toContain("'AssetRealEstateRectificationCreate'")
    expect(sql).toContain("'real-estate/detail/:assetId/rectification/create'")
    expect(sql).toContain("'AssetRealEstateRectificationEdit'")
    expect(sql).toContain("'real-estate/detail/:assetId/rectification/edit/:rectificationId'")
    expect(sql).toContain("'asset:realEstate:list'")
    expect(sql).toContain("'asset:realEstate:query'")
    expect(sql).toContain("'asset:realEstate:add'")
    expect(sql).toContain("'asset:realEstate:edit'")
  })

  it('提供详情子路由清理脚本，删除旧的页签菜单数据', () => {
    expect(existsSync(cleanupSqlFile)).toBe(true)
    const cleanupSql = readFileSync(cleanupSqlFile, 'utf8')

    expect(cleanupSql).toContain('delete from sys_role_menu where menu_id in (2135, 2136, 2137, 2138)')
    expect(cleanupSql).toContain('delete from sys_menu where menu_id in (2135, 2136, 2137, 2138)')
  })

  it('提供不动产点测样例和整改建模脚本', () => {
    const sql = aggregateAssetSql()

    expect(sql).toContain('REAL_ESTATE')
    expect(sql).toContain('insert into ast_asset_real_estate_profile')
    expect(sql).toContain('RE-2026-')
    expect(sql).toContain('ast_asset_rectification_order')
    expect(sql).toContain('rectification_no')
    expect(sql).toContain('inventory_item_id')
    expect(sql).toContain('rectification_status')
  })
})
