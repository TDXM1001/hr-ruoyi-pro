# 中小企业资产台账与生命周期管理重构 Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 基于《中小企业资产台账与生命周期管理设计方案（重构版）》把资产系统拆成可执行的六个批次，先收拢治理口径，再补齐主档治理、审批分派、固定资产语义分叉、不动产时间线、报表预警和文档收口，使系统进入可实施、可验收状态。

**Architecture:** 采用“治理基线优先、业务闭环分批推进”的方式。先统一 SQL、字典、状态和接口口径，再围绕 `asset_info` 聚合台账完善主档归档与生命周期约束；审批层在现有最小闭环上增强到“按业务模板分派待办”，固定资产与不动产动作继续复用共享主档，但在动作层严格分叉，最后用报表、预警、契约文档和聚焦测试完成收口。

**Tech Stack:** Java 17、Spring Boot 3、MyBatis、MySQL、Vue 3、TypeScript、Element Plus、Vitest、RuoYi

---

## 实施前提

- 设计基线文档：`docs/plans/2026-03-16-sme-asset-ledger-lifecycle-design.md`
- 当前后端主线能力已存在于：
  - `RuoYi-Vue/ruoyi-asset`
  - `RuoYi-Vue/ruoyi-workflow`
  - `RuoYi-Vue/ruoyi-admin`
- 当前前端主线能力已存在于：
  - `art-design-pro/src/api/asset/*`
  - `art-design-pro/src/views/asset/*`
  - `art-design-pro/src/api/workflow/task.ts`
  - `art-design-pro/src/views/asset/workflow/*`
- 建议在独立 worktree 中执行本计划，避免和现有开发修改互相污染

## 本阶段非目标

- 不处理 `art-design-pro` 中 `monitor` / `system` 目录历史 TypeScript 报错
- 不引入 Flowable 或其他重量级流程引擎
- 不实现完整采购、供应商、合同、RFID 或 IoT 集成
- 不扩展集团级多账簿、多组织并账能力

## 批次拆分

- 批次一：治理口径、字典、SQL 契约统一
- 批次二：主档归档、安全删除与生命周期约束
- 批次三：审批模板与待办分派增强
- 批次四：固定资产“报废 / 处置”语义正式分叉
- 批次五：不动产动作时间线与资产详情联动
- 批次六：报表预警 MVP、文档收口与整体验证

### Task 1: 收拢治理口径、字典与 SQL 契约

**Files:**

- Create: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/sql/AssetGovernanceSqlContractTest.java`
- Create: `RuoYi-Vue/sql/20260316_asset_governance_patch.sql`
- Modify: `RuoYi-Vue/sql/20260311_asset_dicts.sql`
- Modify: `RuoYi-Vue/sql/20260314_asset_data_model_refactor.sql`
- Modify: `RuoYi-Vue/sql/20260312_asset_workflow_business.sql`
- Modify: `RuoYi-Vue/sql/sql执行.md`
- Modify: `art-design-pro/src/types/asset.ts`
- Modify: `art-design-pro/src/views/asset/list/asset-lifecycle.helper.ts`
- Modify: `art-design-pro/tests/views/asset/asset-lifecycle.helper.test.ts`

**Step 1: 先写 SQL 契约失败测试**

在 `AssetGovernanceSqlContractTest.java` 中先锁定以下口径：

- `asset_type` 最终口径必须是 `1=固定资产 2=不动产`
- `asset_status` 最终口径必须包含 `1=在用 2=领用中 3=维修中 4=盘点中 5=已报废 6=已处置 7=闲置`
- `asset_disposal` 必须存在 `disposal_type`
- 执行说明必须提到 `20260316_asset_governance_patch.sql`

示例断言：

```java
assertAll(
    () -> assertTrue(dictSql.contains("资产类型")),
    () -> assertTrue(refactorSql.contains("资产类型：1=固定资产 2=不动产")),
    () -> assertTrue(refactorSql.contains("资产状态：1=在用 2=领用中 3=维修中 4=盘点中 5=已报废 6=已处置 7=闲置")),
    () -> assertTrue(workflowSql.contains("`disposal_type` varchar(20)")),
    () -> assertTrue(executionDoc.contains("20260316_asset_governance_patch.sql"))
);
```

**Step 2: 运行测试确认失败**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetGovernanceSqlContractTest test`

Expected: FAIL，原因应为治理补丁脚本和对应契约尚不存在。

**Step 3: 编写治理补丁与脚本说明**

