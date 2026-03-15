# 资产前端聚合对接 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 基于 `docs/plans/2026-03-14-asset-frontend-integration-plan.md` 的“方案 B：分阶段聚合改造”，完成 `art-design-pro` 资产台账、资产编辑、领用流程与财务联调的前端改造，使内部关联统一切换到 `assetId`，新增/修改请求体统一切换到资产聚合 DTO。

**Architecture:** 先在 `src/api/asset` 与 `src/types` 中收敛接口契约，再把列表页、编辑抽屉、动态属性/附件、财务弹窗、领用页面中的数据转换逻辑拆到可单测的 helper / mapper。页面路由和主体布局尽量保持不变，优先保留现有 `ArtTable`、`ArtSearchBar`、`ArtForm` 的使用习惯，通过“分区表单 + 映射器”适配聚合模型，降低一次性重写风险。

**Tech Stack:** Vue 3、TypeScript、Element Plus、Vitest、art-design-pro、RuoYi 资产接口

---

## 实施基线

- 执行基线：`docs/plans/2026-03-14-asset-frontend-integration-plan.md` 中的“方案 B：分阶段聚合改造”。
- 接口基线：`docs/plans/2026-03-14-asset-api-contract-notes.md`。
- 当前前端真实触点：
  - `art-design-pro/src/api/asset/info.ts`
  - `art-design-pro/src/api/asset/requisition.ts`
  - `art-design-pro/src/views/asset/list/index.vue`
  - `art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue`
  - `art-design-pro/src/views/asset/requisition/index.vue`
- 当前后端已确认可直接使用的接口路径：
  - `GET /asset/info/list`
  - `GET /asset/info/{assetId}`
  - `POST /asset/info`
  - `PUT /asset/info`
  - `DELETE /asset/info/{assetId}`
  - `GET /asset/categoryAttr/category/{categoryId}`
  - `POST /asset/finance/{assetId}/recalculate`
  - `GET /asset/finance/{assetId}/depreciation-logs`
  - `POST /asset/finance/accrue/{period}`
  - `POST /common/upload`
- 本轮不新增独立维修/报废页面；`art-design-pro/src/views/asset/list/index.vue` 中的 `handleRepair` 仅统一“选中资产上下文模型”，继续保留占位提示。
- `art-design-pro/src/views/asset/workflow/todo/index.vue` 与 `art-design-pro/src/views/asset/workflow/done/index.vue` 本轮只做兼容性回归，不做结构重写。
- 已知契约差异必须先纠正：
  - 旧前端代码仍把 `status` 当资产状态字段，新接口已改为 `assetStatus`。
  - 旧前端代码仍把 `assetNo` 当内部主键，新接口已改为 `assetId`。
  - 旧前端注释仍存在 `assetType` 口径错误，当前后端口径已统一为 `1=固定资产`、`2=不动产`。
  - `returnAsset` 前端接口已存在，但当前 `AssetRequisitionController` 未看到对应归还接口；执行到领用页改造时必须先确认后端是否补齐，否则只能保留按钮占位或改成联调待办。

### Task 1: 收敛资产聚合类型与 API 契约

**文件：**
- 创建：`art-design-pro/src/types/asset.ts`
- 创建：`art-design-pro/src/api/asset/finance.ts`
- 创建：`art-design-pro/src/api/asset/category-attr.ts`
- 修改：`art-design-pro/src/api/asset/info.ts`
- 修改：`art-design-pro/src/api/asset/requisition.ts`
- 测试：`art-design-pro/tests/api/asset-info.test.ts`
- 测试：`art-design-pro/tests/api/asset-finance.test.ts`
- 测试：`art-design-pro/tests/api/asset-category-attr.test.ts`
- 测试：`art-design-pro/tests/api/asset-requisition.test.ts`
- 参考：`docs/plans/2026-03-14-asset-api-contract-notes.md`

**步骤 1：先写失败的 API 契约测试**

在 4 个测试文件里分别锁定以下事实：

- `getInfo` 和 `delInfo` 必须使用 `assetId`
- 财务重算和折旧日志接口必须走 `/asset/finance`
- 分类属性接口必须走 `/asset/categoryAttr/category/{categoryId}`
- 领用申请必须同时提交 `assetId` 和 `assetNo`

测试最小骨架：

