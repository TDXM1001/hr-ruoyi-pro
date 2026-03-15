# 资产分类、台账与生命周期分层改造 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在现有资产聚合模型基础上，补齐分类属性模板管理，明确台账实例只消费模板并填写实例值，同时为固定资产与不动产的生命周期分叉建立稳定边界。

**Architecture:** 先补齐分类属性模板的前后端共享契约与前端管理入口，再收敛台账抽屉中动态属性的职责边界，最后通过资产类型约束把生命周期入口按固定资产与不动产分流。本轮不接工作流，不扩张固定资产之外的业务流程实现，只把边界与入口约束收稳。

**Tech Stack:** Vue 3、TypeScript、Element Plus、Vitest、Java 17、Spring Boot 3、MyBatis、RuoYi

## 执行拆分

- 批次一：Task 1、Task 2、Task 3，先收敛分类模板契约、分类页模板管理入口和台账实例填值边界。
- 批次二：Task 4，单独收敛固定资产与不动产的生命周期入口，避免和台账主数据调整交叉修改。
- 批次三：Task 5，统一做聚焦测试、局部 lint、前端构建与工作区核对，并记录非资产模块历史阻塞项。

## 执行记录

- 批次一已完成，对应提交：`e5ef306`、`5ede2e7`、`407fab3`
- 批次二已完成，对应提交：`b057683`
- 批次三已完成，对应提交：`cacb09f`
- 2026-03-15 分类属性模板新增回归修复已完成：
  - 已提交 `2fd3057`，修复新增模板时 `attr_type` 为空导致的数据库非空约束错误。
  - 当前批次补齐模板新增默认字段兜底与选项来源编码归一，避免 `is_unique`、`is_list_display`、`is_query_condition` 等字段缺失时再次插入失败。
- 2026-03-15 复核结果：
  - `npx vitest run tests/api/asset-category-attr.test.ts tests/views/asset/category-attr.helper.test.ts tests/views/asset/asset-dynamic-attr.helper.test.ts tests/views/asset/asset-form.mapper.test.ts tests/views/asset/asset-lifecycle.helper.test.ts` 通过，`5` 个测试文件、`18` 个用例全部通过。
  - `npm run lint -- src/api/asset/category-attr.ts src/views/asset/category src/views/asset/list` 通过。
  - `mvn -pl ruoyi-asset -am -DskipTests=true compile` 通过，`BUILD SUCCESS`。
  - `npm run build` 未通过，阻塞来自既有 `monitor` / `system` 模块类型问题，不属于本轮资产分类、台账和生命周期改造引入。

---

### Task 1: 收敛分类属性模板前后端契约

**Status:** 已完成（批次一，提交：`e5ef306`）

**Files:**
- Modify: `art-design-pro/src/types/asset.ts`
- Modify: `art-design-pro/src/api/asset/category-attr.ts`
- Modify: `art-design-pro/tests/api/asset-category-attr.test.ts`
- Verify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetCategoryAttrController.java`
- Verify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetCategoryAttrServiceImpl.java`

**Step 1: 写失败的分类属性 API 契约测试**

把现有 `art-design-pro/tests/api/asset-category-attr.test.ts` 扩成完整 CRUD 契约，至少锁定以下行为：

- `listCategoryAttrs(categoryId)` 走 `GET /asset/categoryAttr/category/{categoryId}`
- `getCategoryAttr(attrId)` 走 `GET /asset/categoryAttr/{attrId}`
- `addCategoryAttr(data)` 走 `POST /asset/categoryAttr`
- `updateCategoryAttr(data)` 走 `PUT /asset/categoryAttr`
- `disableCategoryAttr(attrId)` 走 `PUT /asset/categoryAttr/disable/{attrId}`
- `delCategoryAttr(attrIds)` 走 `DELETE /asset/categoryAttr/{attrIds}`

测试骨架：

