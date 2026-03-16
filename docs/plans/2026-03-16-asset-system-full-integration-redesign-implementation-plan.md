# Asset System Full Integration Redesign Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 在 `art-design-pro` 与 `RuoYi-Vue` 中完成左侧资产系统全部菜单的统一契约收口、分阶段联调和回归验证。

**Architecture:** 采用“公共契约先收敛、主数据先落地、固定资产与不动产业务分包接入、流程治理最后收口”的推进方式。所有菜单统一围绕 `assetId`、资产聚合主档、统一业务单据模型和统一状态体系演进，避免继续按页面打补丁。

**Tech Stack:** Vue 3、TypeScript、Vitest、Element Plus、Spring Boot、MyBatis、JUnit、Maven、RuoYi

## 执行批次与状态

> 状态取值统一使用：`待开始` / `进行中` / `已完成` / `已阻塞`。

| 批次 | 对应任务 | 状态 | 说明 |
| --- | --- | --- | --- |
| 批次 1 | Task 1 输出现状差异矩阵并冻结基础口径 | 已完成 | 已完成基线矩阵、冻结项补充、计划拆分与状态跟踪落盘 |
| 批次 2 | Task 2 收敛前端共享类型和 API 契约 | 已完成 | 已补共享类型、业务单据基础字段，并通过前端 API 契约测试 |
| 批次 3 | Task 3 收敛后端 Controller、DTO 和状态回写基础契约 | 已完成 | 已补聚合 DTO 主键快捷口径、业务单据 `wfStatus` 与后端服务回填逻辑，并通过后端定向测试 |
| 批次 4 | Task 4 完成资产分类与动态属性建模收口 | 已完成 | 已收口属性编码 snake_case 归一化、前后端保留字段校验与弹窗开关字段 |
| 批次 5 | Task 5 完成资产台账聚合表单与财务附件收口 | 已完成 | 已补附件聚合映射回归测试，并统一财务基础字段只读口径覆盖 lastDepreciationPeriod |
| 批次 6 | Task 6 打通固定资产业务菜单 | 待开始 | 统一固定资产业务单据模型与状态回写 |
| 批次 7 | Task 7 打通不动产业务菜单 | 待开始 | 统一不动产业务单据模型与主档回写 |
| 批次 8 | Task 8 收口流程中心、报表预警与权限治理 | 待开始 | 收口流程跳转、报表聚合、预警口径与治理 SQL |
| 批次 9 | Task 9 执行全菜单联调回归与上线前验证 | 待开始 | 汇总回归结果并完成上线前验证 |

---

### Task 1: 输出现状差异矩阵并冻结基础口径