在 `20260316_asset_governance_patch.sql` 中至少补齐：

- 旧库 `asset_type` 字典纠偏
- `asset_status` 缺失值补齐
- 与 `asset_disposal.disposal_type` 相关的历史数据默认值或检查说明
- 对旧菜单、旧状态、旧字典口径的升级备注

同时修改 `20260311_asset_dicts.sql` 和 `sql执行.md`，确保新库初始化与升级库补丁两条路径都清晰。

**Step 4: 同步前端枚举与帮助方法**

在 `art-design-pro/src/types/asset.ts` 与 `asset-lifecycle.helper.ts` 中同步：

- `assetType` 最终口径
- `assetStatus` 最终口径
- 闲置、报废、处置的动作可见性规则

并补齐 `asset-lifecycle.helper.test.ts` 覆盖字典口径和按钮分流。

**Step 5: 重新运行测试确认通过**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetGovernanceSqlContractTest test`

Expected: PASS

Run: `cd art-design-pro && npx vitest run tests/views/asset/asset-lifecycle.helper.test.ts`

Expected: PASS

**Step 6: Commit**

```bash
git add RuoYi-Vue/sql/20260311_asset_dicts.sql RuoYi-Vue/sql/20260314_asset_data_model_refactor.sql RuoYi-Vue/sql/20260312_asset_workflow_business.sql RuoYi-Vue/sql/20260316_asset_governance_patch.sql RuoYi-Vue/sql/sql执行.md RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/sql/AssetGovernanceSqlContractTest.java art-design-pro/src/types/asset.ts art-design-pro/src/views/asset/list/asset-lifecycle.helper.ts art-design-pro/tests/views/asset/asset-lifecycle.helper.test.ts
git commit -m "test: lock asset governance dictionaries and sql contract"
```

### Task 2: 落地主档归档、安全删除与生命周期约束

**Files:**

- Create: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetInfoServiceImplTest.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetInfo.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetInfoService.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetInfoServiceImpl.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetInfoMapper.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetInfoMapper.xml`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetInfoController.java`
- Modify: `art-design-pro/src/api/asset/info.ts`
- Modify: `art-design-pro/src/views/asset/list/index.vue`
- Modify: `art-design-pro/src/views/asset/list/asset-list.helper.ts`
- Modify: `art-design-pro/tests/api/asset-info.test.ts`
- Modify: `art-design-pro/tests/views/asset/asset-list.helper.test.ts`

**Step 1: 先写失败测试，锁定“删除不是处置”**

在 `AssetInfoServiceImplTest.java` 中先锁定以下行为：

- 已报废、已处置资产不能通过普通删除接口物理删除
- 默认删除改为逻辑归档，不再直接删行
- 有未完成动作单据的资产不能归档
- 归档资产默认不在列表中展示，但可通过查询条件查看

示例：

```java
@Test
void shouldArchiveInsteadOfPhysicalDelete() {
    when(assetInfoMapper.selectAssetInfoByAssetId(1001L)).thenReturn(activeAsset());
    service.deleteAssetInfoByAssetIds(new Long[]{1001L});
    verify(assetInfoMapper).archiveAssetInfoByAssetIds(new Long[]{1001L});
    verify(assetInfoMapper, never()).deleteAssetInfoByAssetIds(any());
}
```

**Step 2: 运行测试确认失败**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetInfoServiceImplTest test`

Expected: FAIL，原因应为当前服务仍直接调用物理删除。

**Step 3: 改造主档删除语义**

最小实现要求：

- `deleteAssetInfoByAssetIds` 改为逻辑归档
- `AssetInfoMapper.xml` 默认过滤 `del_flag != '2'`
- 仅误建、草稿或允许归档的数据允许进入归档流程
- 对存在未完成领用、维修、处置、不动产动作单据的资产返回明确错误

**Step 4: 同步控制器与前端交互**

在 `AssetInfoController.java` 与前端列表页同步以下口径：

- UI 文案从“删除”调整为“归档”或“移出日常台账”
- 默认列表不显示归档资产
- 需要时保留“查看已归档”筛选入口