```ts
import { beforeEach, describe, expect, it, vi } from 'vitest'

const http = {
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  del: vi.fn()
}

vi.mock('@/utils/http', () => ({ default: http }))

import {
  addCategoryAttr,
  delCategoryAttr,
  disableCategoryAttr,
  getCategoryAttr,
  listCategoryAttrs,
  updateCategoryAttr
} from '../../src/api/asset/category-attr'

describe('asset category attr api', () => {
  beforeEach(() => vi.clearAllMocks())

  it('loads attrs by category id', async () => {
    await listCategoryAttrs(10)
    expect(http.get).toHaveBeenCalledWith({ url: '/asset/categoryAttr/category/10' })
  })

  it('supports attr crud routes', async () => {
    await getCategoryAttr(1)
    await addCategoryAttr({ categoryId: 10, attrCode: 'manufacturer', attrName: '厂商' })
    await updateCategoryAttr({ attrId: 1, categoryId: 10, attrCode: 'manufacturer', attrName: '生产厂商' })
    await disableCategoryAttr(1)
    await delCategoryAttr([1, 2])

    expect(http.get).toHaveBeenCalledWith({ url: '/asset/categoryAttr/1' })
    expect(http.post).toHaveBeenCalledWith({
      url: '/asset/categoryAttr',
      data: { categoryId: 10, attrCode: 'manufacturer', attrName: '厂商' }
    })
    expect(http.put).toHaveBeenCalledWith({
      url: '/asset/categoryAttr',
      data: { attrId: 1, categoryId: 10, attrCode: 'manufacturer', attrName: '生产厂商' }
    })
    expect(http.put).toHaveBeenCalledWith({ url: '/asset/categoryAttr/disable/1' })
    expect(http.del).toHaveBeenCalledWith({ url: '/asset/categoryAttr/1,2' })
  })
})
```

**Step 2: 运行测试确认失败**

Run: `cd art-design-pro && npx vitest run tests/api/asset-category-attr.test.ts`

Expected: FAIL，原因应为 `category-attr.ts` 仅支持列表查询，CRUD 方法尚未补齐。

**Step 3: 扩展前端类型与 API 文件**

在 `art-design-pro/src/types/asset.ts` 中新增或补齐模板管理所需类型：

```ts
export interface AssetDynamicAttrDefinitionReq {
  attrId?: number
  categoryId: number
  attrCode: string
  attrName: string
  dataType?: string
  isRequired?: string
  defaultValue?: string
  optionSourceType?: string
  optionSource?: string
  validationRule?: string
  status?: string
  remark?: string
}
```

在 `art-design-pro/src/api/asset/category-attr.ts` 中实现完整 CRUD：

```ts
export function getCategoryAttr(attrId: number | string) {
  return request.get<AssetDynamicAttrDefinition>({
    url: `/asset/categoryAttr/${attrId}`
  })
}

export function addCategoryAttr(data: AssetDynamicAttrDefinitionReq) {
  return request.post({
    url: '/asset/categoryAttr',
    data
  })
}

export function updateCategoryAttr(data: AssetDynamicAttrDefinitionReq) {
  return request.put({
    url: '/asset/categoryAttr',
    data
  })
}

export function disableCategoryAttr(attrId: number | string) {
  return request.put({
    url: `/asset/categoryAttr/disable/${attrId}`
  })
}

export function delCategoryAttr(attrIds: Array<number | string>) {
  return request.del({
    url: `/asset/categoryAttr/${attrIds.join(',')}`
  })
}
```