```ts
import { beforeEach, describe, expect, it, vi } from 'vitest'

const http = {
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn(),
  request: vi.fn()
}

vi.mock('@/utils/http', () => ({ default: http }))

import { getInfo, delInfo } from '../../src/api/asset/info'
import { listCategoryAttrs } from '../../src/api/asset/category-attr'
import { listDepreciationLogs, recalculateFinance } from '../../src/api/asset/finance'
import { applyRequisition } from '../../src/api/asset/requisition'

describe('asset api contracts', () => {
  beforeEach(() => vi.clearAllMocks())

  it('uses assetId for detail and delete', async () => {
    await getInfo(101)
    await delInfo(101)
    expect(http.get).toHaveBeenCalledWith({ url: '/asset/info/101' })
    expect(http.del).toHaveBeenCalledWith({ url: '/asset/info/101' })
  })

  it('loads dynamic attributes by category', async () => {
    await listCategoryAttrs(10)
    expect(http.get).toHaveBeenCalledWith({ url: '/asset/categoryAttr/category/10' })
  })

  it('uses finance endpoints by assetId', async () => {
    await recalculateFinance(101)
    await listDepreciationLogs(101)
    expect(http.post).toHaveBeenCalledWith({ url: '/asset/finance/101/recalculate' })
    expect(http.get).toHaveBeenCalledWith({ url: '/asset/finance/101/depreciation-logs' })
  })

  it('posts requisition with assetId and assetNo', async () => {
    await applyRequisition({ assetId: 101, assetNo: 'FA-2026-0001', reason: '领用测试' })
    expect(http.post).toHaveBeenCalledWith({
      url: '/asset/requisition',
      data: { assetId: 101, assetNo: 'FA-2026-0001', reason: '领用测试' }
    })
  })
})
```

**步骤 2：运行测试确认失败**

Run: `cd art-design-pro && npx vitest run tests/api/asset-info.test.ts tests/api/asset-finance.test.ts tests/api/asset-category-attr.test.ts tests/api/asset-requisition.test.ts`

Expected: 失败，原因应包含以下至少一项：

- `finance.ts` 或 `category-attr.ts` 尚不存在
- `info.ts` 仍使用 `assetNo`
- `requisition.ts` 仍只传 `assetNo`

**步骤 3：补齐共享类型与 API 文件**

在 `art-design-pro/src/types/asset.ts` 中至少定义以下类型：

- `AssetBasicInfo`
- `AssetFinanceInfo`
- `AssetRealEstateInfo`
- `AssetDynamicAttrDefinition`
- `AssetDynamicAttrValue`
- `AssetAttachment`
- `AssetDepreciationLog`
- `AssetListItem`
- `AssetAggregateDetail`
- `AssetAggregateReq`

最小类型骨架：

```ts
export interface AssetBasicInfo {
  assetId?: number
  assetNo: string
  assetName: string
  categoryId?: number
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
}

export interface AssetFinanceInfo {
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
}

export interface AssetAggregateReq {
  basicInfo: AssetBasicInfo
  financeInfo: AssetFinanceInfo
  realEstateInfo: AssetRealEstateInfo | null
  dynamicAttrs: AssetDynamicAttrValue[]
  attachments: AssetAttachment[]
}
```

在 API 文件中统一使用聚合契约：

```ts
export function getInfo(assetId: number | string) {
  return request.get<AssetAggregateDetail>({
    url: `/asset/info/${assetId}`
  })
}

export function delInfo(assetId: number | string) {
  return request.del({
    url: `/asset/info/${assetId}`
  })
}

export function listCategoryAttrs(categoryId: number | string) {
  return request.get<AssetDynamicAttrDefinition[]>({
    url: `/asset/categoryAttr/category/${categoryId}`
  })
}

export function recalculateFinance(assetId: number | string) {
  return request.post<AssetFinanceInfo>({
    url: `/asset/finance/${assetId}/recalculate`
  })
}

export function listDepreciationLogs(assetId: number | string) {
  return request.get<AssetDepreciationLog[]>({
    url: `/asset/finance/${assetId}/depreciation-logs`
  })
}

export function accrueDepreciation(period: string) {
  return request.post({
    url: `/asset/finance/accrue/${period}`
  })
}

export function applyRequisition(data: { assetId: number; assetNo: string; reason: string }) {
  return request.post({
    url: '/asset/requisition',
    data
  })
}
```

同时修正 `info.ts` 中所有旧注释，不再出现“`assetType: 1=不动产 2=固定资产`”这种过期口径。

**步骤 4：运行测试与局部 lint**

Run: `cd art-design-pro && npx vitest run tests/api/asset-info.test.ts tests/api/asset-finance.test.ts tests/api/asset-category-attr.test.ts tests/api/asset-requisition.test.ts`

Expected: PASS

Run: `cd art-design-pro && npm run lint -- src/api/asset/info.ts src/api/asset/requisition.ts src/api/asset/finance.ts src/api/asset/category-attr.ts src/types/asset.ts`

Expected: PASS

**步骤 5：提交**

```bash
git add art-design-pro/src/types/asset.ts art-design-pro/src/api/asset/info.ts art-design-pro/src/api/asset/requisition.ts art-design-pro/src/api/asset/finance.ts art-design-pro/src/api/asset/category-attr.ts art-design-pro/tests/api/asset-info.test.ts art-design-pro/tests/api/asset-finance.test.ts art-design-pro/tests/api/asset-category-attr.test.ts art-design-pro/tests/api/asset-requisition.test.ts
git commit -m "feat: align asset frontend api contracts with aggregate model"
```

### Task 2: 迁移资产台账列表页到 `assetId`