**Step 5: 重新运行测试确认通过**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetInfoServiceImplTest,AssetAggregateServiceImplTest test`

Expected: PASS

Run: `cd art-design-pro && npx vitest run tests/api/asset-info.test.ts tests/views/asset/asset-list.helper.test.ts`

Expected: PASS

**Step 6: Commit**

```bash
git add RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetInfo.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetInfoService.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetInfoServiceImpl.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetInfoMapper.java RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetInfoMapper.xml RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetInfoController.java RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetInfoServiceImplTest.java art-design-pro/src/api/asset/info.ts art-design-pro/src/views/asset/list/index.vue art-design-pro/src/views/asset/list/asset-list.helper.ts art-design-pro/tests/api/asset-info.test.ts art-design-pro/tests/views/asset/asset-list.helper.test.ts
git commit -m "feat: add asset archive semantics and safe ledger removal"
```

### Task 3: 增强审批模板与待办分派基线

**Files:**

- Create: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/domain/WfApprovalTemplate.java`
- Create: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/mapper/WfApprovalTemplateMapper.java`
- Create: `RuoYi-Vue/ruoyi-workflow/src/main/resources/mapper/workflow/WfApprovalTemplateMapper.xml`
- Create: `RuoYi-Vue/ruoyi-workflow/src/test/java/com/ruoyi/workflow/service/impl/SimpleApprovalEngineAssignmentTest.java`
- Modify: `RuoYi-Vue/sql/20260312_asset_workflow_business.sql`
- Modify: `RuoYi-Vue/sql/20260316_asset_governance_patch.sql`
- Modify: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/domain/WfApprovalInstance.java`
- Modify: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/domain/vo/WorkflowTaskVo.java`
- Modify: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/service/IApprovalEngine.java`
- Modify: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/service/impl/SimpleApprovalEngineImpl.java`
- Modify: `RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/mapper/WfApprovalInstanceMapper.java`
- Modify: `RuoYi-Vue/ruoyi-workflow/src/main/resources/mapper/workflow/WfApprovalInstanceMapper.xml`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/workflow/WorkflowTaskController.java`
- Modify: `art-design-pro/src/api/workflow/task.ts`
- Modify: `art-design-pro/src/views/asset/workflow/todo/index.vue`
- Modify: `art-design-pro/src/views/asset/workflow/done/index.vue`

**Step 1: 先写失败测试，锁定“待办不能全员可见”**

在 `SimpleApprovalEngineAssignmentTest.java` 中先锁定：

- `startProcess` 会按 `businessType` 解析审批模板
- 审批实例会记录 `approverId`
- `getTasks(approverId)` 只返回分配给该审批人的待办
- 无模板时应返回明确错误，而不是落成“所有人都能审批”

示例：

```java
@Test
void shouldReturnTodoOnlyForAssignedApprover() {
    Long instanceId = approvalEngine.startProcess("REQ-20260316-001", "asset_requisition");
    List<WorkflowTaskVo> adminTasks = approvalEngine.getTasks(1L);
    List<WorkflowTaskVo> otherTasks = approvalEngine.getTasks(2L);
    assertEquals(1, adminTasks.size());
    assertTrue(otherTasks.isEmpty());
}
```

**Step 2: 运行测试确认失败**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-workflow -Dtest=SimpleApprovalEngineAssignmentTest test`

Expected: FAIL，原因应为当前待办查询未真正按审批人过滤，也无模板解析能力。

**Step 3: 扩展流程模板与实例分派**

最小实现要求：

- 新增 `wf_approval_template` 领域对象与 Mapper
- 模板至少支持：
  - `business_type`
  - `approver_id`
  - `template_name`
  - `status`
- 发起流程时按 `businessType` 找模板并把 `approverId` 落到实例
- `selectTodoTaskList` 只返回当前审批人的待办

**Step 4: 同步前端待办与已办视图**

前端只需做最小同步：

- 展示审批人、业务类型、状态标签
- 待办页不再假定“所有待办都对当前用户可见”
- 已办页补齐审批意见和处理时间展示

**Step 5: 重新运行测试确认通过**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-workflow -Dtest=SimpleApprovalEngineImplTest,SimpleApprovalEngineAssignmentTest test`

Expected: PASS

Run: `cd art-design-pro && npx vitest run tests/api/asset-requisition.test.ts`

Expected: PASS 或不变更；若待办 API mock 已调整，则相关 workflow 页面本地联调应正常

**Step 6: Commit**