**Step 4: 校验后端接口口径未漂移**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -am -DskipTests=true compile`

Expected: `BUILD SUCCESS`

若失败，只允许修复 `AssetCategoryAttrController`、`AssetCategoryAttrServiceImpl` 中与模板 CRUD 或保留字段校验直接相关的问题。

**Step 5: 重新运行测试并提交**

Run: `cd art-design-pro && npx vitest run tests/api/asset-category-attr.test.ts`

Expected: PASS

Run: `cd art-design-pro && npm run lint -- src/api/asset/category-attr.ts src/types/asset.ts`

Expected: PASS

```bash
git add art-design-pro/src/types/asset.ts art-design-pro/src/api/asset/category-attr.ts art-design-pro/tests/api/asset-category-attr.test.ts
git commit -m "feat: 补齐资产分类属性模板接口契约"
```

### Task 2: 在资产分类页补齐属性模板管理入口

**Status:** 已完成（批次一，提交：`5ede2e7`）

**Files:**
- Create: `art-design-pro/src/views/asset/category/category-attr.helper.ts`
- Create: `art-design-pro/src/views/asset/category/modules/category-attr-manager.vue`
- Create: `art-design-pro/src/views/asset/category/modules/category-attr-edit-dialog.vue`
- Modify: `art-design-pro/src/views/asset/category/index.vue`
- Test: `art-design-pro/tests/views/asset/category-attr.helper.test.ts`

**Step 1: 先写失败的辅助函数测试**

新增 `art-design-pro/tests/views/asset/category-attr.helper.test.ts`，锁定以下事实：

- 保留字段编码有稳定提示
- 模板状态能映射成页面标签
- 分类未选中时，模板区应为空态

```ts
import { describe, expect, it } from 'vitest'
import {
  buildAttrStatusTag,
  getReservedCodeMessage,
  shouldShowAttrPanel
} from '../../../src/views/asset/category/category-attr.helper'

describe('category attr helper', () => {
  it('maps attr status tag', () => {
    expect(buildAttrStatusTag('0')).toEqual({ type: 'success', label: '启用' })
    expect(buildAttrStatusTag('1')).toEqual({ type: 'info', label: '停用' })
  })

  it('builds reserved code message', () => {
    expect(getReservedCodeMessage('asset_no')).toContain('系统保留字段')
  })

  it('shows panel only when category is selected', () => {
    expect(shouldShowAttrPanel(undefined)).toBe(false)
    expect(shouldShowAttrPanel(10)).toBe(true)
  })
})
```

**Step 2: 运行测试确认失败**

Run: `cd art-design-pro && npx vitest run tests/views/asset/category-attr.helper.test.ts`

Expected: FAIL，因为 `category-attr.helper.ts` 尚不存在。

**Step 3: 写最小辅助函数实现**

```ts
export function shouldShowAttrPanel(categoryId?: number) {
  return Boolean(categoryId)
}

export function buildAttrStatusTag(status?: string) {
  return status === '1'
    ? { type: 'info', label: '停用' }
    : { type: 'success', label: '启用' }
}

export function getReservedCodeMessage(attrCode: string) {
  return `字段编码[${attrCode}]为系统保留字段，请调整后重试`
}
```

**Step 4: 在分类页挂接模板管理区域**

改造 [index.vue](/e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/category/index.vue)：

- 增加当前选中分类状态 `currentCategory`
- 表格行点击后高亮当前分类
- 在页面右侧或卡片下方挂载 `CategoryAttrManager`

最小接线骨架：

```ts
const currentCategory = ref<any>()