**Files:**
- Create: `docs/plans/2026-03-16-asset-system-gap-matrix.md`
- Modify: `docs/plans/2026-03-16-asset-system-full-integration-redesign-design.md`
- Reference: `art-design-pro/src/types/asset.ts`
- Reference: `art-design-pro/src/api/asset/info.ts`
- Reference: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetInfoController.java`

**Step 1: 盘点前端资产入口**

Run: `rg --files art-design-pro/src/views/asset art-design-pro/src/api/asset`
Expected: 输出全部资产菜单页面和 API 文件路径

**Step 2: 盘点后端资产入口**

Run: `rg --files RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset`
Expected: 输出资产 Controller、Service、Mapper 与 Domain 文件路径

**Step 3: 建立差异矩阵文档**

在 `docs/plans/2026-03-16-asset-system-gap-matrix.md` 中新增以下表头：

```md
| 菜单 | 前端页面 | 前端 API | 后端 Controller | 主键口径 | 状态口径 | 已知缺口 |
| --- | --- | --- | --- | --- | --- | --- |
```

**Step 4: 填写全部左侧菜单现状**

Expected: 资产分类、资产台账、领用归还、维修管理、报废处置、权属变更、用途变更、状态变更、注销处置全部有一行记录

**Step 5: 冻结统一口径**

在 `docs/plans/2026-03-16-asset-system-full-integration-redesign-design.md` 中追加“实施冻结项”小节，明确：

- 主键统一为 `assetId`
- 4 套状态统一命名
- 权限前缀与菜单命名本轮不再变动

**Step 6: 提交**

```bash
git add docs/plans/2026-03-16-asset-system-gap-matrix.md docs/plans/2026-03-16-asset-system-full-integration-redesign-design.md
git commit -m "docs: freeze asset system integration baseline"
```

### Task 2: 收敛前端共享类型和 API 契约

**Files:**
- Modify: `art-design-pro/src/types/asset.ts`
- Modify: `art-design-pro/src/api/asset/info.ts`
- Modify: `art-design-pro/src/api/asset/requisition.ts`
- Modify: `art-design-pro/src/api/asset/maintenance.ts`
- Modify: `art-design-pro/src/api/asset/disposal.ts`
- Modify: `art-design-pro/src/api/asset/real-estate-ownership.ts`
- Modify: `art-design-pro/src/api/asset/real-estate-usage.ts`
- Modify: `art-design-pro/src/api/asset/real-estate-status.ts`
- Modify: `art-design-pro/src/api/asset/real-estate-disposal.ts`
- Modify: `art-design-pro/src/api/asset/report.ts`
- Test: `art-design-pro/tests/api/asset-info.test.ts`
- Test: `art-design-pro/tests/api/asset-requisition.test.ts`
- Test: `art-design-pro/tests/api/asset-maintenance.test.ts`
- Test: `art-design-pro/tests/api/asset-disposal.test.ts`
- Test: `art-design-pro/tests/api/asset-real-estate-ownership.test.ts`
- Test: `art-design-pro/tests/api/asset-real-estate-usage.test.ts`
- Test: `art-design-pro/tests/api/asset-real-estate-status.test.ts`
- Test: `art-design-pro/tests/api/asset-real-estate-disposal.test.ts`
- Test: `art-design-pro/tests/api/asset-report.test.ts`

**Step 1: 为 `AssetRef` 和统一业务单据基类写失败测试**

在相关 API 测试中新增断言，至少覆盖：

```ts
expect(payload.assetId).toBeDefined()
expect(payload.assetNo).toBeDefined()
expect(payload.status ?? payload.assetStatus).not.toBeUndefined()
```

**Step 2: 运行前端 API 测试确认失败**

Run: `npx vitest run tests/api/asset-info.test.ts tests/api/asset-requisition.test.ts tests/api/asset-maintenance.test.ts tests/api/asset-disposal.test.ts`
Expected: 至少出现 1 条关于字段缺失或 URL 契约不匹配的 FAIL

**Step 3: 最小修改共享类型**

在 `art-design-pro/src/types/asset.ts` 中补齐：

- `AssetRef`
- `AssetBusinessOrderBase`
- 统一 `assetStatus / status / wfStatus / archiveStatus` 注释与字段

**Step 4: 最小修改 API 文件**

统一各业务申请的最小字段要求：

```ts
{
  assetId,
  assetNo,
  reason
}
```

不动产业务请求再叠加自身扩展字段。

**Step 5: 运行前端 API 测试确认通过**

Run: `npx vitest run tests/api/asset-info.test.ts tests/api/asset-requisition.test.ts tests/api/asset-maintenance.test.ts tests/api/asset-disposal.test.ts tests/api/asset-real-estate-ownership.test.ts tests/api/asset-real-estate-usage.test.ts tests/api/asset-real-estate-status.test.ts tests/api/asset-real-estate-disposal.test.ts tests/api/asset-report.test.ts`
Expected: PASS

**Step 6: 提交**

```bash
git add art-design-pro/src/types/asset.ts art-design-pro/src/api/asset
git commit -m "feat: unify frontend asset contracts across all menus"
```

### Task 3: 收敛后端 Controller、DTO 和状态回写基础契约

**Files:**
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetInfoController.java`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRequisitionController.java`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetMaintenanceController.java`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetDisposalController.java`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateOwnershipChangeController.java`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateUsageChangeController.java`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateStatusChangeController.java`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetRealEstateDisposalController.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/dto/AssetCreateReq.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/dto/AssetUpdateReq.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetAggregateServiceImplTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRequisitionServiceImplTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetMaintenanceServiceImplTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetDisposalServiceImplTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateOwnershipChangeServiceImplTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateUsageChangeServiceImplTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateStatusChangeServiceImplTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateDisposalServiceImplTest.java`

**Step 1: 为统一主键和状态语义补失败测试**

在对应后端测试中新增断言，至少覆盖：

```java
assertThat(request.getAssetId()).isNotNull();
assertThat(result.getStatus()).isNotBlank();
assertThat(result.getWfStatus()).isNotBlank();
```

**Step 2: 运行后端测试确认失败**

Run: `mvn -pl ruoyi-asset "-Dtest=AssetAggregateServiceImplTest,AssetRequisitionServiceImplTest,AssetMaintenanceServiceImplTest,AssetDisposalServiceImplTest,AssetRealEstateOwnershipChangeServiceImplTest,AssetRealEstateUsageChangeServiceImplTest,AssetRealEstateStatusChangeServiceImplTest,AssetRealEstateDisposalServiceImplTest" test`
Expected: FAIL，体现字段未统一或状态未回写

**Step 3: 收敛 Controller 入参与响应字段**

确保各 Controller 暴露的入参/响应至少统一包含：

- `assetId`
- `assetNo`
- `status`
- `wfStatus`（适用审批菜单）

**Step 4: 收敛 DTO 与 Service 边界**

在 DTO 中明确：

- 主档写入走聚合请求体
- 业务动作创建走单据公共字段 + 扩展字段

**Step 5: 运行后端测试确认通过**

Run: `mvn -pl ruoyi-asset "-Dtest=AssetAggregateServiceImplTest,AssetRequisitionServiceImplTest,AssetMaintenanceServiceImplTest,AssetDisposalServiceImplTest,AssetRealEstateOwnershipChangeServiceImplTest,AssetRealEstateUsageChangeServiceImplTest,AssetRealEstateStatusChangeServiceImplTest,AssetRealEstateDisposalServiceImplTest" test`
Expected: PASS

**Step 6: 提交**

```bash
git add RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/dto RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl
git commit -m "feat: align backend asset contracts for full menu integration"
```

### Task 4: 完成资产分类与动态属性建模收口

**Files:**
- Modify: `art-design-pro/src/views/asset/category/index.vue`
- Modify: `art-design-pro/src/views/asset/category/modules/category-edit-dialog.vue`
- Modify: `art-design-pro/src/views/asset/category/modules/category-attr-manager.vue`
- Modify: `art-design-pro/src/views/asset/category/modules/category-attr-edit-dialog.vue`
- Modify: `art-design-pro/src/api/asset/category.ts`
- Modify: `art-design-pro/src/api/asset/category-attr.ts`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetCategoryController.java`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetCategoryAttrController.java`
- Test: `art-design-pro/tests/api/asset-category-attr.test.ts`
- Test: `art-design-pro/tests/views/asset/category-attr.helper.test.ts`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetCategoryAttrServiceImplTest.java`

