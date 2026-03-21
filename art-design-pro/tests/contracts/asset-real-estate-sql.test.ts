import { readdirSync, readFileSync } from "node:fs"
import { dirname, join } from "node:path"
import { fileURLToPath } from "node:url"
import { describe, expect, it } from "vitest"

const currentDir = dirname(fileURLToPath(import.meta.url))
const assetSqlDir = join(currentDir, "..", "..", "..", "RuoYi-Vue", "sql", "asset")

const aggregateAssetSql = () => {
  return readdirSync(assetSqlDir)
    .filter((fileName) => fileName.endsWith(".sql"))
    .sort()
    .map((fileName) => readFileSync(join(assetSqlDir, fileName), "utf8"))
    .join("\n")
}

describe("Asset Real Estate SQL Contract", () => {
  it("should provide real estate routes for list, detail, create, edit and detail tabs", () => {
    const sql = aggregateAssetSql()

    expect(sql).toContain("'AssetRealEstate'")
    expect(sql).toContain("'real-estate'")
    expect(sql).toContain("'asset/real-estate/index'")
    expect(sql).toContain("'AssetRealEstateDetail'")
    expect(sql).toContain("'real-estate/detail/:assetId'")
    expect(sql).toContain("'asset/real-estate/detail/index'")
    expect(sql).toContain("'AssetRealEstateCreate'")
    expect(sql).toContain("'real-estate/create'")
    expect(sql).toContain("'asset/real-estate/form/index'")
    expect(sql).toContain("'AssetRealEstateEdit'")
    expect(sql).toContain("'real-estate/edit/:assetId'")
    expect(sql).toContain("'AssetRealEstateDetailOccupancy'")
    expect(sql).toContain("'real-estate/detail/:assetId/occupancy'")
    expect(sql).toContain("'AssetRealEstateDetailInspection'")
    expect(sql).toContain("'real-estate/detail/:assetId/inspection'")
    expect(sql).toContain("'AssetRealEstateDetailDisposal'")
    expect(sql).toContain("'real-estate/detail/:assetId/disposal'")
    expect(sql).toContain("'asset:realEstate:list'")
    expect(sql).toContain("'asset:realEstate:query'")
    expect(sql).toContain("'asset:realEstate:add'")
    expect(sql).toContain("'asset:realEstate:edit'")
  })

  it("should provide real estate sample ledger and profile data for smoke testing", () => {
    const sql = aggregateAssetSql()

    expect(sql).toContain("REAL_ESTATE")
    expect(sql).toContain("insert into ast_asset_real_estate_profile")
    expect(sql).toContain("RE-2026-")
  })
})