const handleCurrentChange = (row?: any) => {
  currentCategory.value = row
}
```

`CategoryAttrManager` 至少实现：

- 读取 `props.categoryId`
- 调用 `listCategoryAttrs`
- 展示模板列表
- 提供新增、编辑、禁用、删除按钮
- 打开 `CategoryAttrEditDialog`

`CategoryAttrEditDialog` 至少实现：

- 字段：`attrCode`、`attrName`、`dataType`、`isRequired`、`defaultValue`
- 可选字段：`optionSourceType`、`optionSource`、`validationRule`
- 提交时调用新增或修改 API

页面约束：

- 没有选中分类时，模板区展示空态
- 发现保留字段错误时，前端给出明确中文提示
- 不把模板管理塞进现有 `CategoryEditDialog`

**Step 5: 运行测试与局部 lint 并提交**

Run: `cd art-design-pro && npx vitest run tests/views/asset/category-attr.helper.test.ts tests/api/asset-category-attr.test.ts`

Expected: PASS

Run: `cd art-design-pro && npm run lint -- src/views/asset/category src/api/asset/category-attr.ts`

Expected: PASS

```bash
git add art-design-pro/src/views/asset/category/index.vue art-design-pro/src/views/asset/category/category-attr.helper.ts art-design-pro/src/views/asset/category/modules/category-attr-manager.vue art-design-pro/src/views/asset/category/modules/category-attr-edit-dialog.vue art-design-pro/tests/views/asset/category-attr.helper.test.ts
git commit -m "feat: 增加资产分类属性模板管理界面"
```

### Task 3: 收紧台账扩展信息只消费模板并填写实例值

**Status:** 已完成（批次一，提交：`407fab3`）

**Files:**
- Modify: `art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue`
- Modify: `art-design-pro/src/views/asset/list/modules/asset-dynamic-attr.helper.ts`
- Modify: `art-design-pro/src/views/asset/list/modules/asset-form.mapper.ts`
- Test: `art-design-pro/tests/views/asset/asset-dynamic-attr.helper.test.ts`
- Test: `art-design-pro/tests/views/asset/asset-form.mapper.test.ts`

**Step 1: 先扩失败用例，锁定模板与实例的职责边界**

在 `art-design-pro/tests/views/asset/asset-dynamic-attr.helper.test.ts` 中补两组行为：

- 模板定义缺失时，抽屉扩展信息为空态
- 只有模板允许的字段才进入 `dynamicAttrs[]`

追加测试片段：

```ts
it('keeps empty state when no attr definitions exist', () => {
  expect(buildDynamicAttrPayload([], { manufacturer: '联想' })).toEqual([])
})

it('builds payload only from definitions', () => {
  expect(
    buildDynamicAttrPayload(
      [{ attrId: 1, categoryId: 10, attrCode: 'manufacturer', dataType: 'text' }],
      { manufacturer: '联想', weight: 2.5 }
    )
  ).toEqual([
    { attrId: 1, categoryId: 10, attrCode: 'manufacturer', attrValueText: '联想' }
  ])
})
```

在 `art-design-pro/tests/views/asset/asset-form.mapper.test.ts` 中补充：

- 固定资产提交时 `realEstateInfo` 必须为 `null`
- 动态属性提交结果来源于模板定义而不是自由输入字段

**Step 2: 运行测试确认失败**

Run: `cd art-design-pro && npx vitest run tests/views/asset/asset-dynamic-attr.helper.test.ts tests/views/asset/asset-form.mapper.test.ts`

Expected: FAIL，原因应为当前 helper 还没有显式表达“只消费模板、不接收任意字段”的边界。

**Step 3: 写最小实现，显式限制扩展信息职责**

在 `asset-dynamic-attr.helper.ts` 中保持“以定义为准”：

```ts
export function buildDynamicAttrPayload(definitions: Array<any>, formRecord: Record<string, unknown>) {
  return definitions.map((item) => {
    const value = formRecord[item.attrCode]
    if (item.dataType === 'number') {
      return value === undefined || value === '' ? null : {
        attrId: item.attrId,
        categoryId: item.categoryId,
        attrCode: item.attrCode,
        attrValueNumber: Number(value)
      }
    }
    return {
      attrId: item.attrId,
      categoryId: item.categoryId,
      attrCode: item.attrCode,
      attrValueText: String(value || '')
    }
  }).filter(Boolean)
}
```

在 `asset-form.mapper.ts` 中保证：

- 固定资产：`realEstateInfo: null`
- 不动产：保留 `realEstateInfo`
- `dynamicAttrs` 只从 `state.dynamicAttrDefinitions` 和 `state.dynamicAttrForm` 构建

**Step 4: 调整抽屉页面说明与交互**

在 [asset-edit-drawer.vue](/e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue) 中补齐以下约束：

- 扩展信息页签头部明确展示“当前页仅填写实例值，模板请到资产分类维护”
- 当分类未选中时，展示空态而不是任意可编辑控件
- 当模板加载失败或返回为空时，不允许用户误以为可以直接新增模板字段

最小提示文案：

```vue
<ElAlert
  title="扩展信息仅维护当前资产实例值，模板字段请在资产分类页配置。"
  type="info"
  :closable="false"
  class="mb-4"