**Step 1: 为分类属性冲突校验写失败测试**

至少覆盖保留字段冲突：

```ts
expect(() => validateReservedAttrCode('asset_no')).toThrow()
```

**Step 2: 运行前后端相关测试确认失败**

Run: `npx vitest run tests/api/asset-category-attr.test.ts tests/views/asset/category-attr.helper.test.ts`
Expected: FAIL

Run: `mvn -pl ruoyi-asset -Dtest=AssetCategoryAttrServiceImplTest test`
Expected: FAIL

**Step 3: 最小修改前端分类与属性页面**

确保前端能明确维护：

- 分类树
- 属性定义
- 校验规则
- 是否必填
- 是否列表展示

**Step 4: 最小修改后端分类接口**

确保后端返回足够驱动动态表单的属性元数据。

**Step 5: 重新运行测试确认通过**

Run: `npx vitest run tests/api/asset-category-attr.test.ts tests/views/asset/category-attr.helper.test.ts`
Expected: PASS

Run: `mvn -pl ruoyi-asset -Dtest=AssetCategoryAttrServiceImplTest test`
Expected: PASS

**Step 6: 提交**

```bash
git add art-design-pro/src/views/asset/category art-design-pro/src/api/asset/category.ts art-design-pro/src/api/asset/category-attr.ts RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetCategoryController.java RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetCategoryAttrController.java
git commit -m "feat: complete asset classification contract and dynamic attribute modeling"
```