**文件：**
- 创建：`art-design-pro/src/views/asset/list/asset-list.helper.ts`
- 修改：`art-design-pro/src/views/asset/list/index.vue`
- 测试：`art-design-pro/tests/views/asset/asset-list.helper.test.ts`
- 参考：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo/AssetListVo.java`

**步骤 1：先写失败的列表辅助函数测试**

测试锁定以下行为：

- 查询参数必须输出 `assetStatus` 而不是 `status`
- 表格多选必须收集 `assetId[]`
- 从列表行构造的业务申请上下文必须包含 `assetId + assetNo + assetName + assetStatus`

```ts
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
```

**步骤 2：运行测试确认失败**

Run: `cd art-design-pro && npx vitest run tests/views/asset/asset-list.helper.test.ts`

Expected: 失败，因为 `asset-list.helper.ts` 尚不存在。

**步骤 3：实现列表辅助函数**

```ts
export function buildAssetListQuery(filters: {
  assetNo?: string
  assetName?: string
  assetStatus?: string
  categoryId?: number
}) {
  return {
    assetNo: filters.assetNo?.trim() || undefined,
    assetName: filters.assetName?.trim() || undefined,
    assetStatus: filters.assetStatus || undefined,
    categoryId: filters.categoryId
  }
}

export function collectAssetIds(selection: Array<{ assetId: number }>) {
  return selection.map((item) => item.assetId)
}

export function toApplyAssetContext(row: {
  assetId: number
  assetNo: string
  assetName: string
  assetStatus: string
}) {
  return {
    assetId: row.assetId,
    assetNo: row.assetNo,
    assetName: row.assetName,
    assetStatus: row.assetStatus
  }
}
```

**步骤 4：改造 `art-design-pro/src/views/asset/list/index.vue`**

必须完成以下改动：

- `rowKey="assetId"`
- `ids` 从 `string[]` 改为 `number[]`
- 搜索表单字段从 `status` 改为 `assetStatus`
- 状态列从 `row.status` 改为 `row.assetStatus`
- 删除动作展示 `assetNo`，实际传给 `delInfo` 的是 `assetId`
- 编辑抽屉打开时传递 `assetId`
- 领用/维修入口统一保存 `toApplyAssetContext(row)`

同时按当前 `AssetListVo` 收敛表格字段，不要硬塞后端尚未返回的 `manageDeptId`、`responsibleUserId`。本轮列表页优先展示：

- `assetNo`
- `assetName`
- `assetType`
- `specModel`
- `locationText`
- `assetStatus`
- `purchaseDate`
- `capitalizationDate`

最小改造片段：

```ts
const ids = ref<number[]>([])

const initialSearchState = {
  assetNo: '',
  assetName: '',
  assetStatus: undefined as string | undefined
}

const handleSelectionChange = (selection: AssetListItem[]) => {
  ids.value = collectAssetIds(selection)
  multiple.value = selection.length === 0
}

const handleRequisition = (row: AssetListItem) => {
  applyType.value = 'requisition'
  applyTask.value = toApplyAssetContext(row)
  applyForm.reason = ''
  applyDialogVisible.value = true
}
```

`handleRepair` 继续只弹“暂无维修接口”，但 `applyTask` 也必须统一成同一套资产上下文结构，避免下一轮再返工。

**步骤 5：运行测试与 lint**

Run: `cd art-design-pro && npx vitest run tests/views/asset/asset-list.helper.test.ts`

Expected: PASS

Run: `cd art-design-pro && npm run lint -- src/views/asset/list/index.vue src/views/asset/list/asset-list.helper.ts`

Expected: PASS

**步骤 6：提交**

```bash
git add art-design-pro/src/views/asset/list/index.vue art-design-pro/src/views/asset/list/asset-list.helper.ts art-design-pro/tests/views/asset/asset-list.helper.test.ts
git commit -m "feat: migrate asset list interactions to asset id"
```

### Task 3: 将编辑抽屉改造成聚合分区表单

**文件：**
- 创建：`art-design-pro/src/views/asset/list/modules/asset-form.mapper.ts`
- 修改：`art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue`
- 修改：`art-design-pro/src/types/asset.ts`
- 测试：`art-design-pro/tests/views/asset/asset-form.mapper.test.ts`
- 参考：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetInfo.java`
- 参考：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetFinance.java`
- 参考：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRealEstate.java`

**步骤 1：先写失败的 mapper 测试**

测试锁定以下事实：

- 抽屉初始化时应生成 4 段状态：基础、财务、不动产、附件/动态属性
- 详情响应应能被拆解成抽屉状态
- 提交时应能从抽屉状态重新组装 `AssetAggregateReq`
- 编辑态才允许携带 `basicInfo.assetId`
- `assetType='2'` 时必须保留 `realEstateInfo`