```bash
git add RuoYi-Vue/sql/20260312_asset_workflow_business.sql RuoYi-Vue/sql/20260316_asset_governance_patch.sql RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/domain/WfApprovalTemplate.java RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/mapper/WfApprovalTemplateMapper.java RuoYi-Vue/ruoyi-workflow/src/main/resources/mapper/workflow/WfApprovalTemplateMapper.xml RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/domain/WfApprovalInstance.java RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/domain/vo/WorkflowTaskVo.java RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/service/IApprovalEngine.java RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/service/impl/SimpleApprovalEngineImpl.java RuoYi-Vue/ruoyi-workflow/src/main/java/com/ruoyi/workflow/mapper/WfApprovalInstanceMapper.java RuoYi-Vue/ruoyi-workflow/src/main/resources/mapper/workflow/WfApprovalInstanceMapper.xml RuoYi-Vue/ruoyi-workflow/src/test/java/com/ruoyi/workflow/service/impl/SimpleApprovalEngineAssignmentTest.java RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/workflow/WorkflowTaskController.java art-design-pro/src/api/workflow/task.ts art-design-pro/src/views/asset/workflow/todo/index.vue art-design-pro/src/views/asset/workflow/done/index.vue
git commit -m "feat: assign workflow tasks by approval template"
```

### Task 4: 让固定资产“报废 / 处置”正式分叉

**Files:**

- Create: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetDisposalSemanticsServiceImplTest.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetDisposal.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetDisposalService.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetDisposalServiceImpl.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetWorkflowBusinessHandler.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetDisposalMapper.xml`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetDisposalController.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetWorkflowBusinessHandlerTest.java`
- Modify: `art-design-pro/src/api/asset/disposal.ts`
- Modify: `art-design-pro/src/views/asset/disposal/index.vue`
- Modify: `art-design-pro/src/views/asset/list/index.vue`
- Modify: `art-design-pro/src/views/asset/list/asset-lifecycle.helper.ts`
- Modify: `art-design-pro/src/types/asset.ts`
- Modify: `art-design-pro/tests/api/asset-disposal.test.ts`
- Modify: `art-design-pro/tests/views/asset/asset-lifecycle.helper.test.ts`

**Step 1: 先写失败测试，锁定报废与处置不是同一个结果**

在 `AssetDisposalSemanticsServiceImplTest.java` 中先锁定：

- `disposalType=scrap` 审批通过后资产状态应为 `5=已报废`
- `disposalType=sell|transfer|donate` 审批通过后资产状态应为 `6=已处置`
- 驳回时资产恢复为 `1=在用`
- 未填写 `disposalType` 时返回明确错误

示例：

```java
@Test
void shouldMarkAssetDisposedWhenDisposalTypeIsSell() {
    AssetDisposal disposal = buildDisposal("sell");
    service.insertAssetDisposal(disposal);
    workflowBusinessHandler.onApprove("asset_disposal", disposal.getDisposalNo());
    verify(assetInfoMapper).updateAssetInfo(argThat(info -> "6".equals(info.getAssetStatus())));
}
```

**Step 2: 运行测试确认失败**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset,ruoyi-workflow -am "-Dtest=AssetDisposalSemanticsServiceImplTest,AssetWorkflowBusinessHandlerTest" test`

Expected: FAIL，原因应为当前审批通过统一回写为“已报废”。

**Step 3: 最小实现分叉语义**

实现要求：

- `AssetDisposal` 明确约束 `disposalType`
- 服务层拒绝空的 `disposalType`
- `AssetWorkflowBusinessHandler.approveDisposal` 按 `disposalType` 分叉回写 `5` 或 `6`
- 处置单列表和详情中清晰展示“报废 / 处置”类型

**Step 4: 同步前端动作入口和页面文案**

前端应同步：

- 列表页动作文案区分“报废”与“处置”
- 处置页表单强制选择 `disposalType`
- 生命周期按钮在不同状态下正确隐藏

**Step 5: 重新运行测试确认通过**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset,ruoyi-workflow -am "-Dtest=AssetDisposalSemanticsServiceImplTest,AssetWorkflowBusinessHandlerTest,AssetDisposalServiceImplTest" test`

Expected: PASS

Run: `cd art-design-pro && npx vitest run tests/api/asset-disposal.test.ts tests/views/asset/asset-lifecycle.helper.test.ts`

Expected: PASS

**Step 6: Commit**

```bash
git add RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/AssetDisposal.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetDisposalService.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetDisposalServiceImpl.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetWorkflowBusinessHandler.java RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetDisposalMapper.xml RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetDisposalController.java RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetDisposalSemanticsServiceImplTest.java RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetWorkflowBusinessHandlerTest.java art-design-pro/src/api/asset/disposal.ts art-design-pro/src/views/asset/disposal/index.vue art-design-pro/src/views/asset/list/index.vue art-design-pro/src/views/asset/list/asset-lifecycle.helper.ts art-design-pro/src/types/asset.ts art-design-pro/tests/api/asset-disposal.test.ts art-design-pro/tests/views/asset/asset-lifecycle.helper.test.ts
git commit -m "feat: split fixed asset scrap and disposal semantics"
```