### Task 5: 完成资产台账聚合表单与财务附件收口

**Files:**
- Modify: `art-design-pro/src/views/asset/list/index.vue`
- Modify: `art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue`
- Modify: `art-design-pro/src/views/asset/list/modules/asset-form.mapper.ts`
- Modify: `art-design-pro/src/views/asset/list/modules/asset-dynamic-attr.helper.ts`
- Modify: `art-design-pro/src/views/asset/list/modules/asset-finance-dialog.vue`
- Modify: `art-design-pro/src/views/asset/list/modules/asset-finance.helper.ts`
- Modify: `art-design-pro/src/views/asset/list/asset-list.helper.ts`
- Modify: `art-design-pro/src/api/asset/info.ts`
- Modify: `art-design-pro/src/api/asset/finance.ts`
- Modify: `art-design-pro/src/api/common/upload.ts`
- Test: `art-design-pro/tests/api/asset-info.test.ts`
- Test: `art-design-pro/tests/api/asset-finance.test.ts`
- Test: `art-design-pro/tests/api/common-upload.test.ts`
- Test: `art-design-pro/tests/views/asset/asset-form.mapper.test.ts`
- Test: `art-design-pro/tests/views/asset/asset-dynamic-attr.helper.test.ts`
- Test: `art-design-pro/tests/views/asset/asset-finance.helper.test.ts`
- Test: `art-design-pro/tests/views/asset/asset-list.helper.test.ts`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetInfoServiceImplTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetAggregateDetailRecentLogsTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetDepreciationServiceImplTest.java`

**Step 1: 为聚合表单映射和财务只读规则写失败测试**

至少覆盖：

```ts
expect(payload.basicInfo.assetId).toBe(assetId)
expect(payload.attachments).toHaveLength(1)
expect(isFinanceFieldReadonly({ lastDepreciationPeriod: '2026-03' })).toBe(true)
```

**Step 2: 运行前端测试确认失败**

Run: `npx vitest run tests/api/asset-info.test.ts tests/api/asset-finance.test.ts tests/api/common-upload.test.ts tests/views/asset/asset-form.mapper.test.ts tests/views/asset/asset-dynamic-attr.helper.test.ts tests/views/asset/asset-finance.helper.test.ts tests/views/asset/asset-list.helper.test.ts`
Expected: FAIL

**Step 3: 最小修改前端资产台账**

完成以下最小能力：

- 统一 `assetId` 作为内部主键
- 聚合详情回填
- 动态属性按分类渲染
- 附件上传与回显
- 财务重算与折旧日志弹层

**Step 4: 补齐后端主档聚合与财务细节**

确保后端主档详情、最近折旧日志、重算接口和附件/动态属性落库口径一致。

**Step 5: 运行前后端测试确认通过**

Run: `npx vitest run tests/api/asset-info.test.ts tests/api/asset-finance.test.ts tests/api/common-upload.test.ts tests/views/asset/asset-form.mapper.test.ts tests/views/asset/asset-dynamic-attr.helper.test.ts tests/views/asset/asset-finance.helper.test.ts tests/views/asset/asset-list.helper.test.ts`
Expected: PASS

Run: `mvn -pl ruoyi-asset "-Dtest=AssetInfoServiceImplTest,AssetAggregateDetailRecentLogsTest,AssetDepreciationServiceImplTest" test`
Expected: PASS

**Step 6: 提交**

```bash
git add art-design-pro/src/views/asset/list art-design-pro/src/api/asset/info.ts art-design-pro/src/api/asset/finance.ts art-design-pro/src/api/common/upload.ts RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl
git commit -m "feat: complete aggregate ledger form finance and attachments integration"
```

### Task 6: 打通固定资产业务菜单

**Files:**
- Modify: `art-design-pro/src/views/asset/requisition/index.vue`
- Modify: `art-design-pro/src/views/asset/requisition/requisition.helper.ts`
- Modify: `art-design-pro/src/views/asset/maintenance/index.vue`
- Modify: `art-design-pro/src/views/asset/disposal/index.vue`
- Modify: `art-design-pro/src/views/asset/list/asset-lifecycle.helper.ts`
- Modify: `art-design-pro/src/api/asset/requisition.ts`
- Modify: `art-design-pro/src/api/asset/maintenance.ts`
- Modify: `art-design-pro/src/api/asset/disposal.ts`
- Test: `art-design-pro/tests/api/asset-requisition.test.ts`
- Test: `art-design-pro/tests/api/asset-maintenance.test.ts`
- Test: `art-design-pro/tests/api/asset-disposal.test.ts`
- Test: `art-design-pro/tests/views/asset/requisition.helper.test.ts`
- Test: `art-design-pro/tests/views/asset/asset-lifecycle.helper.test.ts`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRequisitionServiceImplTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetMaintenanceServiceImplTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetDisposalServiceImplTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetTimelineServiceImplTest.java`