```ts
import { describe, expect, it } from 'vitest'
import {
  buildAggregatePayload,
  createEmptyDrawerState,
  hydrateDrawerState
} from '../../../src/views/asset/list/modules/asset-form.mapper'

describe('asset form mapper', () => {
  it('creates empty drawer state', () => {
    const state = createEmptyDrawerState()
    expect(state.basicForm.assetNo).toBe('')
    expect(state.financeForm.currencyCode).toBe('CNY')
    expect(state.realEstateForm.propertyCertNo).toBe('')
    expect(state.attachments).toEqual([])
  })

  it('hydrates drawer state from aggregate detail', () => {
    const state = hydrateDrawerState({
      basicInfo: { assetId: 1, assetNo: 'FA-001', assetName: '房产', assetType: '2', assetStatus: '1' },
      financeInfo: { bookType: '1', currencyCode: 'CNY', originalValue: 1000000, salvageRate: 0.03, depreciationMethod: '1', usefulLifeMonth: 360, depreciationStartDate: '2026-03-01' },
      realEstateInfo: { propertyCertNo: '沪(2026)001号', addressFull: '测试地址' },
      dynamicAttrs: [],
      attachments: [],
      depreciationLogs: []
    })
    expect(state.basicForm.assetId).toBe(1)
    expect(state.realEstateForm.propertyCertNo).toBe('沪(2026)001号')
  })

  it('keeps assetId only in edit payload', () => {
    const state = createEmptyDrawerState()
    state.basicForm.assetId = 1
    state.basicForm.assetNo = 'FA-001'
    state.basicForm.assetName = '电脑'
    state.basicForm.assetType = '1'
    state.basicForm.assetStatus = '1'
    const addPayload = buildAggregatePayload(state, 'add')
    const editPayload = buildAggregatePayload(state, 'edit')
    expect(addPayload.basicInfo.assetId).toBeUndefined()
    expect(editPayload.basicInfo.assetId).toBe(1)
  })
})
```

**步骤 2：运行测试确认失败**

Run: `cd art-design-pro && npx vitest run tests/views/asset/asset-form.mapper.test.ts`

Expected: 失败，因为 `asset-form.mapper.ts` 尚不存在。

**步骤 3：实现抽屉状态与映射器**

不要继续把所有字段平铺在一个 `formData` 对象里。改为“分区状态 + 聚合映射器”：

```ts
export interface AssetDrawerState {
  basicForm: AssetBasicInfo
  financeForm: AssetFinanceInfo
  realEstateForm: AssetRealEstateInfo
  dynamicAttrForm: Record<string, unknown>
  attachments: AssetAttachment[]
}

export function createEmptyDrawerState(): AssetDrawerState {
  return {
    basicForm: {
      assetNo: '',
      assetName: '',
      assetType: '1',
      assetStatus: '1'
    },
    financeForm: {
      bookType: '1',
      currencyCode: 'CNY',
      originalValue: 0,
      salvageRate: 0,
      depreciationMethod: '1',
      usefulLifeMonth: 0,
      depreciationStartDate: ''
    },
    realEstateForm: {
      propertyCertNo: '',
      addressFull: ''
    },
    dynamicAttrForm: {},
    attachments: []
  }
}
```

`buildAggregatePayload` 需要负责：

- 新增时删掉 `basicInfo.assetId`
- 固定资产时返回 `realEstateInfo: null`
- 已存在附件直接透传，新增附件保留 `bizType/fileName/fileUrl/fileSize/fileSuffix`

**步骤 4：把抽屉页面改成分区表单**

在 `art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue` 中完成以下结构调整：

- 用 `ElTabs` 拆为 `basic`、`finance`、`realEstate`、`extras` 四个页签
- 基础页签至少包含：
  - `assetNo`
  - `assetName`
  - `categoryId`
  - `assetType`
  - `specModel`
  - `unit`
  - `manageDeptId`
  - `useDeptId`
  - `responsibleUserId`
  - `userId`
  - `locationText`
  - `assetStatus`
- 财务页签至少包含：
  - `bookType`
  - `currencyCode`
  - `originalValue`
  - `salvageRate`
  - `depreciationMethod`
  - `usefulLifeMonth`
  - `depreciationStartDate`
- 不动产页签按 `AssetRealEstate` 结构最少接入：
  - `propertyCertNo`
  - `realEstateUnitNo`
  - `addressFull`
  - `landUse`
  - `buildingUse`
  - `buildingArea`
  - `completionDate`
  - `rightsHolder`
- 详情回填必须改为：

```ts
if (props.dialogType === 'edit' && props.assetData?.assetId) {
  const detail = await getInfo(props.assetData.assetId)
  Object.assign(state, hydrateDrawerState(detail))
}
```

- 提交必须改为：

```ts
const payload = buildAggregatePayload(state, props.dialogType)
if (props.dialogType === 'edit') {
  await updateInfo(payload)
} else {
  await addInfo(payload)
}
```

**步骤 5：加上前端约束**

在抽屉中显式实现以下规则：

- `assetType === '2'` 时显示并校验不动产页签
- `assetType === '1'` 时隐藏不动产页签，提交 `realEstateInfo: null`
- 如果 `financeForm.accumulatedDepreciation > 0`，则将 `originalValue`、`salvageRate`、`depreciationMethod`、`usefulLifeMonth`、`depreciationStartDate` 置为只读

