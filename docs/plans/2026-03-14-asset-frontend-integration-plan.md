# 资产数据模型前端对接实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 让 `art-design-pro` 兼容新的资产聚合接口，完成资产台账、资产编辑、领用流程、财务联调的前端改造，并保证现有资产工作流页面可继续使用。

**Architecture:** 采用“接口层先收敛、页面层后改造”的分阶段方案。先在 API 与类型层引入 `assetId` 和聚合模型，再升级台账列表与编辑抽屉，随后补齐动态属性、附件、财务与流程联调入口，避免一次性重写整套资产前端。

**Tech Stack:** Vue 3、TypeScript、Element Plus、art-design-pro、RuoYi 资产接口

---

## 对接方案选择

### 方案 A：最小兼容改造

- 仅修改 `src/api/asset/info.ts` 和现有抽屉数据结构，尽量保持现有页面不动。
- 优点：改动面小，最适合抢时间联调。
- 缺点：编辑抽屉会迅速膨胀，后续财务、动态属性、附件都很难继续扩展。

### 方案 B：分阶段聚合改造（推荐）

- 保留现有“台账列表 + 编辑抽屉 + 业务台账”的页面结构。
- 在接口层与类型层先切到聚合模型，再把编辑抽屉升级为分区表单，并补一个财务/折旧弹层。
- 优点：兼顾联调速度、代码可维护性和后续扩展空间。
- 缺点：前端需要同步改造的文件比方案 A 多。

### 方案 C：重做独立详情页/编辑页

- 新建独立的资产详情页、资产新增页、资产编辑页，抽屉只保留轻量操作。
- 优点：最利于中长期演进。
- 缺点：会引入新的路由、菜单和交互学习成本，当前联调周期内性价比不高。

**推荐：** 采用方案 B。它最贴合当前 `art-design-pro` 已有的资产页面组织方式，也与后端“聚合 DTO/VO + `assetId` 主键 + 独立财务接口”的设计最匹配。

### Task 1：收敛前端资产类型与接口契约

**Files:**
- Create: `art-design-pro/src/types/asset.ts`
- Create: `art-design-pro/src/api/asset/finance.ts`
- Create: `art-design-pro/src/api/asset/category-attr.ts`
- Modify: `art-design-pro/src/api/asset/info.ts`
- Modify: `art-design-pro/src/api/asset/requisition.ts`
- Reference: `docs/plans/2026-03-14-asset-api-contract-notes.md`

**Step 1: 定义新的资产领域类型**

在 `art-design-pro/src/types/asset.ts` 中至少定义以下类型：

- `AssetBasicInfo`
- `AssetFinanceInfo`
- `AssetRealEstateInfo`
- `AssetDynamicAttrValue`
- `AssetAttachment`
- `AssetListItem`
- `AssetAggregateReq`
- `AssetAggregateDetail`

类型约束要体现以下事实：

- `assetId` 是内部主键，`assetNo` 只承担展示和业务编码职责。
- 新增/修改请求统一采用 `basicInfo`、`financeInfo`、`realEstateInfo`、`dynamicAttrs`、`attachments` 五段式结构。
- `financeInfo` 中 `salvageValue`、`depreciableValue`、`monthlyDepreciationAmount`、`accumulatedDepreciation`、`netBookValue`、`bookValue` 为只读字段。

**Step 2: 修改资产主 API 文件**

将 `art-design-pro/src/api/asset/info.ts` 从单表 `AssetInfo` 改为聚合模型，至少完成以下调整：

- 列表接口返回 `AssetListItem[]`
- 详情接口改为 `getInfo(assetId: number | string)`
- 删除接口改为 `delInfo(assetId: number | string)`
- 新增/修改接口入参统一改为 `AssetAggregateReq`

接口路径按后端契约切换为：

```ts
GET    /asset/info/list
GET    /asset/info/{assetId}
POST   /asset/info
PUT    /asset/info
DELETE /asset/info/{assetId}
```

**Step 3: 新增分类属性和财务 API**

在新增 API 文件中补齐最小可用接口：