**Step 1: 为固定资产业务单据统一模型写失败测试**

至少覆盖：

```ts
expect(createPayload.assetId).toBeGreaterThan(0)
expect(row.status).toBeDefined()
expect(row.wfStatus ?? 'N/A').toBeTruthy()
```

**Step 2: 运行前后端测试确认失败**

Run: `npx vitest run tests/api/asset-requisition.test.ts tests/api/asset-maintenance.test.ts tests/api/asset-disposal.test.ts tests/views/asset/requisition.helper.test.ts tests/views/asset/asset-lifecycle.helper.test.ts`
Expected: FAIL

Run: `mvn -pl ruoyi-asset "-Dtest=AssetRequisitionServiceImplTest,AssetMaintenanceServiceImplTest,AssetDisposalServiceImplTest,AssetTimelineServiceImplTest" test`
Expected: FAIL

**Step 3: 最小修改前端固定资产业务页**

确保所有动作从资产台账接收统一 `AssetRef`，并展示：

- 业务单据状态 `status`
- 审批状态 `wfStatus`
- 资产快照

**Step 4: 最小修改后端状态回写**

确保审批通过、归还、完工、报废/处置时同步回写资产状态和时间线。

**Step 5: 运行测试确认通过**

Run: `npx vitest run tests/api/asset-requisition.test.ts tests/api/asset-maintenance.test.ts tests/api/asset-disposal.test.ts tests/views/asset/requisition.helper.test.ts tests/views/asset/asset-lifecycle.helper.test.ts`
Expected: PASS

Run: `mvn -pl ruoyi-asset "-Dtest=AssetRequisitionServiceImplTest,AssetMaintenanceServiceImplTest,AssetDisposalServiceImplTest,AssetTimelineServiceImplTest" test`
Expected: PASS

**Step 6: 提交**

```bash
git add art-design-pro/src/views/asset/requisition art-design-pro/src/views/asset/maintenance/index.vue art-design-pro/src/views/asset/disposal/index.vue art-design-pro/src/views/asset/list/asset-lifecycle.helper.ts art-design-pro/src/api/asset/requisition.ts art-design-pro/src/api/asset/maintenance.ts art-design-pro/src/api/asset/disposal.ts
git commit -m "feat: align fixed asset business menus with unified order model"
```

### Task 7: 打通不动产业务菜单