不要继续沿用旧字段名 `deptId`、`status`；统一改为 `useDeptId`、`assetStatus`。

**步骤 6：运行测试与 lint**

Run: `cd art-design-pro && npx vitest run tests/views/asset/asset-form.mapper.test.ts`

Expected: PASS

Run: `cd art-design-pro && npm run lint -- src/views/asset/list/modules/asset-edit-drawer.vue src/views/asset/list/modules/asset-form.mapper.ts src/types/asset.ts`

Expected: PASS

**步骤 7：提交**

```bash
git add art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue art-design-pro/src/views/asset/list/modules/asset-form.mapper.ts art-design-pro/src/types/asset.ts art-design-pro/tests/views/asset/asset-form.mapper.test.ts
git commit -m "feat: migrate asset edit drawer to aggregate form sections"
```

### Task 4: 接入动态属性与附件上传

**文件：**
- 创建：`art-design-pro/src/api/common/upload.ts`
- 创建：`art-design-pro/src/views/asset/list/modules/asset-dynamic-attr.helper.ts`
- 修改：`art-design-pro/src/api/asset/category-attr.ts`
- 修改：`art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue`
- 测试：`art-design-pro/tests/api/common-upload.test.ts`
- 测试：`art-design-pro/tests/views/asset/asset-dynamic-attr.helper.test.ts`
- 参考：`RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/common/CommonController.java`
- 参考：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetCategoryAttr.java`
- 参考：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetAttrValue.java`
- 参考：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetAttachment.java`

**步骤 1：先写失败的 helper 与上传测试**

测试锁定以下行为：

- 动态属性定义中的保留编码必须被识别出来
- 文本、数字、日期、JSON 四类值必须能映射到正确字段
- 通用上传封装必须走 `POST /common/upload`

```ts
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
```

**步骤 2：运行测试确认失败**

Run: `cd art-design-pro && npx vitest run tests/api/common-upload.test.ts tests/views/asset/asset-dynamic-attr.helper.test.ts`

Expected: 失败，因为上传封装和动态属性 helper 都还不存在。

**步骤 3：实现上传封装与动态属性 helper**

`art-design-pro/src/api/common/upload.ts` 最小实现：

```ts
import request from '@/utils/http'

export interface CommonUploadResp {
  url: string
  fileName: string
  newFileName: string
  originalFilename: string
}

export function uploadCommonFile(file: File) {
  const data = new FormData()
  data.append('file', file)
  return request.request<CommonUploadResp>({
    url: '/common/upload',
    method: 'POST',
    data
  })
}
```

`art-design-pro/src/views/asset/list/modules/asset-dynamic-attr.helper.ts` 最小实现：

```ts
const RESERVED_ATTR_CODES = ['asset_no', 'asset_name', 'original_value', 'property_cert_no']

export function findReservedAttrCodes(definitions: Array<{ attrCode: string }>) {
  return definitions
    .map((item) => item.attrCode)
    .filter((code) => RESERVED_ATTR_CODES.includes(code))
}

export function toDynamicAttrFormRecord(items: Array<any>) {
  return items.reduce<Record<string, unknown>>((acc, item) => {
    acc[item.attrCode] =
      item.attrValueText ?? item.attrValueNumber ?? item.attrValueDate ?? item.attrValueJson ?? ''
    return acc
  }, {})
}

