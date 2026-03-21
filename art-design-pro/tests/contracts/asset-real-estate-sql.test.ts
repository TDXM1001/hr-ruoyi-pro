import { readdirSync, readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const currentDir = dirname(fileURLToPath(import.meta.url))
const assetSqlDir = join(currentDir, '..', '..', '..', 'RuoYi-Vue', 'sql', 'asset')

const aggregateAssetSql = () => {
  return readdirSync(assetSqlDir)
    .filter((fileName) => fileName.endsWith('.sql'))
    .sort()
    .map((fileName) => readFileSync(join(assetSqlDir, fileName), 'utf8'))
    .join('\n')
}

describe('Asset Real Estate SQL Contract', () => {
  it('should provide a real estate menu seed for backend routes', () => {
    const sql = aggregateAssetSql()

    expect(sql).toContain("'AssetRealEstate'")
    expect(sql).toContain("'real-estate'")
    expect(sql).toContain("'asset/real-estate/index'")
    expect(sql).toContain("'asset:realEstate:list'")
    expect(sql).toContain("'asset:realEstate:query'")
  })

  it('should provide real estate sample ledger and profile data for smoke testing', () => {
    const sql = aggregateAssetSql()

    expect(sql).toContain('REAL_ESTATE')
    expect(sql).toContain('insert into ast_asset_real_estate_profile')
    expect(sql).toContain('RE-2026-')
  })
})