/>
```

**Step 5: 运行测试、lint 并提交**

Run: `cd art-design-pro && npx vitest run tests/views/asset/asset-dynamic-attr.helper.test.ts tests/views/asset/asset-form.mapper.test.ts`

Expected: PASS

Run: `cd art-design-pro && npm run lint -- src/views/asset/list/modules/asset-edit-drawer.vue src/views/asset/list/modules/asset-dynamic-attr.helper.ts src/views/asset/list/modules/asset-form.mapper.ts`

Expected: PASS

```bash
git add art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue art-design-pro/src/views/asset/list/modules/asset-dynamic-attr.helper.ts art-design-pro/src/views/asset/list/modules/asset-form.mapper.ts art-design-pro/tests/views/asset/asset-dynamic-attr.helper.test.ts art-design-pro/tests/views/asset/asset-form.mapper.test.ts
git commit -m "feat: 收紧资产台账扩展信息为实例填值"
```

### Task 4: 为固定资产与不动产建立生命周期入口约束

**Status:** 已完成（批次二，提交：`b057683`）

**Files:**
- Create: `art-design-pro/src/views/asset/list/asset-lifecycle.helper.ts`
- Modify: `art-design-pro/src/views/asset/list/index.vue`
- Modify: `art-design-pro/src/types/asset.ts`
- Test: `art-design-pro/tests/views/asset/asset-lifecycle.helper.test.ts`
- Reference: `docs/plans/2026-03-15-asset-classification-ledger-lifecycle-design.md`

**Step 1: 先写失败的入口约束测试**

新增 `art-design-pro/tests/views/asset/asset-lifecycle.helper.test.ts`，锁定以下事实：

- 固定资产允许展示领用、维修、报废入口
- 不动产不展示领用、维修入口
- 两类资产都保留“变更”和“删除”基础动作

```ts
import { describe, expect, it } from 'vitest'
import { buildLifecycleActions } from '../../../src/views/asset/list/asset-lifecycle.helper'