export function buildDynamicAttrPayload(definitions: Array<any>, formRecord: Record<string, unknown>) {
  return definitions.map((item) => {
    const value = formRecord[item.attrCode]
    if (item.dataType === 'number') {
      return { attrId: item.attrId, categoryId: item.categoryId, attrCode: item.attrCode, attrValueNumber: Number(value) }
    }
    if (item.dataType === 'date') {
      return { attrId: item.attrId, categoryId: item.categoryId, attrCode: item.attrCode, attrValueDate: String(value || '') }
    }
    if (item.dataType === 'json') {
      return { attrId: item.attrId, categoryId: item.categoryId, attrCode: item.attrCode, attrValueJson: String(value || '') }
    }
    return { attrId: item.attrId, categoryId: item.categoryId, attrCode: item.attrCode, attrValueText: String(value || '') }
  })
}
```

**步骤 4：在编辑抽屉里接入动态属性与附件**

必须完成以下改造：

- 监听 `basicForm.categoryId`，切换分类时调用 `listCategoryAttrs(categoryId)`
- 对返回结果先执行 `findReservedAttrCodes`，发现冲突就阻断编辑
- 动态属性控件按 `dataType` 渲染：
  - `text` -> `ElInput`
  - `number` -> `ElInputNumber`
  - `date` -> `ElDatePicker`
  - `json` -> `ElInput type="textarea"`
- `optionSourceType` / `optionSource` 若存在，则优先渲染 `ElSelect`
- 使用 `uploadCommonFile` 作为 `ElUpload` 的 `http-request`
- 上传成功后把响应写入 `attachments[]`

最小附件接入片段：

```ts
const handleUploadRequest = async (options: UploadRequestOptions) => {
  const resp = await uploadCommonFile(options.file as File)
  state.attachments.push({
    bizType: activeBizType.value,
    fileName: resp.originalFilename || resp.newFileName,
    fileUrl: resp.url,
    fileSize: options.file.size,
    fileSuffix: (options.file.name.split('.').pop() || '').toLowerCase()
  })
  options.onSuccess?.(resp)
}
```

**步骤 5：运行测试与 lint**

Run: `cd art-design-pro && npx vitest run tests/api/common-upload.test.ts tests/views/asset/asset-dynamic-attr.helper.test.ts`

Expected: PASS

Run: `cd art-design-pro && npm run lint -- src/api/common/upload.ts src/views/asset/list/modules/asset-dynamic-attr.helper.ts src/views/asset/list/modules/asset-edit-drawer.vue`

Expected: PASS

**步骤 6：提交**

```bash
git add art-design-pro/src/api/common/upload.ts art-design-pro/src/views/asset/list/modules/asset-dynamic-attr.helper.ts art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue art-design-pro/tests/api/common-upload.test.ts art-design-pro/tests/views/asset/asset-dynamic-attr.helper.test.ts
git commit -m "feat: add dynamic asset attributes and attachment upload support"
```

### Task 5: 补齐财务重算与折旧日志弹窗

**文件：**
- 创建：`art-design-pro/src/views/asset/list/modules/asset-finance.helper.ts`
- 创建：`art-design-pro/src/views/asset/list/modules/asset-finance-dialog.vue`
- 修改：`art-design-pro/src/views/asset/list/index.vue`
- 修改：`art-design-pro/src/api/asset/finance.ts`
- 修改：`art-design-pro/src/types/asset.ts`
- 测试：`art-design-pro/tests/views/asset/asset-finance.helper.test.ts`
- 参考：`RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetFinanceController.java`
- 参考：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetDepreciationLog.java`

**步骤 1：先写失败的财务辅助函数测试**

测试锁定以下行为：

- 已开始折旧的资产不能再编辑基础财务字段
- 财务摘要字段输出稳定
- 折旧日志能被映射为表格行

```ts
import { describe, expect, it } from 'vitest'
import {
  canEditFinanceBaseFields,
  buildFinanceSummaryRows,
  buildDepreciationRows
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
    ).toHaveLength(3)
  })

  it('maps depreciation logs', () => {
    expect(
      buildDepreciationRows([
        { period: '2026-03', depreciationAmount: 26.94, accumulatedDepreciation: 26.94 }
      ])
    )[0].periodLabel.toString().includes('2026-03')
  })
})
```

**步骤 2：运行测试确认失败**

Run: `cd art-design-pro && npx vitest run tests/views/asset/asset-finance.helper.test.ts`

Expected: 失败，因为 `asset-finance.helper.ts` 尚不存在。

**步骤 3：实现财务辅助函数**

```ts
export function canEditFinanceBaseFields(finance: { accumulatedDepreciation?: number }) {
  return !finance.accumulatedDepreciation || finance.accumulatedDepreciation <= 0
}

export function buildFinanceSummaryRows(finance: any) {
  return [
    { label: '原值', value: finance.originalValue },
    { label: '净残值', value: finance.salvageValue },
    { label: '月折旧额', value: finance.monthlyDepreciationAmount }
  ]
}

export function buildDepreciationRows(logs: Array<any>) {
  return logs.map((item) => ({
    periodLabel: item.period,
    depreciationAmount: item.depreciationAmount,
    accumulatedDepreciation: item.accumulatedDepreciation,
    netBookValue: item.netBookValue,
    bookValue: item.bookValue,
    calcTime: item.calcTime,
    calcType: item.calcType
  }))
}
```

**步骤 4：新增财务弹窗组件**

`art-design-pro/src/views/asset/list/modules/asset-finance-dialog.vue` 至少实现：

- `props`:
  - `modelValue`
  - `assetId`
  - `assetNo`
- 打开时并行请求：
  - `getInfo(assetId)` 取财务快照和最近折旧日志预览
  - `listDepreciationLogs(assetId)` 取完整日志
- 页面内容至少包括：
  - 财务摘要
  - 重算按钮
  - 折旧日志表格

最小行为骨架：

```ts
const loadData = async () => {
  const [detail, logs] = await Promise.all([getInfo(props.assetId), listDepreciationLogs(props.assetId)])
  financeInfo.value = (detail.financeInfo ?? detail.data?.financeInfo) || {}
  depreciationRows.value = buildDepreciationRows(logs)
}

const handleRecalculate = async () => {
  await recalculateFinance(props.assetId)
  await loadData()
}
```

**步骤 5：从列表页挂接财务入口**

在 `art-design-pro/src/views/asset/list/index.vue` 中：

- 操作列新增“财务”按钮
- 保存 `currentFinanceAsset`
- 打开 `AssetFinanceDialog`
- 调整操作列宽度，避免按钮拥挤