- `listCategoryAttrs(categoryId)`
- `recalculateFinance(assetId)`
- `listDepreciationLogs(assetId)`
- `accrueDepreciation(period)`

其中 `accrueDepreciation(period)` 先只在 API 层暴露，页面层可以第二阶段再接入。

**Step 4: 调整领用 API 契约**

将 `art-design-pro/src/api/asset/requisition.ts` 的申请结构从：

```ts
{ assetNo, reason }
```

调整为至少支持：

```ts
{ assetId, assetNo, reason }
```

其中：

- `assetId` 用于后端内部关联
- `assetNo` 仅用于表格展示或兼容过渡

**Step 5: 提交**

```bash
git add art-design-pro/src/types/asset.ts art-design-pro/src/api/asset/info.ts art-design-pro/src/api/asset/finance.ts art-design-pro/src/api/asset/category-attr.ts art-design-pro/src/api/asset/requisition.ts
git commit -m "feat: align frontend asset api contracts with aggregate model"
```

### Task 2：改造资产台账列表页以适配 `assetId`

**Files:**
- Modify: `art-design-pro/src/views/asset/list/index.vue`
- Modify: `art-design-pro/src/api/asset/info.ts`
- Modify: `art-design-pro/src/api/asset/requisition.ts`

**Step 1: 替换页面内部主键**

将列表页中所有“把 `assetNo` 当内部主键”的逻辑替换为 `assetId`：

- `rowKey` 从 `assetNo` 改为 `assetId`
- 多选 `ids` 改为 `number[]`
- 删除动作传 `assetId`
- 编辑动作传 `assetId`

展示层仍保留 `assetNo` 列。

**Step 2: 收敛筛选字段**

列表页查询参数至少统一为：

- `assetNo`
- `assetName`
- `categoryId`
- `assetType`
- `assetStatus`

旧字段 `status` 需要切换为 `assetStatus`，避免和业务流程单据状态混淆。

**Step 3: 调整表格展示字段**

列表页最少要兼容以下新字段：

- `assetStatus`
- `useDeptId`
- `manageDeptId`
- `responsibleUserId`

如果后端列表 VO 还未直接返回名称字段，前端先展示编码/ID 并在联调说明中记录缺口，不要在前端臆造映射。

**Step 4: 修正领用/维修入口的选中资产载荷**

从列表页发起业务申请时，保存如下最小上下文：

```ts
{
  assetId,
  assetNo,
  assetName,
  assetStatus
}
```

这样后续领用、维修、报废都能沿用同一套资产引用模型。

**Step 5: 提交**

```bash
git add art-design-pro/src/views/asset/list/index.vue art-design-pro/src/api/asset/info.ts art-design-pro/src/api/asset/requisition.ts
git commit -m "feat: migrate asset list page to asset id based interactions"
```

### Task 3：将编辑抽屉升级为聚合表单

**Files:**
- Modify: `art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue`
- Modify: `art-design-pro/src/types/asset.ts`
- Modify: `art-design-pro/src/api/asset/info.ts`
- Modify: `art-design-pro/src/api/asset/category-attr.ts`

**Step 1: 将抽屉表单模型改为聚合结构**

把当前平铺的 `formData`：

- `assetNo`
- `assetName`
- `categoryId`
- `assetType`
- `deptId`
- `userId`
- `status`

改为：

```ts
{
  basicInfo: {},
  financeInfo: {},
  realEstateInfo: null,
  dynamicAttrs: [],
  attachments: []
}
```

**Step 2: 重组页面区块**

抽屉内至少拆成以下 4 个表单区块：

- 基础信息
- 财务信息
- 不动产信息
- 动态属性与附件

推荐使用 `ElTabs` 或 `ElCollapse`，不要继续把所有字段堆在同一屏。

**Step 3: 实现资产类型联动**

联动规则至少包含：

- `assetType=1` 时显示财务区，不显示不动产区
- `assetType=2` 时显示财务区和不动产区
- 切换分类后重新拉取 `dynamicAttrs` 字段定义

**Step 4: 适配详情回填**