### Task 5: 补齐不动产动作时间线与资产详情联动

**Files:**

- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo/AssetTimelineVo.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetTimelineMapper.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetTimelineMapper.xml`
- Create: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetTimelineServiceImplTest.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo/AssetDetailVo.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetAggregateService.java`
- Modify: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetAggregateServiceImpl.java`
- Modify: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetInfoController.java`
- Modify: `art-design-pro/src/types/asset.ts`
- Modify: `art-design-pro/src/views/asset/real-estate/real-estate-lifecycle.helper.ts`
- Modify: `art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue`
- Modify: `art-design-pro/src/views/asset/list/index.vue`
- Modify: `art-design-pro/tests/views/asset/real-estate-ledger.helper.test.ts`
- Modify: `art-design-pro/tests/api/asset-info.test.ts`

**Step 1: 先写失败测试，锁定资产详情需要返回动作时间线**

在 `AssetTimelineServiceImplTest.java` 或 `AssetAggregateServiceImplTest.java` 中先锁定：

- 资产详情会返回最近动作时间线
- 固定资产动作和不动产动作都会被统一映射到时间线结构
- 被驳回动作也保留历史记录，但不改主档事实

示例：

```java
assertEquals("REAL_ESTATE_OWNERSHIP_CHANGE", timeline.get(0).getActionType());
assertEquals("approved", timeline.get(0).getDocStatus());
```

**Step 2: 运行测试确认失败**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetTimelineServiceImplTest,AssetAggregateDetailRecentLogsTest test`

Expected: FAIL，原因应为当前详情仅返回折旧日志，不返回统一动作时间线。

**Step 3: 建立统一时间线查询**

最小实现要求：

- 新增统一 `AssetTimelineVo`
- 以 `assetId` 聚合：
  - 领用
  - 维修
  - 报废/处置
  - 权属变更
  - 用途变更
  - 状态变更
  - 不动产注销/处置
- `AssetDetailVo` 增加 `timeline`

**Step 4: 同步前端详情展示与不动产入口判断**

前端应同步：

- 在资产详情或编辑抽屉中展示最近时间线
- 不动产页基于时间线和当前状态展示最近关键动作
- `real-estate-lifecycle.helper.ts` 补齐入口判断，去掉“可点但必失败”的动作

**Step 5: 重新运行测试确认通过**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetTimelineServiceImplTest,AssetAggregateServiceImplTest,AssetAggregateDetailRecentLogsTest test`

Expected: PASS

Run: `cd art-design-pro && npx vitest run tests/api/asset-info.test.ts tests/views/asset/real-estate-ledger.helper.test.ts`

Expected: PASS

**Step 6: Commit**

```bash
git add RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo/AssetTimelineVo.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetTimelineMapper.java RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetTimelineMapper.xml RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo/AssetDetailVo.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetAggregateService.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetAggregateServiceImpl.java RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetInfoController.java RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetTimelineServiceImplTest.java art-design-pro/src/types/asset.ts art-design-pro/src/views/asset/real-estate/real-estate-lifecycle.helper.ts art-design-pro/src/views/asset/list/modules/asset-edit-drawer.vue art-design-pro/src/views/asset/list/index.vue art-design-pro/tests/views/asset/real-estate-ledger.helper.test.ts art-design-pro/tests/api/asset-info.test.ts
git commit -m "feat: add unified asset action timeline to detail view"
```

### Task 6: 落地报表预警 MVP、契约文档与整体验证

**Files:**

- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo/AssetReportSummaryVo.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo/AssetWarningVo.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetReportMapper.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetReportMapper.xml`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetReportService.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetReportServiceImpl.java`
- Create: `RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetReportController.java`
- Create: `RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetReportServiceImplTest.java`
- Create: `art-design-pro/src/api/asset/report.ts`
- Create: `art-design-pro/src/views/asset/report/index.vue`
- Create: `art-design-pro/tests/api/asset-report.test.ts`
- Modify: `docs/plans/2026-03-14-asset-api-contract-notes.md`
- Modify: `RuoYi-Vue/sql/sql执行.md`
- Modify: `docs/plans/2026-03-16-sme-asset-ledger-lifecycle-design.md`

**Step 1: 先写失败测试，锁定中小企业最小管理视图**

在 `AssetReportServiceImplTest.java` 中先锁定：