```ts
const financeVisible = ref(false)
const currentFinanceAsset = ref<AssetListItem>()

const openFinanceDialog = (row: AssetListItem) => {
  currentFinanceAsset.value = row
  financeVisible.value = true
}
```

**步骤 6：运行测试与 lint**

Run: `cd art-design-pro && npx vitest run tests/views/asset/asset-finance.helper.test.ts tests/api/asset-finance.test.ts`

Expected: PASS

Run: `cd art-design-pro && npm run lint -- src/views/asset/list/index.vue src/views/asset/list/modules/asset-finance-dialog.vue src/views/asset/list/modules/asset-finance.helper.ts src/api/asset/finance.ts`

Expected: PASS

**步骤 7：提交**

```bash
git add art-design-pro/src/views/asset/list/index.vue art-design-pro/src/views/asset/list/modules/asset-finance-dialog.vue art-design-pro/src/views/asset/list/modules/asset-finance.helper.ts art-design-pro/src/api/asset/finance.ts art-design-pro/src/types/asset.ts art-design-pro/tests/views/asset/asset-finance.helper.test.ts
git commit -m "feat: add asset finance recalculate and depreciation log dialog"
```

### Task 6: 迁移领用流程页与申请弹窗到 `assetId`

**文件：**
- 创建：`art-design-pro/src/views/asset/requisition/requisition.helper.ts`
- 修改：`art-design-pro/src/views/asset/requisition/index.vue`
- 修改：`art-design-pro/src/views/asset/list/index.vue`
- 修改：`art-design-pro/src/api/asset/requisition.ts`
- 测试：`art-design-pro/tests/views/asset/requisition.helper.test.ts`
- 参考：`docs/plans/2026-03-12-asset-management-phase2-plan.md`
- 参考：`RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetRequisition.java`

**步骤 1：先写失败的领用辅助函数测试**

测试锁定以下行为：

- 领用申请请求体必须包含 `assetId`
- 领用台账的 `status` 必须映射到 `wf_status`
- 归还按钮只在“已通过”状态下展示

```ts
import { describe, expect, it } from 'vitest'
import {
  buildApplyRequisitionReq,
  canReturnAsset,
  mapRequisitionStatusToWorkflow
} from '../../../src/views/asset/requisition/requisition.helper'

describe('requisition helper', () => {
  it('builds apply payload with assetId', () => {
    expect(
      buildApplyRequisitionReq(
        { assetId: 1, assetNo: 'FA-001', assetName: '电脑', assetStatus: '1' },
        '领用测试'
      )
    ).toEqual({
      assetId: 1,
      assetNo: 'FA-001',
      reason: '领用测试'
    })
  })

  it('maps requisition status to workflow tag', () => {
    expect(mapRequisitionStatusToWorkflow(0)).toBe('IN_PROGRESS')
    expect(mapRequisitionStatusToWorkflow(1)).toBe('COMPLETED')
    expect(mapRequisitionStatusToWorkflow(2)).toBe('REJECTED')
    expect(mapRequisitionStatusToWorkflow(3)).toBe('COMPLETED')
  })

  it('shows return button only for approved rows', () => {
    expect(canReturnAsset({ status: 1 })).toBe(true)
    expect(canReturnAsset({ status: 0 })).toBe(false)
  })
})
```

**步骤 2：运行测试确认失败**

Run: `cd art-design-pro && npx vitest run tests/views/asset/requisition.helper.test.ts`

Expected: 失败，因为 `requisition.helper.ts` 尚不存在。

**步骤 3：实现领用辅助函数**

```ts
export function buildApplyRequisitionReq(
  asset: { assetId: number; assetNo: string },
  reason: string
) {
  return {
    assetId: asset.assetId,
    assetNo: asset.assetNo,
    reason
  }
}

export function mapRequisitionStatusToWorkflow(status: number) {
  if (status === 0) return 'IN_PROGRESS'
  if (status === 1 || status === 3) return 'COMPLETED'
  if (status === 2) return 'REJECTED'
  return ''
}

export function canReturnAsset(row: { status: number }) {
  return row.status === 1
}
```

**步骤 4：改造列表页申请弹窗**

在 `art-design-pro/src/views/asset/list/index.vue` 中完成：

- 申请弹窗显示 `assetNo`、`assetName`、`assetStatus`
- 提交领用时调用：

```ts
await applyRequisition(buildApplyRequisitionReq(applyTask.value, applyForm.reason))
```

- 维修入口继续保留“暂无维修接口”，但不再依赖旧的 `assetNo` 单字段模型

**步骤 5：改造领用台账页**

在 `art-design-pro/src/views/asset/requisition/index.vue` 中完成：

- 状态列统一调用 `mapRequisitionStatusToWorkflow`
- 若后端列表已返回 `assetId`，前端保留但不强行展示
- `assetNo` 继续显示为用户可识别字段
- `returnAsset` 只有在后端接口已确认存在时才继续调用

如果执行到这里发现后端仍未提供归还接口，则做以下处理之一，并在变更说明中写清楚：