编辑态不再通过 `assetNo` 拉详情，而是通过 `assetId` 调用聚合详情接口，并完成以下映射：

- `basicInfo` 回填基础字段
- `financeInfo` 回填财务字段
- `realEstateInfo` 回填不动产字段
- `dynamicAttrs` 回填动态属性数组
- `attachments` 回填附件列表

**Step 5: 适配提交约束**

提交时遵循以下规则：

- 新增不传 `basicInfo.assetId`
- 修改必须传 `basicInfo.assetId`
- `financeInfo` 必填
- `assetType=2` 时 `realEstateInfo` 必填
- 已进入折旧期的资产，前端对应财务基础字段要置为只读

**Step 6: 提交**

```bash
git add art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue art-design-pro/src/types/asset.ts art-design-pro/src/api/asset/info.ts art-design-pro/src/api/asset/category-attr.ts
git commit -m "feat: upgrade asset edit drawer to aggregate form model"
```

### Task 4：补齐动态属性与附件联调

**Files:**
- Modify: `art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue`
- Modify: `art-design-pro/src/api/asset/category-attr.ts`
- Modify: `art-design-pro/src/types/asset.ts`

**Step 1: 按分类拉取动态属性定义**

在选择 `categoryId` 后调用分类属性定义接口，前端根据以下定义动态渲染控件：

- `dataType=text`
- `dataType=number`
- `dataType=date`
- `dataType=json`

同时尊重：

- `isRequired`
- `defaultValue`
- `optionSourceType`
- `optionSource`
- `validationRule`

**Step 2: 统一动态属性提交结构**

前端表单内部可以维护“字段编码到值”的映射，但提交给后端时必须转成数组：

```ts
[
  {
    attrId,
    categoryId,
    attrCode,
    attrValueText,
    attrValueNumber,
    attrValueDate,
    attrValueJson
  }
]
```

**Step 3: 做保留字段防覆盖校验**

前端拿到属性定义后，若发现以下保留字段编码直接报警并禁止继续编辑：

- `asset_no`
- `asset_name`
- `original_value`
- `property_cert_no`

**Step 4: 接入附件列表**

附件区最小能力包括：

- 上传
- 删除
- 列表展示
- 附件类型 `bizType` 标记

提交时统一归入 `attachments[]`，不要再把发票、合同号、产权附件塞回基础表单字段。

**Step 5: 提交**

```bash
git add art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue art-design-pro/src/api/asset/category-attr.ts art-design-pro/src/types/asset.ts
git commit -m "feat: support dynamic asset attributes and attachments"
```

### Task 5：补齐财务重算与折旧日志交互

**Files:**
- Create: `art-design-pro/src/views/asset/list/modules/asset-finance-dialog.vue`
- Modify: `art-design-pro/src/views/asset/list/index.vue`
- Modify: `art-design-pro/src/api/asset/finance.ts`
- Modify: `art-design-pro/src/types/asset.ts`

**Step 1: 新增单资产财务弹层**

弹层至少展示：

- 原值
- 净残值率
- 净残值
- 可折旧金额
- 折旧方法
- 使用月数
- 月折旧额
- 累计折旧
- 净值
- 账面价值
- 最近折旧期间

**Step 2: 接入财务重算**

在弹层中提供“财务重算”按钮，调用：

```ts
POST /asset/finance/{assetId}/recalculate
```

重算成功后刷新当前资产的财务快照和折旧日志。

**Step 3: 接入折旧日志查询**

通过：

```ts
GET /asset/finance/{assetId}/depreciation-logs
```

展示完整折旧日志列表。详情接口里自带的最近 12 条日志，只作为详情预览，不替代完整日志接口。

**Step 4: 预留月度计提入口**

将 `accrueDepreciation(period)` 先沉到 API 层，并在计划中明确：

- 第一阶段只做接口与权限预留
- 第二阶段再决定放在列表页工具栏还是独立财务页

**Step 5: 提交**