describe('asset lifecycle helper', () => {
  it('returns fixed asset actions', () => {
    expect(
      buildLifecycleActions({ assetType: '1', assetStatus: '1' }).map((item) => item.key)
    ).toEqual(expect.arrayContaining(['change', 'delete', 'requisition', 'repair', 'disposal']))
  })

  it('filters out fixed-asset-only actions for real estate', () => {
    expect(
      buildLifecycleActions({ assetType: '2', assetStatus: '1' }).map((item) => item.key)
    ).not.toEqual(expect.arrayContaining(['requisition', 'repair']))
  })
})
```

**Step 2: 运行测试确认失败**

Run: `cd art-design-pro && npx vitest run tests/views/asset/asset-lifecycle.helper.test.ts`

Expected: FAIL，因为 `asset-lifecycle.helper.ts` 尚不存在。

**Step 3: 写最小帮助函数**

```ts
export function buildLifecycleActions(asset: { assetType: string; assetStatus?: string }) {
  const commonActions = [
    { key: 'change', label: '变更' },
    { key: 'delete', label: '删除' }
  ]

  if (asset.assetType === '2') {
    return [
      ...commonActions,
      { key: 'realEstateChange', label: '权属变更' },
      { key: 'disposal', label: '注销/处置' }
    ]
  }

  return [
    ...commonActions,
    { key: 'requisition', label: '领用' },
    { key: 'repair', label: '维修' },
    { key: 'disposal', label: '报废/处置' }
  ]
}
```

**Step 4: 在台账列表页按资产类型控制入口**

改造 [index.vue](/e:/my-project/hr-ruoyi-pro/art-design-pro/src/views/asset/list/index.vue)：

- 操作列根据 `buildLifecycleActions(row)` 决定展示哪些入口
- 固定资产保留现有领用入口
- 不动产展示中文提示型占位动作，如“权属变更待规划”
- 本轮不接工作流，不新增必然失败的审批按钮

要求：

- “编辑”和“变更”语义分离
- 删除只作为基础动作出现，不等同于报废/注销
- 领用、维修、报废仅出现在固定资产上

**Step 5: 运行测试、lint 并提交**

Run: `cd art-design-pro && npx vitest run tests/views/asset/asset-lifecycle.helper.test.ts`

Expected: PASS

Run: `cd art-design-pro && npm run lint -- src/views/asset/list/index.vue src/views/asset/list/asset-lifecycle.helper.ts src/types/asset.ts`

Expected: PASS

```bash
git add art-design-pro/src/views/asset/list/index.vue art-design-pro/src/views/asset/list/asset-lifecycle.helper.ts art-design-pro/src/types/asset.ts art-design-pro/tests/views/asset/asset-lifecycle.helper.test.ts
git commit -m "feat: 按资产类型收敛生命周期入口"
```

### Task 5: 最终验证分类模板与台账主数据边界

**Status:** 已完成（批次三，提交：`cacb09f`；前端构建被 `monitor` / `system` 目录既有类型问题阻塞，非本轮资产改造引入）

**Files:**
- Verify only

**Step 1: 运行聚焦前端测试**

Run: `cd art-design-pro && npx vitest run tests/api/asset-category-attr.test.ts tests/views/asset/category-attr.helper.test.ts tests/views/asset/asset-dynamic-attr.helper.test.ts tests/views/asset/asset-form.mapper.test.ts tests/views/asset/asset-lifecycle.helper.test.ts`

Expected: PASS

**Step 2: 运行分类与台账相关 lint**

Run: `cd art-design-pro && npm run lint -- src/api/asset/category-attr.ts src/views/asset/category src/views/asset/list`

Expected: PASS

**Step 3: 运行前端构建**

Run: `cd art-design-pro && npm run build`

Expected: `vue-tsc --noEmit` 与 `vite build` 通过。若仍被非资产模块历史问题阻塞，必须在执行记录中明确指出阻塞文件和原因，不能把问题归因到本轮分类/台账改造。

实际复核阻塞文件与原因（2026-03-15）：

- `art-design-pro/src/views/monitor/job/index.vue`：布尔值回调参数类型与组件声明不匹配。
- `art-design-pro/src/views/monitor/logininfor/index.vue`：表格列 `sortable` 类型与 `ColumnOption` 定义不匹配。
- `art-design-pro/src/views/monitor/operlog/index.vue`：表格列 `sortable` 类型不匹配，且 `ArtButtonTable` 的 `type="info"` 不符合组件类型约束。
- `art-design-pro/src/views/monitor/server/index.vue`：数字类型不匹配、`unknown` 未收窄，以及 ECharts 实例空值/属性访问类型错误。
- `art-design-pro/src/views/system/log/logininfor/index.vue`：表格列 `sortable` 类型与 `ColumnOption` 定义不匹配。
- `art-design-pro/src/views/system/log/operlog/index.vue`：表格列 `sortable` 类型不匹配，且 `ArtButtonTable` 的 `type="info"` 不符合组件类型约束。
- `art-design-pro/src/views/system/operlog/index.vue`：缺失 `@/api/system/operlog`、`./modules/operlog-detail-dialog.vue` 模块声明，同时存在 `ArtButtonTable` 类型不匹配问题。

**Step 4: 手工回归关键链路**

检查点：

- 分类页可新增、编辑、禁用、删除模板
- 台账页切换分类后，只出现模板定义内的扩展字段
- 台账页扩展信息有明确提示“模板请到分类页维护”
- 固定资产显示领用/维修/报废入口
- 不动产不显示固定资产专属入口
- 页面中不存在明知会失败的工作流按钮

**Step 5: 检查工作区并提交**

Run: `git status --short`

Expected: 只包含分类模板管理、台账扩展信息边界、生命周期入口约束相关改动

```bash
git add art-design-pro
git commit -m "feat: 完成资产分类模板与台账边界收敛"
```