- 临时隐藏“归还资产”按钮
- 保留按钮但改为提示“接口待补齐”

不要保留一个会 404 的操作按钮。

**步骤 6：运行测试与 lint**

Run: `cd art-design-pro && npx vitest run tests/views/asset/requisition.helper.test.ts tests/api/asset-requisition.test.ts`

Expected: PASS

Run: `cd art-design-pro && npm run lint -- src/views/asset/list/index.vue src/views/asset/requisition/index.vue src/views/asset/requisition/requisition.helper.ts src/api/asset/requisition.ts`

Expected: PASS

**步骤 7：提交**

```bash
git add art-design-pro/src/views/asset/list/index.vue art-design-pro/src/views/asset/requisition/index.vue art-design-pro/src/views/asset/requisition/requisition.helper.ts art-design-pro/src/api/asset/requisition.ts art-design-pro/tests/views/asset/requisition.helper.test.ts
git commit -m "feat: align requisition flow with asset id payloads"
```

### Task 7: 运行整体验证并完成联调回归

**文件：**
- 仅验证，不新增文件

**步骤 1：运行聚焦 Vitest 套件**

Run: `cd art-design-pro && npx vitest run tests/api/asset-info.test.ts tests/api/asset-finance.test.ts tests/api/asset-category-attr.test.ts tests/api/asset-requisition.test.ts tests/api/common-upload.test.ts tests/views/asset/asset-list.helper.test.ts tests/views/asset/asset-form.mapper.test.ts tests/views/asset/asset-dynamic-attr.helper.test.ts tests/views/asset/asset-finance.helper.test.ts tests/views/asset/requisition.helper.test.ts`

Expected: PASS

**步骤 2：运行前端 lint**

Run: `cd art-design-pro && npm run lint -- src/api/asset src/api/common/upload.ts src/views/asset`

Expected: PASS

**步骤 3：运行前端构建**

Run: `cd art-design-pro && npm run build`

Expected: `vue-tsc --noEmit` 与 `vite build` 都通过

**步骤 4：手工联调资产台账与编辑抽屉**

检查点：

- 台账列表查询使用 `assetStatus`
- 多选、编辑、删除都基于 `assetId`
- 新增固定资产成功
- 新增不动产成功
- 编辑已有资产时能通过 `assetId` 拉到聚合详情
- 已开始折旧的财务基础字段为只读

**步骤 5：手工联调动态属性与附件**

检查点：

- 选择分类后能拉出动态属性定义
- 动态属性能按 `text/number/date/json` 正确渲染
- 附件能通过 `/common/upload` 上传
- 附件上传后回显正常，提交请求体带上 `attachments[]`

**步骤 6：手工联调财务弹窗**

检查点：

- 列表页“财务”按钮能打开弹窗
- 弹窗能显示财务摘要
- 财务重算成功后摘要与折旧日志刷新
- 完整折旧日志来自 `GET /asset/finance/{assetId}/depreciation-logs`

**步骤 7：手工联调领用流程**

检查点：

- 从资产列表发起领用时，请求体包含 `assetId + assetNo + reason`
- 领用台账页显示 `assetNo` 正常
- `status` 与 `wf_status` 的映射正确
- 若归还接口未补齐，页面不会保留一个必定失败的按钮

**步骤 8：回归工作流页面**

手工打开：

- `art-design-pro/src/views/asset/workflow/todo/index.vue`
- `art-design-pro/src/views/asset/workflow/done/index.vue`

检查点：

- 页面能正常打开
- 本轮资产主键切换没有带来编译错误或运行时报错

**步骤 9：检查最终工作区**

Run: `git status --short`

Expected: 只包含资产前端聚合对接相关文件变更

**步骤 10：提交**

```bash
git add art-design-pro
git commit -m "feat: complete asset aggregate frontend integration"
```

## 联调前置清单

- 后端 `GET /asset/info/{assetId}` 已返回 `AssetDetailVo` 聚合结构
- 后端 `POST /asset/info` 与 `PUT /asset/info` 已接受聚合请求体
- 后端 `GET /asset/categoryAttr/category/{categoryId}` 已可用
- 后端 `POST /asset/finance/{assetId}/recalculate` 已可用
- 后端 `GET /asset/finance/{assetId}/depreciation-logs` 已可用
- 后端 `POST /common/upload` 已可用
- 后端领用接口已支持 `assetId`
- 若要保留归还按钮，必须先确认后端是否已提供归还接口

## 完成定义

- `assetNo` 不再承担前端内部主键职责，所有内部关联统一依赖 `assetId`
- `src/api/asset` 中的资产接口与后端聚合契约一致
- 列表页、编辑抽屉、领用申请、财务弹窗都能消费聚合模型
- 编辑抽屉已支持基础信息、财务信息、不动产信息、动态属性、附件
- 财务重算与完整折旧日志可以从台账页触达
- 领用流程页已按 `assetId` 组织请求与内部关联
- 对于当前仓库尚未落地或后端未补齐的能力，计划中已有明确降级策略，不留下“看起来能点、实际上必失败”的入口