```bash
git add art-design-pro/src/views/asset/list/modules/asset-finance-dialog.vue art-design-pro/src/views/asset/list/index.vue art-design-pro/src/api/asset/finance.ts art-design-pro/src/types/asset.ts
git commit -m "feat: add asset finance recalculate and depreciation log ui"
```

### Task 6：同步业务流程页对 `assetId` 的依赖

**Files:**
- Modify: `art-design-pro/src/api/asset/requisition.ts`
- Modify: `art-design-pro/src/views/asset/requisition/index.vue`
- Modify: `art-design-pro/src/views/asset/list/index.vue`
- Reference: `docs/plans/2026-03-12-asset-management-phase2-plan.md`

**Step 1: 收敛领用申请载荷**

领用申请接口从只传 `assetNo` 改为以 `assetId` 为主：

```ts
{
  assetId,
  assetNo,
  reason
}
```

**Step 2: 保持业务台账展示友好**

领用台账页仍保留以下展示字段：

- `requisitionNo`
- `assetNo`
- `assetName`
- `reason`
- `status`

但内部所有需要再次关联资产详情、资产状态、资产操作权限的场景，都统一依赖 `assetId`。

**Step 3: 统一流程状态和资产状态语义**

前端要明确区分：

- 资产状态：`assetStatus`
- 业务单据状态：`status`
- 审批流程状态：`wf_status`

不要继续在资产台账页里混用 `status` 展示资产本体状态。

**Step 4: 为后续维修/报废对接预留相同模型**

虽然当前前端仓库里还没有完整的维修/报废页面，但本次对接要先统一约束：

- 所有业务申请都以 `assetId` 作为内部关联
- `assetNo` 只作展示字段
- 弹窗选中资产时统一保存 `assetId + assetNo + assetName + assetStatus`

**Step 5: 提交**

```bash
git add art-design-pro/src/api/asset/requisition.ts art-design-pro/src/views/asset/requisition/index.vue art-design-pro/src/views/asset/list/index.vue
git commit -m "feat: align asset workflow pages with asset id references"
```

### Task 7：完成联调验证与回归检查

**Files:**
- Verify only

**Step 1: 手工联调新增资产**

重点验证：

- 固定资产新增成功
- 不动产新增成功
- 分类切换后动态属性正常变化
- 附件可上传并回显

**Step 2: 手工联调编辑资产**

重点验证：

- 通过 `assetId` 拉取详情
- 编辑保存后列表数据刷新正确
- 已进入折旧期的财务基础字段不可直接修改

**Step 3: 手工联调财务功能**

重点验证：

- 单资产财务重算成功
- 折旧日志列表可打开
- 详情预览日志与完整日志接口数据口径一致

**Step 4: 手工联调业务流程**

重点验证：

- 从资产台账发起领用申请成功
- 领用台账显示 `assetNo` 正常
- 返回流程后仍能按 `assetId` 找到资产

**Step 5: 运行前端校验**

Run: `cd art-design-pro && npm run lint`
Expected: 无 ESLint 阻断问题

**Step 6: 运行构建校验**

Run: `cd art-design-pro && npm run build`
Expected: `vue-tsc --noEmit` 与 `vite build` 均通过

**Step 7: 检查最终变更集**

Run: `git status --short`
Expected: 仅包含资产前端对接相关文件改动

**Step 8: 提交**

```bash
git add art-design-pro
git commit -m "feat: complete frontend integration for asset aggregate model"
```

## 联调前置清单

- 后端已提供聚合详情接口 `GET /asset/info/{assetId}`
- 后端新增/修改接口已切到聚合请求体
- 后端财务接口已提供重算与折旧日志能力
- 后端分类属性定义接口已可按 `categoryId` 查询
- 领用流程接口已接受 `assetId` 作为内部关联字段

## 验收标准

- 资产列表、编辑、删除不再依赖 `assetNo` 作为内部主键
- 新增/编辑请求体已经完全切到聚合模型
- 固定资产与不动产都能在同一套前端入口内完成维护
- 动态属性、附件、财务信息都能在前端可见且可联调
- 现有领用流程页面不因 `assetId` 切换而失效