**Files:**
- Modify: `art-design-pro/src/views/asset/real-estate/ownership/index.vue`
- Modify: `art-design-pro/src/views/asset/real-estate/usage/index.vue`
- Modify: `art-design-pro/src/views/asset/real-estate/status/index.vue`
- Modify: `art-design-pro/src/views/asset/real-estate/disposal/index.vue`
- Modify: `art-design-pro/src/views/asset/real-estate/real-estate-lifecycle.helper.ts`
- Modify: `art-design-pro/src/api/asset/real-estate-ownership.ts`
- Modify: `art-design-pro/src/api/asset/real-estate-usage.ts`
- Modify: `art-design-pro/src/api/asset/real-estate-status.ts`
- Modify: `art-design-pro/src/api/asset/real-estate-disposal.ts`
- Test: `art-design-pro/tests/api/asset-real-estate-ownership.test.ts`
- Test: `art-design-pro/tests/api/asset-real-estate-usage.test.ts`
- Test: `art-design-pro/tests/api/asset-real-estate-status.test.ts`
- Test: `art-design-pro/tests/api/asset-real-estate-disposal.test.ts`
- Test: `art-design-pro/tests/views/asset/real-estate-ledger.helper.test.ts`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateOwnershipChangeServiceImplTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateUsageChangeServiceImplTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateStatusChangeServiceImplTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetRealEstateDisposalServiceImplTest.java`

**Step 1: 为不动产业务统一模型写失败测试**

至少覆盖：

```ts
expect(form.assetId).toBeDefined()
expect(form.assetNo).toBeDefined()
expect(form.targetStatus ?? form.targetRightsHolder ?? form.targetLandUse).toBeDefined()
```

**Step 2: 运行前后端测试确认失败**

Run: `npx vitest run tests/api/asset-real-estate-ownership.test.ts tests/api/asset-real-estate-usage.test.ts tests/api/asset-real-estate-status.test.ts tests/api/asset-real-estate-disposal.test.ts tests/views/asset/real-estate-ledger.helper.test.ts`
Expected: FAIL

Run: `mvn -pl ruoyi-asset "-Dtest=AssetRealEstateOwnershipChangeServiceImplTest,AssetRealEstateUsageChangeServiceImplTest,AssetRealEstateStatusChangeServiceImplTest,AssetRealEstateDisposalServiceImplTest" test`
Expected: FAIL

**Step 3: 最小修改前端不动产业务页**

确保 4 个页面都遵循：

- 引用统一 `AssetRef`
- 展示统一单据状态
- 从资产主档回填不动产事实字段

**Step 4: 最小修改后端回写逻辑**

确保 4 类动作创建后对不动产主档字段的回写规则稳定可测。

**Step 5: 运行测试确认通过**

Run: `npx vitest run tests/api/asset-real-estate-ownership.test.ts tests/api/asset-real-estate-usage.test.ts tests/api/asset-real-estate-status.test.ts tests/api/asset-real-estate-disposal.test.ts tests/views/asset/real-estate-ledger.helper.test.ts`
Expected: PASS

Run: `mvn -pl ruoyi-asset "-Dtest=AssetRealEstateOwnershipChangeServiceImplTest,AssetRealEstateUsageChangeServiceImplTest,AssetRealEstateStatusChangeServiceImplTest,AssetRealEstateDisposalServiceImplTest" test`
Expected: PASS

**Step 6: 提交**

```bash
git add art-design-pro/src/views/asset/real-estate art-design-pro/src/api/asset/real-estate-ownership.ts art-design-pro/src/api/asset/real-estate-usage.ts art-design-pro/src/api/asset/real-estate-status.ts art-design-pro/src/api/asset/real-estate-disposal.ts
git commit -m "feat: align real estate business menus with unified order model"
```

### Task 8: 收口流程中心、报表预警与权限治理

**Files:**
- Modify: `art-design-pro/src/views/asset/workflow/todo/index.vue`
- Modify: `art-design-pro/src/views/asset/workflow/done/index.vue`
- Modify: `art-design-pro/src/views/asset/report/index.vue`
- Modify: `art-design-pro/src/api/workflow/task.ts`
- Modify: `art-design-pro/src/api/asset/report.ts`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/workflow/WorkflowTaskController.java`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetReportController.java`
- Test: `art-design-pro/tests/api/workflow-task.test.ts`
- Test: `art-design-pro/tests/api/asset-report.test.ts`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetWorkflowBusinessHandlerTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetReportServiceImplTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/sql/AssetWorkflowSqlMigrationContractTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/sql/AssetMenuSqlContractTest.java`
- Test: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/sql/AssetGovernanceSqlContractTest.java`

**Step 1: 为待办跳转、报表口径和权限收口写失败测试**

至少覆盖：

```ts
expect(task.businessType).toBeDefined()
expect(summary.byStatus).toBeDefined()
expect(warnings.length).toBeGreaterThanOrEqual(0)
```

**Step 2: 运行前后端测试确认失败**

Run: `npx vitest run tests/api/workflow-task.test.ts tests/api/asset-report.test.ts`
Expected: FAIL

Run: `mvn -pl ruoyi-asset "-Dtest=AssetWorkflowBusinessHandlerTest,AssetReportServiceImplTest,AssetWorkflowSqlMigrationContractTest,AssetMenuSqlContractTest,AssetGovernanceSqlContractTest" test`
Expected: FAIL

**Step 3: 最小修改流程中心与报表页**

确保：

- 待办/已办按 `bizType + bizNo + wfStatus` 跳转
- 报表按资产状态、业务状态和流程状态分别聚合
- 预警口径与资产主档一致

**Step 4: 最小修改后端流程与报表接口**

确保：

- 流程任务返回统一业务类型
- 报表与预警的聚合字段口径稳定
- 菜单/权限 SQL 合约测试通过

**Step 5: 运行测试确认通过**

Run: `npx vitest run tests/api/workflow-task.test.ts tests/api/asset-report.test.ts`
Expected: PASS

Run: `mvn -pl ruoyi-asset "-Dtest=AssetWorkflowBusinessHandlerTest,AssetReportServiceImplTest,AssetWorkflowSqlMigrationContractTest,AssetMenuSqlContractTest,AssetGovernanceSqlContractTest" test`
Expected: PASS

**Step 6: 提交**

```bash
git add art-design-pro/src/views/asset/workflow art-design-pro/src/views/asset/report/index.vue art-design-pro/src/api/workflow/task.ts art-design-pro/src/api/asset/report.ts RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/workflow/WorkflowTaskController.java RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetReportController.java
git commit -m "feat: complete workflow reporting and governance integration for asset system"
```

### Task 9: 执行全菜单联调回归与上线前验证

**Files:**
- Verify only

**Step 1: 运行前端资产与流程测试**

Run: `npx vitest run tests/api/asset-info.test.ts tests/api/asset-category-attr.test.ts tests/api/asset-requisition.test.ts tests/api/asset-maintenance.test.ts tests/api/asset-disposal.test.ts tests/api/asset-real-estate-ownership.test.ts tests/api/asset-real-estate-usage.test.ts tests/api/asset-real-estate-status.test.ts tests/api/asset-real-estate-disposal.test.ts tests/api/asset-report.test.ts tests/api/workflow-task.test.ts tests/views/asset/asset-form.mapper.test.ts tests/views/asset/asset-dynamic-attr.helper.test.ts tests/views/asset/asset-finance.helper.test.ts tests/views/asset/asset-list.helper.test.ts tests/views/asset/asset-lifecycle.helper.test.ts tests/views/asset/category-attr.helper.test.ts tests/views/asset/requisition.helper.test.ts tests/views/asset/real-estate-ledger.helper.test.ts`
Expected: PASS

**Step 2: 运行后端资产核心测试**

Run: `mvn -pl ruoyi-asset "-Dtest=AssetAggregateServiceImplTest,AssetAggregateDetailRecentLogsTest,AssetCategoryAttrServiceImplTest,AssetInfoServiceImplTest,AssetDepreciationServiceImplTest,AssetRequisitionServiceImplTest,AssetMaintenanceServiceImplTest,AssetDisposalServiceImplTest,AssetRealEstateOwnershipChangeServiceImplTest,AssetRealEstateUsageChangeServiceImplTest,AssetRealEstateStatusChangeServiceImplTest,AssetRealEstateDisposalServiceImplTest,AssetTimelineServiceImplTest,AssetWorkflowBusinessHandlerTest,AssetReportServiceImplTest,AssetWorkflowSqlMigrationContractTest,AssetRealEstateLifecycleSqlContractTest,AssetMenuSqlContractTest,AssetGovernanceSqlContractTest" test`
Expected: PASS

**Step 3: 运行前端构建校验**

Run: `npm run build`
Workdir: `art-design-pro`
Expected: `vue-tsc --noEmit` 与 `vite build` 完成；如仍被监控模块历史问题阻塞，需在回归报告中显式记录

**Step 4: 检查工作区改动**

Run: `git status --short`
Expected: 仅包含资产系统联调相关文件，以及用户已知的未跟踪历史文件

**Step 5: 产出联调回归报告**

在 `docs/plans/2026-03-16-asset-system-gap-matrix.md` 末尾追加“回归结果”小节，按菜单列出：

- 是否通过
- 失败场景
- 剩余阻塞

**Step 6: 提交**

```bash
git add docs/plans/2026-03-16-asset-system-gap-matrix.md art-design-pro RuoYi-Vue
git commit -m "feat: complete full integration redesign for asset system menus"
```