- 能按资产类型、状态、部门汇总资产数量和原值
- 能返回闲置、维修中、待审批、土地到期等预警视图

示例：

```java
assertAll(
    () -> assertEquals(12L, summary.getFixedAssetCount()),
    () -> assertEquals(3L, warnings.getIdleAssets().size()),
    () -> assertEquals(1L, warnings.getLandTermExpiringAssets().size())
);
```

**Step 2: 运行测试确认失败**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset -Dtest=AssetReportServiceImplTest test`

Expected: FAIL，原因应为报表服务和 Mapper 尚不存在。

**Step 3: 实现报表与预警服务**

最小实现要求：

- 汇总统计：
  - 按资产类型统计数量与原值
  - 按状态统计数量
  - 按部门统计在用资产
- 风险预警：
  - 闲置资产
  - 维修中未关闭资产
  - 待审批关键动作
  - 不动产土地期限临近到期

**Step 4: 实现前端报表页**

在 `art-design-pro/src/views/asset/report/index.vue` 中最小完成：

- 统计卡片
- 预警列表
- 简单筛选

不追求大屏，先保证口径可信。

**Step 5: 同步契约文档与执行说明**

更新：

- `2026-03-14-asset-api-contract-notes.md`
- `sql执行.md`
- `2026-03-16-sme-asset-ledger-lifecycle-design.md`

明确新增报表接口、预警口径、归档语义和处置分叉口径。

**Step 6: 运行最终聚焦验证**

Run: `cd RuoYi-Vue && mvn -pl ruoyi-asset,ruoyi-workflow -am "-Dtest=AssetGovernanceSqlContractTest,AssetInfoServiceImplTest,AssetDisposalSemanticsServiceImplTest,AssetWorkflowBusinessHandlerTest,AssetTimelineServiceImplTest,AssetReportServiceImplTest,SimpleApprovalEngineImplTest,SimpleApprovalEngineAssignmentTest" test`

Expected: PASS

Run: `cd art-design-pro && npx vitest run tests/api/asset-info.test.ts tests/api/asset-disposal.test.ts tests/api/asset-report.test.ts tests/views/asset/asset-list.helper.test.ts tests/views/asset/asset-lifecycle.helper.test.ts tests/views/asset/real-estate-ledger.helper.test.ts`

Expected: PASS

**Step 7: Commit**

```bash
git add RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo/AssetReportSummaryVo.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/domain/vo/AssetWarningVo.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/mapper/AssetReportMapper.java RuoYi-Vue/ruoyi-asset/src/main/resources/mapper/asset/AssetReportMapper.xml RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/IAssetReportService.java RuoYi-Vue/ruoyi-asset/src/main/java/com/ruoyi/asset/service/impl/AssetReportServiceImpl.java RuoYi-Vue/ruoyi-admin/src/main/java/com/ruoyi/web/controller/asset/AssetReportController.java RuoYi-Vue/ruoyi-asset/src/test/java/com/ruoyi/asset/service/impl/AssetReportServiceImplTest.java art-design-pro/src/api/asset/report.ts art-design-pro/src/views/asset/report/index.vue art-design-pro/tests/api/asset-report.test.ts docs/plans/2026-03-14-asset-api-contract-notes.md RuoYi-Vue/sql/sql执行.md docs/plans/2026-03-16-sme-asset-ledger-lifecycle-design.md
git commit -m "feat: add asset reports warnings and delivery docs"
```

## 完成定义

- 字典、状态、SQL、菜单、接口契约口径一致
- 主档删除改为可审计的归档语义
- 审批待办已按审批人分派，不再全员可见
- 固定资产“报废 / 处置”语义正式分叉
- 资产详情可查看统一动作时间线
- 报表页可展示中小企业最小统计与预警能力
- 所有关键后端测试和前端 Vitest 聚焦用例通过

## 风险与对策

- 风险 1：旧库升级时字典与状态口径继续漂移
  - 对策：先做 SQL 契约测试，再补升级补丁，不直接依赖人工执行记忆
- 风险 2：归档语义改造影响现有删除操作习惯
  - 对策：同步改后端接口、前端文案和执行文档，避免“删除”和“归档”混用
- 风险 3：审批模板增强后影响当前最小闭环
  - 对策：先锁定 `SimpleApprovalEngineImplTest` 现有行为，再叠加分派测试
- 风险 4：报表与预警范围失控
  - 对策：只做数量、原值、状态、闲置、维修中、待审批、土地期限四类最小口径

